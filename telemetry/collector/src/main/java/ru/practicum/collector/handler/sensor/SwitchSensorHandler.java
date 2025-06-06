package ru.practicum.collector.handler.sensor;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;
import ru.yandex.practicum.protobuf.telemetry.event.SensorEventProto;
import ru.yandex.practicum.protobuf.telemetry.event.SwitchSensorEventProto;

@Component
public class SwitchSensorHandler extends BaseSensorHandler {

    public SwitchSensorHandler(KafkaTemplate<String, SensorEventAvro> kafkaTemplate) {
        super(kafkaTemplate);
    }

    @Override
    public SensorEventAvro toSensorEventAvro(SensorEventProto sensorEvent) {
        SwitchSensorEventProto event = sensorEvent.getSwitchSensorEvent();

        return SensorEventAvro.newBuilder()
                .setId(sensorEvent.getId())
                .setHubId(sensorEvent.getHubId())
                .setTimestamp(sensorEvent.getTimestamp().getSeconds())
                .setPayload(new SwitchSensorAvro(event.getState()))
                .build();
    }

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.SWITCH_SENSOR_EVENT;
    }
}

