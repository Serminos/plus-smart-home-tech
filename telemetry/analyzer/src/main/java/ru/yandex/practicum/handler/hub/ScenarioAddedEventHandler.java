package ru.yandex.practicum.handler.hub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.model.Action;
import ru.yandex.practicum.model.Condition;
import ru.yandex.practicum.model.Enum.ActionType;
import ru.yandex.practicum.model.Enum.ConditionOperation;
import ru.yandex.practicum.model.Enum.ConditionType;
import ru.yandex.practicum.model.Scenario;
import ru.yandex.practicum.model.Sensor;
import ru.yandex.practicum.repository.ActionRepository;
import ru.yandex.practicum.repository.ConditionRepository;
import ru.yandex.practicum.repository.ScenarioRepository;
import ru.yandex.practicum.repository.SensorRepository;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScenarioAddedEventHandler implements HubEventHandler {
    private final ScenarioRepository scenarioRepository;
    private final SensorRepository sensorRepository;
    private final ActionRepository actionRepository;
    private final ConditionRepository conditionRepository;

    @Override
    public String getType() {
        return ScenarioAddedEventAvro.class.getName();
    }

    @Override
    @Transactional
    public void handle(HubEventAvro hubEventAvro) {
        ScenarioAddedEventAvro scenarioAddedEventAvro = (ScenarioAddedEventAvro) hubEventAvro.getPayload();

        if (!checkSensors(getConditionsSensorIds(scenarioAddedEventAvro.getConditions()), hubEventAvro.getHubId())) {
            throw new RuntimeException("Не найдены сенсоры условий сценария");
        }
        if (!checkSensors(getActionsSensorIds(scenarioAddedEventAvro.getActions()), hubEventAvro.getHubId())) {
            throw new RuntimeException("Не найдены сенсоры действий сценария");
        }

        Optional<Scenario> scenarioOpt = scenarioRepository.findByHubIdAndName(hubEventAvro.getHubId(), scenarioAddedEventAvro.getName());

        if (scenarioOpt.isEmpty()) {
            Scenario scenario = toScenario(hubEventAvro, scenarioAddedEventAvro);
            log.info("создали новый сценарий {} ", scenario.getName());
            scenarioRepository.save(scenario);
        } else {
            Scenario scenario = scenarioOpt.get();
            List<Long> oldConditionIds = scenario.getConditions().stream().map(Condition::getId).toList();
            List<Long> oldActionIds = scenario.getActions().stream().map(Action::getId).toList();

            List<Condition> conditions = new ArrayList<>(scenarioAddedEventAvro.getConditions().stream()
                    .map(conditionAvro -> toCondition(scenario, conditionAvro))
                    .toList());
            List<Action> actions = new ArrayList<>(scenarioAddedEventAvro.getActions().stream()
                    .map(actionAvro -> toAction(scenario, actionAvro))
                    .toList());
            scenario.setConditions(conditions);
            scenario.setActions(actions);
            log.info("обнавили сценарий {} ", scenario.getName());
            scenarioRepository.save(scenario);

            deleteUnusedConditions(oldConditionIds);
            deleteUnusedActions(oldActionIds);
        }
    }

    private ConditionType toConditionType(ConditionTypeAvro conditionTypeAvro) {
        return ConditionType.valueOf(conditionTypeAvro.name());
    }

    private ConditionOperation toConditionOperation(ConditionOperationAvro conditionOperationAvro) {
        return ConditionOperation.valueOf(conditionOperationAvro.name());
    }

    private ActionType toActionType(ActionTypeAvro actionTypeAvro) {
        return ActionType.valueOf(actionTypeAvro.name());
    }

    private Integer getConditionValue(Object conditionValue) {
        if (conditionValue == null) {
            return null;
        }
        if (conditionValue instanceof Boolean) {
            return ((Boolean) conditionValue ? 1 : 0);
        }
        if (conditionValue instanceof Integer) {
            return (Integer) conditionValue;
        }
        throw new ClassCastException("Ошибка преобразования значения");
    }

    private Scenario toScenario(HubEventAvro hubEventAvro, ScenarioAddedEventAvro scenarioAddedEventAvro) {
        Scenario scenario = new Scenario();
        scenario.setHubId(hubEventAvro.getHubId());
        scenario.setName(scenarioAddedEventAvro.getName());
        scenario.setConditions(scenarioAddedEventAvro.getConditions().stream()
                .map(conditionAvro -> toCondition(scenario, conditionAvro))
                .toList());
        scenario.setActions(scenarioAddedEventAvro.getActions().stream()
                .map(actionAvro -> toAction(scenario, actionAvro))
                .toList());

        return scenario;
    }

    private Condition toCondition(Scenario scenario, ScenarioConditionAvro conditionAvro) {
        return Condition.builder()
                .sensor(new Sensor(conditionAvro.getSensorId(), scenario.getHubId()))
                .type(toConditionType(conditionAvro.getType()))
                .operation(toConditionOperation(conditionAvro.getOperation()))
                .value(getConditionValue(conditionAvro.getValue()))
                .scenarios(List.of(scenario))
                .build();
    }

    private Action toAction(Scenario scenario, DeviceActionAvro deviceActionAvro) {
        return Action.builder()
                .sensor(new Sensor(deviceActionAvro.getSensorId(), scenario.getHubId()))
                .type(toActionType(deviceActionAvro.getType()))
                .value(deviceActionAvro.getValue())
                .build();
    }

    private Set<String> getConditionsSensorIds(Collection<ScenarioConditionAvro> conditionsAvro) {
        return conditionsAvro.stream()
                .map(ScenarioConditionAvro::getSensorId)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private Set<String> getActionsSensorIds(Collection<DeviceActionAvro> actionsAvro) {
        return actionsAvro.stream()
                .map(DeviceActionAvro::getSensorId)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private boolean checkSensors(Collection<String> ids, String hubId) {
        return sensorRepository.existsByIdInAndHubId(ids, hubId);
    }

    private void deleteUnusedConditions(Collection<Long> ids) {
        if (ids != null && !ids.isEmpty())
            conditionRepository.deleteAllByIdInBatch(ids);
    }

    private void deleteUnusedActions(Collection<Long> ids) {
        if (ids != null && !ids.isEmpty())
            actionRepository.deleteAllByIdInBatch(ids);

    }
}
