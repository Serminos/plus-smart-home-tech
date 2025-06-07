package ru.yandex.practicum.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
@RequiredArgsConstructor
public class KafkaConsumerFactory {
    private final KafkaPropertiesConfig kafkaPropertiesConfig;

    public <K, V> KafkaConsumer<K, V> createConsumer(
            String groupId,
            String clientId,
            String keyDeserializer,
            String valueDeserializer
    ) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaPropertiesConfig.getBootstrapService());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, clientId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer);
        return new KafkaConsumer<>(props);
    }
}
