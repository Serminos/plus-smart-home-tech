package ru.yandex.practicum.handler;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class AggregatorEventHandler {

    private final Map<String, SensorsSnapshotAvro> snapshots = new HashMap<>();

    public Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        String hubId = event.getHubId();
        SensorsSnapshotAvro snapshot = snapshots.get(hubId);

        if (snapshot == null) {
            snapshot = createSensorSnapshotAvro(hubId);
            snapshots.put(hubId, snapshot);
        }

        SensorStateAvro oldState = snapshot.getSensorsState().get(event.getId());

        if (isStateUpdateRequired(event, oldState)) {
            SensorStateAvro newState = createSensorStateAvro(event);
            snapshot.getSensorsState().put(event.getId(), newState);
            snapshot.setTimestamp(event.getTimestamp());
            return Optional.of(snapshot);
        }

        return Optional.empty();
    }

    private boolean isStateUpdateRequired(SensorEventAvro event, SensorStateAvro oldState) {
        if (oldState == null) {
            return true;
        }
        if (oldState.getTimestamp() < event.getTimestamp()) {
            return true;
        }
        return !oldState.getData().equals(event.getPayload());
    }

    private SensorsSnapshotAvro createSensorSnapshotAvro(String hubId) {
        return SensorsSnapshotAvro.newBuilder()
                .setHubId(hubId)
                .setTimestamp(Instant.now().toEpochMilli())
                .setSensorsState(new HashMap<>())
                .build();
    }

    private SensorStateAvro createSensorStateAvro(SensorEventAvro event) {
        return SensorStateAvro.newBuilder()
                .setTimestamp(event.getTimestamp())
                .setData(event.getPayload())
                .build();
    }
}
