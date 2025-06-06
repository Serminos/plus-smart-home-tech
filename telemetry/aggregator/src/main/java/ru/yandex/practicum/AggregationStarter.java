package ru.yandex.practicum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.handler.AggregatorEventHandler;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {
    private final AggregatorEventHandler eventHandler;
    private final Producer<String, SpecificRecordBase> producer;
    private final Consumer<String, SpecificRecordBase> consumer;
    private final Duration CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(1000);
    @Value("${kafka.telemetry-sensors-topic}")
    private String telemetrySensorsTopic;
    @Value("${kafka.telemetry-snapshots-topic}")
    private String telemetrySnapshotTopic;

    public void start() {

        try {
            consumer.subscribe(List.of(telemetrySensorsTopic));
            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
            while (true) {
                ConsumerRecords<String, SpecificRecordBase> records = consumer.poll(CONSUME_ATTEMPT_TIMEOUT);

                for (ConsumerRecord<String, SpecificRecordBase> record : records) {
                    log.info("обрабатываем сообщение {}", record.value());
                    SensorEventAvro eventAvro = (SensorEventAvro) record.value();
                    Optional<SensorsSnapshotAvro> snapshotAvro = eventHandler.updateState(eventAvro);
                    log.info("Получили снимок состояния {}", snapshotAvro);
                    if (snapshotAvro.isPresent()) {
                        log.info("запись снимка в топик Kafka");
                        ProducerRecord<String, SpecificRecordBase> message = new ProducerRecord<>(
                                telemetrySnapshotTopic,
                                null,
                                eventAvro.getTimestamp(),
                                eventAvro.getHubId(),
                                snapshotAvro.get()
                        );
                        producer.send(message);
                    }
                }
                consumer.commitSync();
            }
        } catch (WakeupException ignored) {
            // игнорируем - закрываем консьюмер и продюсер в блоке finally
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {
            try {
                producer.flush();
                consumer.commitSync();
            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
                log.info("Закрываем продюсер");
                producer.close();
            }
        }
    }
}
