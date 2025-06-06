package ru.practicum.collector.handler.hub;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.protobuf.telemetry.event.DeviceAddedEventProto;
import ru.yandex.practicum.protobuf.telemetry.event.DeviceTypeProto;
import ru.yandex.practicum.protobuf.telemetry.event.HubEventProto;

import com.google.protobuf.util.Timestamps;

@Component
public class DeviceAddedHandler extends BaseHubHandler {

    public DeviceAddedHandler(KafkaTemplate<String, HubEventAvro> kafkaTemplate) {
        super(kafkaTemplate);
    }

    @Override
    public HubEventAvro toHubEventAvro(HubEventProto hubEvent) {
        DeviceAddedEventProto event = hubEvent.getDeviceAdded();

        return HubEventAvro.newBuilder()
                .setHubId(hubEvent.getHubId())
                .setTimestamp(Timestamps.toMillis(hubEvent.getTimestamp()))
                .setPayload(new DeviceAddedEventAvro(event.getId(), toDeviceTypeAvro(event.getType())))
                .build();
    }

    private DeviceTypeAvro toDeviceTypeAvro(DeviceTypeProto deviceType) {
        return DeviceTypeAvro.valueOf(deviceType.name());
    }

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.DEVICE_ADDED;
    }
}
