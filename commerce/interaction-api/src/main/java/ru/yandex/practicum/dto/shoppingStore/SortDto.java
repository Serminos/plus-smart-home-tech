package ru.yandex.practicum.dto.shoppingStore;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SortDto {
    private String direction;
    private String property;
}
