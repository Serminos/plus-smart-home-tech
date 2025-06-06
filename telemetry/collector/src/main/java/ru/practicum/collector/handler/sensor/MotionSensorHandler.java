package ru.practicum.collector.handler.sensor;

import com.google.protobuf.util.Timestamps;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.protobuf.telemetry.event.MotionSensorEventProto;
import ru.yandex.practicum.protobuf.telemetry.event.SensorEventProto;

@Component
public class MotionSensorHandler extends BaseSensorHandler {

    public MotionSensorHandler(KafkaTemplate<String, SensorEventAvro> kafkaTemplate) {
        super(kafkaTemplate);
    }

    @Override
    public SensorEventAvro toSensorEventAvro(SensorEventProto sensorEvent) {
        MotionSensorEventProto event = sensorEvent.getMotionSensorEvent();

        return SensorEventAvro.newBuilder()
                .setId(sensorEvent.getId())
                .setHubId(sensorEvent.getHubId())
                .setTimestamp(Timestamps.toMillis(sensorEvent.getTimestamp()))
                .setPayload(new MotionSensorAvro(event.getLinkQuality(), event.getMotion(), event.getVoltage()))
                .build();
    }

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.MOTION_SENSOR_EVENT;
    }
}
