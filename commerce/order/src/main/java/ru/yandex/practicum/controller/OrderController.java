package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.api.OrderApi;
import ru.yandex.practicum.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.service.OrderService;

import java.util.List;

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
}
