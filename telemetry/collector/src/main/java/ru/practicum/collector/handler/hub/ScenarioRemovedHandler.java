package ru.practicum.collector.handler.hub;

import com.google.protobuf.util.Timestamps;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;
import ru.yandex.practicum.protobuf.telemetry.event.HubEventProto;
import ru.yandex.practicum.protobuf.telemetry.event.ScenarioRemovedEventProto;

@Component
public class ScenarioRemovedHandler extends BaseHubHandler {

    public ScenarioRemovedHandler(
            KafkaTemplate<String, HubEventAvro> kafkaTemplate,
            @Value("${kafka.topic.hubs:telemetry.hubs.v1}") String hubTopic
    ) {
        super(kafkaTemplate, hubTopic);
    }

    @Override
    public HubEventAvro toHubEventAvro(HubEventProto hubEvent) {
        ScenarioRemovedEventProto event = hubEvent.getScenarioRemoved();

        return HubEventAvro.newBuilder()
                .setHubId(hubEvent.getHubId())
                .setTimestamp(Timestamps.toMillis(hubEvent.getTimestamp()))
                .setPayload(new ScenarioRemovedEventAvro(event.getName()))
                .build();
    }

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.SCENARIO_REMOVED;
    }
}
