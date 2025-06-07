package ru.practicum.collector.handler.hub;

import com.google.protobuf.util.Timestamps;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.protobuf.telemetry.event.DeviceRemovedEventProto;
import ru.yandex.practicum.protobuf.telemetry.event.HubEventProto;

@Component
public class DeviceRemovedHandler extends BaseHubHandler {

    public DeviceRemovedHandler(
            KafkaTemplate<String, HubEventAvro> kafkaTemplate,
            @Value("${kafka.topic.hubs:telemetry.hubs.v1}") String hubTopic
    ) {
        super(kafkaTemplate, hubTopic);
    }

    @Override
    public HubEventAvro toHubEventAvro(HubEventProto hubEvent) {
        DeviceRemovedEventProto event = hubEvent.getDeviceRemoved();

        return HubEventAvro.newBuilder()
                .setHubId(hubEvent.getHubId())
                .setTimestamp(Timestamps.toMillis(hubEvent.getTimestamp()))
                .setPayload(new DeviceRemovedEventAvro(event.getId()))
                .build();
    }

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.DEVICE_REMOVED;
    }
}
