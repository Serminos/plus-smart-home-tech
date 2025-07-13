package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.api.OrderApi;
import ru.yandex.practicum.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.order.ProductReturnRequest;
import ru.yandex.practicum.service.OrderService;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class OrderController implements OrderApi {
    private final OrderService orderService;

    @Override
    public OrderDto createOrder(String username, CreateNewOrderRequest newOrderRequest) {
        log.info("Пользователь: {} новый заказ: {}", username, newOrderRequest);
        return orderService.createOrder(username, newOrderRequest);
    }

    @Override
    public List<OrderDto> getUserOrders(String username) {
        log.info("Получить заказы пользователя: {}", username);
        return orderService.getUserOrders(username);
    }

    @Override
    public OrderDto payOrder(UUID orderId) {
        log.info("Запрос на оплату заказа: {}", orderId);
        return orderService.payOrder(orderId);
    }

    @Override
    public OrderDto returnOrder(ProductReturnRequest returnRequest) {
        log.info("Запрос на возврат заказа: {}", returnRequest);
        return orderService.returnOrder(returnRequest);
    }

    @Override
    public OrderDto setPaymentFailed(UUID orderId) {
        log.info("Запрос при неудачной оплате заказа: {}", orderId);
        return orderService.setPaymentFailed(orderId);
    }

    @Override
    public OrderDto calculateTotalCost(UUID orderId) {
        log.info("Запрос на рассчет полной стоимости заказа: {}", orderId);
        return orderService.calculateTotalCost(orderId);
    }

    @Override
    public OrderDto assembleOrder(UUID orderId) {
        log.info("Запрос на сбор заказа на складе: {}", orderId);
        return orderService.assembleOrder(orderId);
    }

    @Override
    public OrderDto assembleOrderFailed(UUID orderId) {
        log.info("Запрос при неудачной сборке заказа на складе: {}", orderId);
        return orderService.assembleOrderFailed(orderId);
    }
}
