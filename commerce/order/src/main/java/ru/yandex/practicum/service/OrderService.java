package ru.yandex.practicum.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.exception.NoOrderFoundException;
import ru.yandex.practicum.exception.NotAuthorizedUserException;
import ru.yandex.practicum.feignClient.WarehouseFeignClient;
import ru.yandex.practicum.mapper.OrderMapper;
import ru.yandex.practicum.model.Order;
import ru.yandex.practicum.repository.OrderRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final WarehouseFeignClient warehouseClient;

    public OrderDto createOrder(String username, CreateNewOrderRequest newOrderRequest) {
        if (newOrderRequest == null) {
            throw new IllegalArgumentException("CreateNewOrderRequest не должен быть null");
        }
        if (username == null || username.isBlank()) {
            throw new NotAuthorizedUserException("Username не должен быть null или пустым");
        }
        BookedProductsDto bookedProductsDto = warehouseClient.checkProductQuantityInWarehouse(newOrderRequest.getShoppingCart());
        log.info("Проверка товара на складе: {}", bookedProductsDto);

        Order order = OrderMapper.mapToOrder(username, newOrderRequest, bookedProductsDto);
        order = orderRepository.save(order);
        log.info("Сохранили заказ в БД: {}", order);
        return OrderMapper.mapToOrderDto(order);
    }

    public List<OrderDto> getUserOrders(String username) {
        if (username == null || username.isBlank()) {
            throw new NotAuthorizedUserException("Имя пользователя не должно быть пустым");
        }
        List<Order> orders = orderRepository.findAllByUsername(username);
        if (orders.isEmpty()) {
            throw new NoOrderFoundException("Нет заказов у пользователя");
        }
        log.info("Заказы пользователя: {}", orders);

        return OrderMapper.mapToOrderDto(orders);
    }
}
