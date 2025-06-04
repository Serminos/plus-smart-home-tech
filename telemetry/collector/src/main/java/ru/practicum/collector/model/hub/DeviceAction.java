package ru.practicum.collector.model.hub;

import lombok.Getter;
import lombok.Setter;
import ru.practicum.collector.model.hub.enums.ActionType;

@Getter
@Setter
public class DeviceAction {
    private String sensorId;
    private ActionType type;
    private Integer value;
}

