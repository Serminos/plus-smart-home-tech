package ru.yandex.practicum.model.Enum;

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
