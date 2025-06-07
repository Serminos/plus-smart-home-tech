package ru.practicum.collector.handler.sensor;

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

        return createBaseBuilder(sensorEvent)
                .setPayload(new TemperatureSensorAvro(event.getTemperatureC(), event.getTemperatureF()))
                .build();
    }

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.TEMPERATURE_SENSOR_EVENT;
    }
}
