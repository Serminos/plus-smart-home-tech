package ru.yandex.practicum.config;

import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
@RequiredArgsConstructor
public class KafkaProducerConfig {
    private final KafkaPropertiesConfig kafkaPropertiesConfig;

    @Bean
    public Producer<String, SpecificRecordBase> getProducer() {
        Properties config = new Properties();

        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaPropertiesConfig.getBootstrapService());
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, kafkaPropertiesConfig.getProducerKeySerializer());
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, kafkaPropertiesConfig.getProducerValueSerializer());

        return new KafkaProducer<>(config);
    }

}
