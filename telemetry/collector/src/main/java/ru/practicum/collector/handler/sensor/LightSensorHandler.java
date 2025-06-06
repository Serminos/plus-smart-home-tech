package ru.practicum.collector.handler.sensor;

import com.google.protobuf.util.Timestamps;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.protobuf.telemetry.event.LightSensorEventProto;
import ru.yandex.practicum.protobuf.telemetry.event.SensorEventProto;

@Component
public class LightSensorHandler extends BaseSensorHandler {

    public LightSensorHandler(KafkaTemplate<String, SensorEventAvro> kafkaTemplate) {
        super(kafkaTemplate);
    }

    @Override
    public SensorEventAvro toSensorEventAvro(SensorEventProto sensorEvent) {
        LightSensorEventProto event = sensorEvent.getLightSensorEvent();

        return SensorEventAvro.newBuilder()
                .setId(sensorEvent.getId())
                .setHubId(sensorEvent.getHubId())
                .setTimestamp(Timestamps.toMillis(sensorEvent.getTimestamp()))
                .setPayload(new LightSensorAvro(event.getLinkQuality(), event.getLuminosity()))
                .build();
    }

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.LIGHT_SENSOR_EVENT;
    }
}