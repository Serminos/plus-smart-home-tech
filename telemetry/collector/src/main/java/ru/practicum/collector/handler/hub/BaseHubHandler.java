package ru.practicum.collector.handler.hub;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import ru.practicum.collector.handler.HubEventHandler;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.protobuf.telemetry.event.HubEventProto;

@RequiredArgsConstructor
public abstract class BaseHubHandler implements HubEventHandler {
    protected final KafkaTemplate<String, HubEventAvro> kafkaTemplate;
    protected final String hubTopic;

    @Override
    public void handle(HubEventProto hubEvent) {
        kafkaTemplate.send(hubTopic, null, hubEvent.getTimestamp().getSeconds(),
                hubEvent.getHubId(), toHubEventAvro(hubEvent));
    }

    public abstract HubEventAvro toHubEventAvro(HubEventProto hubEvent);
}
