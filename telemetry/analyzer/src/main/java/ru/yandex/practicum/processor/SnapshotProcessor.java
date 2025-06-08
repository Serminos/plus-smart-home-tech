package ru.yandex.practicum.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.handler.snapshot.SnapshotHandler;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

@Slf4j
@Component
public class SnapshotProcessor extends AbstractEventProcessor<SensorsSnapshotAvro> {
    private final SnapshotHandler snapshotHandler;

    public SnapshotProcessor(
            Consumer<String, SensorsSnapshotAvro> snapshotConsumer,
            @Value("${kafka.topic.snapshots:telemetry.snapshots.v1}") String topicName,
            SnapshotHandler snapshotHandler,
            RetryTemplate snapshotRetryTemplate
    ) {
        super(snapshotConsumer, topicName, snapshotRetryTemplate);
        this.snapshotHandler = snapshotHandler;
    }

    @Override
    protected void processRecords(ConsumerRecords<String, SensorsSnapshotAvro> records) {
        for (ConsumerRecord<String, SensorsSnapshotAvro> record : records) {
            try {
                processSingleRecordWithRetry(record);
            } catch (Exception e) {
                log.error("Permanent error processing record [{}:{}] - {}",
                        record.topic(), record.offset(), e.getMessage());
            }
        }
    }

    private void processSingleRecordWithRetry(ConsumerRecord<String, SensorsSnapshotAvro> record) {
        retryTemplate.execute(context -> {
            try {
                return processSingleRecord(record);
            } catch (RuntimeException e) {
                log.warn("Retryable error processing record [{}:{}] (attempt {}): {}",
                        record.topic(), record.offset(),
                        context.getRetryCount() + 1, e.getMessage());
                throw e;
            }
        });
    }

    private Void processSingleRecord(ConsumerRecord<String, SensorsSnapshotAvro> record) {
        log.debug("Processing snapshot [{}:{}]", record.topic(), record.offset());
        snapshotHandler.handleSnapshot(record.value());
        return null;
    }
}