package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "scenarios")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Scenario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name = "hub_id")
    String hubId;
    String name;
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(
        name = "scenario_conditions",
        joinColumns = @JoinColumn(name = "scenario_id"),
        inverseJoinColumns = @JoinColumn(name = "condition_id")
    )
    List<Condition> conditions;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(
        name = "scenario_actions",
        joinColumns = @JoinColumn(name = "scenario_id"),
        inverseJoinColumns = @JoinColumn(name = "action_id")
    )
    List<Action> actions;
}
