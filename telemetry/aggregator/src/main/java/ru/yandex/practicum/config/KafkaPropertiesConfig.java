package ru.yandex.practicum.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
public class KafkaPropertiesConfig {
    @Value("${kafka.producer.bootstrap-server}")
    private String bootstrapService;
    @Value("${kafka.producer.key-serializer}")
    private String producerKeySerializer;
    @Value("${kafka.producer.value-serializer}")
    private String producerValueSerializer;
    @Value("${kafka.consumer.group-id}")
    private String consumerGroupId;
    @Value("${kafka.consumer.client-id-config}")
    private String consumerClientIdConfig;
    @Value("${kafka.consumer.key-deserializer}")
    private String consumerKeyDeserializer;
    @Value("${kafka.consumer.value-deserializer}")
    private String consumerValueDeserializer;
}
