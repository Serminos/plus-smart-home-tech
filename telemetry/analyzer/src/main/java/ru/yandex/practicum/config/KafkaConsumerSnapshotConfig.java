package ru.yandex.practicum.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

@Configuration
@RequiredArgsConstructor
public class KafkaConsumerSnapshotConfig {
    private final KafkaPropertiesConfig kafkaPropertiesConfig;

    @Bean
    public KafkaConsumer<String, SensorsSnapshotAvro> getSensorsSnapshotConsumer(KafkaConsumerFactory factory) {
        return factory.createConsumer(
                kafkaPropertiesConfig.getConsumerSnapshotGroupId(),
                kafkaPropertiesConfig.getConsumerSnapshotClientIdConfig(),
                kafkaPropertiesConfig.getConsumerSnapshotKeyDeserializer(),
                kafkaPropertiesConfig.getConsumerSnapshotValueDeserializer()
        );
    }
}
