package ru.practicum.collector.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.collector.model.hub.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class HubEventMapper {
    private final Map<Class<? extends HubEvent>, Function<HubEvent, HubEventAvro>> mapperMap = new HashMap<>();

    public HubEventMapper() {
        mapperMap.put(DeviceAddedEvent.class, this::mapDeviceAddedEvent);
        mapperMap.put(DeviceRemovedEvent.class, this::mapDeviceRemovedEvent);
        mapperMap.put(ScenarioAddedEvent.class, this::mapScenarioAddedEvent);
        mapperMap.put(ScenarioRemovedEvent.class, this::mapScenarioRemovedEvent);
    }

    public HubEventAvro toAvro(HubEvent event) {
        Function<HubEvent, HubEventAvro> mapper = mapperMap.get(event.getClass());
        if (mapper == null) {
            throw new IllegalArgumentException("Неизвестный HubEvent: " + event.getClass());
        }
        return mapper.apply(event);
    }

    private HubEventAvro mapDeviceAddedEvent(HubEvent event) {
        DeviceAddedEvent deviceAddedEvent = (DeviceAddedEvent) event;
        DeviceAddedEventAvro payload = DeviceAddedEventAvro.newBuilder()
                .setId(deviceAddedEvent.getId())
                .setType(DeviceTypeAvro.valueOf(deviceAddedEvent.getDeviceType().name()))
                .build();
        return buildHubEventAvro(deviceAddedEvent.getHubId(), deviceAddedEvent.getTimestamp(), payload);
    }

    private HubEventAvro mapDeviceRemovedEvent(HubEvent event) {
        DeviceRemovedEvent deviceRemovedEvent = (DeviceRemovedEvent) event;
        DeviceRemovedEventAvro payload = DeviceRemovedEventAvro.newBuilder()
                .setId(deviceRemovedEvent.getId())
                .build();
        return buildHubEventAvro(deviceRemovedEvent.getHubId(), deviceRemovedEvent.getTimestamp(), payload);
    }

    private HubEventAvro mapScenarioAddedEvent(HubEvent event) {
        ScenarioAddedEvent scenarioAddedEvent = (ScenarioAddedEvent) event;
        List<ScenarioConditionAvro> conditions = scenarioAddedEvent.getConditions().stream()
                .map(c -> ScenarioConditionAvro.newBuilder()
                        .setSensorId(c.getSensorId())
                        .setType(ConditionTypeAvro.valueOf(c.getType().name()))
                        .setOperation(ConditionOperationAvro.valueOf(c.getOperation().name()))
                        .setValue(c.getValue())
                        .build())
                .collect(Collectors.toList());

        List<DeviceActionAvro> actions = scenarioAddedEvent.getActions().stream()
                .map(a -> DeviceActionAvro.newBuilder()
                        .setSensorId(a.getSensorId())
                        .setType(ActionTypeAvro.valueOf(a.getType().name()))
                        .setValue(a.getValue())
                        .build())
                .collect(Collectors.toList());

        ScenarioAddedEventAvro payload = ScenarioAddedEventAvro.newBuilder()
                .setName(scenarioAddedEvent.getName())
                .setConditions(conditions)
                .setActions(actions)
                .build();

        return buildHubEventAvro(event.getHubId(), event.getTimestamp(), payload);
    }

    private HubEventAvro mapScenarioRemovedEvent(HubEvent event) {
        ScenarioRemovedEvent scenarioRemovedEvent = (ScenarioRemovedEvent) event;
        ScenarioRemovedEventAvro payload = ScenarioRemovedEventAvro.newBuilder()
                .setName(scenarioRemovedEvent.getName())
                .build();
        return buildHubEventAvro(scenarioRemovedEvent.getHubId(), scenarioRemovedEvent.getTimestamp(), payload);
    }

    private HubEventAvro buildHubEventAvro(String hubId, Instant timestamp, Object payload) {
        return HubEventAvro.newBuilder()
                .setHubId(hubId)
                .setTimestamp(timestamp != null ? timestamp.toEpochMilli() : System.currentTimeMillis())
                .setPayload(payload)
                .build();
    }
}
