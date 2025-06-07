package ru.yandex.practicum.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

@Configuration
@RequiredArgsConstructor
public class KafkaConsumerHubConfig {
    private final KafkaPropertiesConfig kafkaPropertiesConfig;

    @Bean
    public KafkaConsumer<String, HubEventAvro> getHubEventConsumer(KafkaConsumerFactory factory) {
        return factory.createConsumer(
                kafkaPropertiesConfig.getConsumerHubGroupId(),
                kafkaPropertiesConfig.getConsumerHubClientIdConfig(),
                kafkaPropertiesConfig.getConsumerHubKeyDeserializer(),
                kafkaPropertiesConfig.getConsumerHubValueDeserializer()
        );
    }
}
