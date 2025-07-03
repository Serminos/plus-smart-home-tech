package ru.yandex.practicum.dto.shoppingStore;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class ProductListResponse {
    private List<ProductDto> content;
    private List<SortDto> sort;
}
