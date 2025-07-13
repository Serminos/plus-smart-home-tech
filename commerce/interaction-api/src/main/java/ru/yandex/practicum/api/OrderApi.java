package ru.yandex.practicum.api;


import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.dto.order.OrderDto;

import java.util.List;

public interface OrderApi {
    @PutMapping("/api/v1/order")
    OrderDto createOrder(@RequestParam(name = "username") String username,
                         @Valid @RequestBody CreateNewOrderRequest newOrderRequest);

    @GetMapping("/api/v1/order")
    List<OrderDto> getUserOrders(@RequestParam(name = "username") String username);
}
