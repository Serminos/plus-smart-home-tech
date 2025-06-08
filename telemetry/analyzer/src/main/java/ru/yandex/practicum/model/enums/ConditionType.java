package ru.yandex.practicum.model.enums;

import ru.yandex.practicum.protobuf.telemetry.event.ConditionTypeProto;

public enum ConditionType {
    MOTION,
    LUMINOSITY,
    SWITCH,
    TEMPERATURE,
    CO2LEVEL,
    HUMIDITY;

    public ConditionTypeProto toProto() {
        return ConditionTypeProto.valueOf(this.name());
    }
}
