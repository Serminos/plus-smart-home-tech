package ru.yandex.practicum.dto.shoppingCart;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString
public class ChangeProductQuantityRequest {
    @NotNull
    private UUID productId;
    @NotNull
    private Integer newQuantity;
}
