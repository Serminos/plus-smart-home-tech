package ru.yandex.practicum.dto.payment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString
public class PaymentDto {
    @NotNull
    private UUID paymentId;

    @NotNull
    @PositiveOrZero
    private Double totalPayment;

    @NotNull
    @PositiveOrZero
    private Double deliveryTotal;

    @NotNull
    @PositiveOrZero
    private Double feeTotal;
}