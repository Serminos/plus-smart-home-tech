package ru.yandex.practicum.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "sensors")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Sensor {
    @Id
    String id;
    @Column(name = "hub_id")
    String hubId;
}
