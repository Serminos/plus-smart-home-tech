package ru.practicum.collector.model.hub;

import lombok.Getter;
import lombok.Setter;
import ru.practicum.collector.model.hub.enums.ConditionOperation;
import ru.practicum.collector.model.hub.enums.ConditionType;

@Getter
@Setter
public class ScenarioCondition {
    private String sensorId;
    private ConditionType type;
    private ConditionOperation operation;
    private Object value;
}
