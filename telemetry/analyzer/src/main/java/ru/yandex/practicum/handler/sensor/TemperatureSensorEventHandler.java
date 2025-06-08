package ru.yandex.practicum.handler.sensor;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;
import ru.yandex.practicum.model.enums.ConditionType;

@Component
public class TemperatureSensorEventHandler implements SensorEventHandler {
    @Override
    public String getType() {
        return TemperatureSensorAvro.class.getName();
    }

    @Override
    public Integer getSensorValue(ConditionType type, SensorStateAvro sensorStateAvro) {
        TemperatureSensorAvro temperatureSensorAvro = (TemperatureSensorAvro) sensorStateAvro.getData();
        return switch (type) {
            case TEMPERATURE -> temperatureSensorAvro.getTemperatureC();
            default -> null;
        };
    }
}
