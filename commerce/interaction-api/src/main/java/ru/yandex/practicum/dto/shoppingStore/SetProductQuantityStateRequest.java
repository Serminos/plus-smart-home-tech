package ru.yandex.practicum.dto.shoppingStore;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class SetProductQuantityStateRequest {
    @NotNull
    private UUID productId;
    @NotNull
    private QuantityState quantityState;
}
