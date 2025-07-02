package ru.yandex.practicum.dto.shoppingCart;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@ToString
public class ShoppingCartDto {
    @NotBlank
    private String shoppingCartId;
    @NotNull
    private Map<UUID, Integer> products;
}
