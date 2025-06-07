package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.model.Enum.ConditionOperation;
import ru.yandex.practicum.model.Enum.ConditionType;

import java.util.List;

@Entity
@Table(name = "conditions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Condition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @ManyToOne
    @JoinColumn(name = "sensor_id")
    Sensor sensor;
    @Enumerated(EnumType.STRING)
    ConditionType type;
    @Enumerated(EnumType.STRING)
    ConditionOperation operation;
    Integer value;
    @ManyToMany(mappedBy = "conditions")
    List<Scenario> scenarios;
}
