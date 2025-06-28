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
    @Value("${kafka.consumer.hub-group-id}")
    private String consumerHubGroupId;
    @Value("${kafka.consumer.hub-client-id-config}")
    private String consumerHubClientIdConfig;
    @Value("${kafka.consumer.hub-key-deserializer}")
    private String consumerHubKeyDeserializer;
    @Value("${kafka.consumer.hub-value-deserializer}")
    private String consumerHubValueDeserializer;
    @Value("${kafka.consumer.snapshot-group-id}")
    private String consumerSnapshotGroupId;
    @Value("${kafka.consumer.snapshot-client-id-config}")
    private String consumerSnapshotClientIdConfig;
    @Value("${kafka.consumer.snapshot-key-deserializer}")
    private String consumerSnapshotKeyDeserializer;
    @Value("${kafka.consumer.snapshot-value-deserializer}")
    private String consumerSnapshotValueDeserializer;

}
