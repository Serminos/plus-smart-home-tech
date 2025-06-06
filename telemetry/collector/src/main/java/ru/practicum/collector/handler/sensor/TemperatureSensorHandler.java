package ru.practicum.collector.handler.sensor;

import com.google.protobuf.util.Timestamps;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;
import ru.yandex.practicum.protobuf.telemetry.event.SensorEventProto;
import ru.yandex.practicum.protobuf.telemetry.event.TemperatureSensorEventProto;

@Component
public class TemperatureSensorHandler extends BaseSensorHandler {

    public TemperatureSensorHandler(KafkaTemplate<String, SensorEventAvro> kafkaTemplate) {
        super(kafkaTemplate);
    }

    @Override
    public SensorEventAvro toSensorEventAvro(SensorEventProto sensorEvent) {
        TemperatureSensorEventProto event = sensorEvent.getTemperatureSensorEvent();

        return SensorEventAvro.newBuilder()
                .setId(sensorEvent.getId())
                .setHubId(sensorEvent.getHubId())
                .setTimestamp(Timestamps.toMillis(sensorEvent.getTimestamp()))
                .setPayload(new TemperatureSensorAvro(event.getTemperatureC(), event.getTemperatureF()))
                .build();
    }

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.TEMPERATURE_SENSOR_EVENT;
    }
}
