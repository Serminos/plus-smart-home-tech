package ru.practicum.collector.handler.hub;

import com.google.protobuf.util.Timestamps;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.protobuf.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.protobuf.telemetry.event.HubEventProto;
import ru.yandex.practicum.protobuf.telemetry.event.ScenarioAddedEventProto;
import ru.yandex.practicum.protobuf.telemetry.event.ScenarioConditionProto;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ScenarioAddedHandler extends BaseHubHandler {

    public ScenarioAddedHandler(
            KafkaTemplate<String, HubEventAvro> kafkaTemplate,
            @Value("${kafka.topic.hubs:telemetry.hubs.v1}") String hubTopic
    ) {
        super(kafkaTemplate, hubTopic);
    }

    @Override
    public HubEventAvro toHubEventAvro(HubEventProto hubEvent) {
        ScenarioAddedEventProto event = hubEvent.getScenarioAdded();

        return HubEventAvro.newBuilder()
                .setHubId(hubEvent.getHubId())
                .setTimestamp(Timestamps.toMillis(hubEvent.getTimestamp()))
                .setPayload(new ScenarioAddedEventAvro(
                        event.getName(),
                        toScenarioConditionAvro(event.getConditionList()),
                        toDeviceActionAvro(event.getActionList())))
                .build();
    }

    private List<ScenarioConditionAvro> toScenarioConditionAvro(List<ScenarioConditionProto> scenarioConditions) {
        return scenarioConditions.stream()
                .map(condition -> ScenarioConditionAvro.newBuilder()
                        .setSensorId(condition.getSensorId())
                        .setType(ConditionTypeAvro.valueOf(condition.getType().name()))
                        .setOperation(ConditionOperationAvro.valueOf(condition.getOperation().name()))
                        .setValue(
                                switch (condition.getValueCase()) {
                                    case INT_VALUE -> condition.getIntValue();
                                    case BOOL_VALUE -> condition.getBoolValue();
                                    case VALUE_NOT_SET -> null;
                                }
                        )
                        .build())
                .collect(Collectors.toList());
    }

    private List<DeviceActionAvro> toDeviceActionAvro(List<DeviceActionProto> deviceActions) {
        return deviceActions.stream()
                .map(action -> DeviceActionAvro.newBuilder()
                        .setSensorId(action.getSensorId())
                        .setType(ActionTypeAvro.valueOf(action.getType().name()))
                        .setValue(action.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.SCENARIO_ADDED;
    }
}
