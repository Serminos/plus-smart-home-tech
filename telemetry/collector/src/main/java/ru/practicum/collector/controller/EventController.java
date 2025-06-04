package ru.practicum.collector.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.collector.model.hub.*;
import ru.practicum.collector.model.sensor.*;
import ru.practicum.collector.service.HubEventService;
import ru.practicum.collector.service.SensorEventService;

@Slf4j
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {
    private final HubEventService hubEventService;
    private final SensorEventService sensorEventService;

    @PostMapping("/hubs")
    public void collectHubEvent(@Valid @RequestBody HubEvent hubEvent) {
        log.info("Эндпоинт для обработки событий от хаба hubEvent = {}", hubEvent);

        switch (hubEvent.getType()) {
            case DEVICE_ADDED -> hubEventService.send((DeviceAddedEvent) hubEvent);
            case DEVICE_REMOVED -> hubEventService.send((DeviceRemovedEvent) hubEvent);
            case SCENARIO_ADDED -> hubEventService.send((ScenarioAddedEvent) hubEvent);
            case SCENARIO_REMOVED -> hubEventService.send((ScenarioRemovedEvent) hubEvent);
            default -> throw new IllegalArgumentException("Неизвестный HubEvent: " + hubEvent.getType());
        }
    }

    @PostMapping("/sensors")
    public void collectSensorEvent(@Valid @RequestBody SensorEvent sensorEvent) {
        log.info("Эндпоинт для обработки событий от датчиков sensorEvent = {}", sensorEvent);

        switch (sensorEvent.getType()) {
            case LIGHT_SENSOR_EVENT -> sensorEventService.send((LightSensorEvent) sensorEvent);
            case MOTION_SENSOR_EVENT -> sensorEventService.send((MotionSensorEvent) sensorEvent);
            case CLIMATE_SENSOR_EVENT -> sensorEventService.send((ClimateSensorEvent) sensorEvent);
            case SWITCH_SENSOR_EVENT -> sensorEventService.send((SwitchSensorEvent) sensorEvent);
            case TEMPERATURE_SENSOR_EVENT -> sensorEventService.send((TemperatureSensorEvent) sensorEvent);
            default -> throw new IllegalArgumentException("Неизвестный тип события: " + sensorEvent.getType());
        }
    }
}
