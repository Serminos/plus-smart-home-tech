package ru.practicum.collector.handler.sensor;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.protobuf.telemetry.event.ClimateSensorEventProto;
import ru.yandex.practicum.protobuf.telemetry.event.SensorEventProto;

@Component
public class ClimateSensorHandler extends BaseSensorHandler {

    public ClimateSensorHandler(KafkaTemplate<String, SensorEventAvro> kafkaTemplate) {
        super(kafkaTemplate);
    }

    @Override
    public SensorEventAvro toSensorEventAvro(SensorEventProto sensorEvent) {
        ClimateSensorEventProto event = sensorEvent.getClimateSensorEvent();

        return createBaseBuilder(sensorEvent)
                .setPayload(new ClimateSensorAvro(event.getTemperatureC(), event.getHumidity(), event.getCo2Level()))
                .build();
    }

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.CLIMATE_SENSOR_EVENT;
    }
}
