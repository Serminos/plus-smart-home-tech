package ru.yandex.practicum.model.Enum;

import ru.yandex.practicum.protobuf.telemetry.event.ConditionOperationProto;

public enum ConditionOperation {
    EQUALS,
    GREATER_THAN,
    LOWER_THAN;

    public ConditionOperationProto toProto() {
        return ConditionOperationProto.valueOf(this.name());
    }
}