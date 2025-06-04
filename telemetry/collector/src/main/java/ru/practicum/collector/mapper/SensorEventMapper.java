package ru.practicum.collector.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.collector.model.sensor.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
@Component
public class SensorEventMapper {

    private final Map<Class<? extends SensorEvent>, Function<SensorEvent, SensorEventAvro>> mapperMap = new HashMap<>();

    public SensorEventMapper() {
        mapperMap.put(LightSensorEvent.class, this::mapLightSensorEvent);
        mapperMap.put(MotionSensorEvent.class, this::mapMotionSensorEvent);
        mapperMap.put(ClimateSensorEvent.class, this::mapClimateSensorEvent);
        mapperMap.put(SwitchSensorEvent.class, this::mapSwitchSensorEvent);
        mapperMap.put(TemperatureSensorEvent.class, this::mapTemperatureSensorEvent);
    }

    public SensorEventAvro toAvro(SensorEvent event) {
        Function<SensorEvent, SensorEventAvro> mapper = mapperMap.get(event.getClass());
        if (mapper == null) {
            throw new IllegalArgumentException("Неизвестный SensorEvent: " + event.getClass().getName());
        }
        return mapper.apply(event);
    }

    private SensorEventAvro mapLightSensorEvent(SensorEvent event) {
        LightSensorEvent lightSensorEvent = (LightSensorEvent) event;
        LightSensorAvro payload = LightSensorAvro.newBuilder()
                .setLinkQuality(lightSensorEvent.getLinkQuality())
                .setLuminosity(lightSensorEvent.getLuminosity())
                .build();
        return buildSensorEvent(lightSensorEvent.getId(), lightSensorEvent.getHubId(),
                lightSensorEvent.getTimestamp().toEpochMilli(), payload);
    }

    private SensorEventAvro mapMotionSensorEvent(SensorEvent event) {
        MotionSensorEvent motionSensorEvent = (MotionSensorEvent) event;
        MotionSensorAvro payload = MotionSensorAvro.newBuilder()
                .setLinkQuality(motionSensorEvent.getLinkQuality())
                .setMotion(motionSensorEvent.isMotion())
                .setVoltage(motionSensorEvent.getVoltage())
                .build();
        return buildSensorEvent(motionSensorEvent.getId(), motionSensorEvent.getHubId(),
                motionSensorEvent.getTimestamp().toEpochMilli(), payload);
    }

    private SensorEventAvro mapClimateSensorEvent(SensorEvent event) {
        ClimateSensorEvent climateSensorEvent = (ClimateSensorEvent) event;
        ClimateSensorAvro payload = ClimateSensorAvro.newBuilder()
                .setTemperatureC(climateSensorEvent.getTemperatureC())
                .setHumidity(climateSensorEvent.getHumidity())
                .setCo2Level(climateSensorEvent.getCo2Level())
                .build();
        return buildSensorEvent(climateSensorEvent.getId(), climateSensorEvent.getHubId(),
                climateSensorEvent.getTimestamp().toEpochMilli(), payload);
    }

    private SensorEventAvro mapSwitchSensorEvent(SensorEvent event) {
        SwitchSensorEvent switchSensorEvent = (SwitchSensorEvent) event;
        SwitchSensorAvro payload = SwitchSensorAvro.newBuilder()
                .setState(switchSensorEvent.isState())
                .build();
        return buildSensorEvent(switchSensorEvent.getId(), switchSensorEvent.getHubId(),
                switchSensorEvent.getTimestamp().toEpochMilli(), payload);
    }

    private SensorEventAvro mapTemperatureSensorEvent(SensorEvent event) {
        TemperatureSensorEvent temperatureSensorEvent = (TemperatureSensorEvent) event;
        TemperatureSensorAvro payload = TemperatureSensorAvro.newBuilder()
                .setTemperatureC(temperatureSensorEvent.getTemperatureC())
                .setTemperatureF(temperatureSensorEvent.getTemperatureF())
                .build();
        return buildSensorEvent(temperatureSensorEvent.getId(), temperatureSensorEvent.getHubId(),
                temperatureSensorEvent.getTimestamp().toEpochMilli(), payload);
    }

    private SensorEventAvro buildSensorEvent(String id, String hubId, long timestamp, Object payload) {
        return SensorEventAvro.newBuilder()
                .setId(id)
                .setHubId(hubId)
                .setTimestamp(timestamp)
                .setPayload(payload)
                .build();
    }
}
