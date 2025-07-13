package ru.yandex.practicum.dto.warehouse;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString
public class NewProductInWarehouseRequest {
    @NotNull
    private UUID productId;
    @NotNull
    private Boolean fragile;
    @NotNull
    private DimensionDto dimension;
    @Min(1)
    private Double weight;
}