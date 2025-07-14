package ru.yandex.practicum.api;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.payment.PaymentDto;

import java.util.UUID;

public interface PaymentApi {
    @PostMapping("/api/v1/payment")
    PaymentDto createPaymentOrder(@Valid @RequestBody OrderDto orderDto);

    @PostMapping("/api/v1/payment/productCost")
    Double calculateProductCost(@Valid @RequestBody OrderDto orderDto);

    @PostMapping("/api/v1/payment/totalCost")
    Double calculateTotalCost(@Valid @RequestBody OrderDto orderDto);

    @PostMapping("/api/v1/payment/failed")
    void setPaymentFailed(@RequestBody UUID paymentId);

    @PostMapping("/api/v1/payment/refund")
    void payOrder(@RequestBody UUID paymentId);
}
