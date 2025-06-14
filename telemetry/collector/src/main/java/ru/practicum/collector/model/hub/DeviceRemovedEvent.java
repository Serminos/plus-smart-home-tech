package ru.practicum.collector.model.hub;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.collector.model.hub.enums.HubEventType;

@Getter
@Setter
@ToString(callSuper = true)
public class DeviceRemovedEvent extends HubEvent {
    private String id;

    @Override
    public HubEventType getType() {
        return HubEventType.DEVICE_REMOVED;
    }
}
