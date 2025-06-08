package ru.practicum.collector.handler.sensor;

import com.google.protobuf.util.Timestamps;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import ru.practicum.collector.handler.SensorEventHandler;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.protobuf.telemetry.event.SensorEventProto;

@RequiredArgsConstructor
public abstract class BaseSensorHandler implements SensorEventHandler {
    private final KafkaTemplate<String, SensorEventAvro> kafkaTemplate;
    @Value("${kafka.topic.sensors:telemetry.sensors.v1}")
    private String sensorTopic;

    @Override
    public void handle(SensorEventProto sensorEvent) {
        kafkaTemplate.send(sensorTopic, null, sensorEvent.getTimestamp().getSeconds(),
                sensorEvent.getHubId(), toSensorEventAvro(sensorEvent));
    }

    protected SensorEventAvro.Builder createBaseBuilder(SensorEventProto sensorEvent) {
        return SensorEventAvro.newBuilder()
                .setId(sensorEvent.getId())
                .setHubId(sensorEvent.getHubId())
                .setTimestamp(Timestamps.toMillis(sensorEvent.getTimestamp()));
    }

    public abstract SensorEventAvro toSensorEventAvro(SensorEventProto sensorEvent);
}
