package ru.yandex.practicum.config;

import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig {
    private final KafkaPropertiesConfig kafkaPropertiesConfig;

    @Bean
    public KafkaConsumer<String, SpecificRecordBase> getConsumer() {
        Properties config = new Properties();
        config.put(ConsumerConfig.CLIENT_ID_CONFIG, kafkaPropertiesConfig.getConsumerClientIdConfig());
        config.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaPropertiesConfig.getConsumerGroupId());
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaPropertiesConfig.getBootstrapService());
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, kafkaPropertiesConfig.getConsumerKeyDeserializer());
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, kafkaPropertiesConfig.getConsumerValueDeserializer());

        return new KafkaConsumer<>(config);
    }
}
