package ru.yandex.practicum.api;


import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.order.ProductReturnRequest;

import java.util.List;
import java.util.UUID;

public interface OrderApi {
    @PutMapping("/api/v1/order")
    OrderDto createOrder(@RequestParam(name = "username") String username,
                         @Valid @RequestBody CreateNewOrderRequest newOrderRequest);

    @GetMapping("/api/v1/order")
    List<OrderDto> getUserOrders(@RequestParam(name = "username") String username);

    @PostMapping("/api/v1/order/payment")
    OrderDto payOrder(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/return")
    OrderDto returnOrder(@Valid @RequestBody ProductReturnRequest returnRequest);

    @PostMapping("/api/v1/order/payment/failed")
    OrderDto setPaymentFailed(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/calculate/total")
    OrderDto calculateTotalCost(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/assembly")
    OrderDto assembleOrder(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/assembly/failed")
    OrderDto assembleOrderFailed(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/calculate/delivery")
    OrderDto calculateDelivery(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/delivery")
    OrderDto deliveryOrder(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/delivery/failed")
    OrderDto deliveryOrderFailed(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/completed")
    OrderDto completedOrder(@RequestBody UUID orderId);
}
