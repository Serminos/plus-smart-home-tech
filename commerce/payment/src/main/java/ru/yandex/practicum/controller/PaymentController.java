package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.api.PaymentApi;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.payment.PaymentDto;
import ru.yandex.practicum.service.PaymentService;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PaymentController implements PaymentApi {
    private final PaymentService paymentService;

    @Override
    public PaymentDto createPaymentOrder(OrderDto orderDto) {
        log.info("Запрос на создание оплаты заказа: {}", orderDto);
        return paymentService.createPaymentOrder(orderDto);
    }

    @Override
    public Double calculateProductCost(OrderDto orderDto) {
        log.info("Запрос на рассчет стоимости продуктов: {}", orderDto);
        return paymentService.calculateProductCost(orderDto);
    }

    @Override
    public Double calculateTotalCost(OrderDto orderDto) {
        log.info("Запрос на рассчет полной стоимости заказа: {}", orderDto);
        return paymentService.calculateTotalCost(orderDto);
    }

    @Override
    public void setPaymentFailed(UUID paymentId) {
        log.info("Запрос при неудачной оплате заказа: {}", paymentId);
        paymentService.setPaymentFailed(paymentId);
    }
}
