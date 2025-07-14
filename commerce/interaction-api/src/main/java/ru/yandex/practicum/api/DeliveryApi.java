package ru.yandex.practicum.api;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.dto.delivery.DeliveryDto;
import ru.yandex.practicum.dto.order.OrderDto;

import java.util.UUID;

public interface DeliveryApi {
    @PutMapping("/api/v1/delivery")
    DeliveryDto createDelivery(@Valid @RequestBody DeliveryDto deliveryDto);

    @PostMapping("/api/v1/delivery/cost")
    Double calculateDelivery(@Valid @RequestBody OrderDto orderDto);

    @PostMapping("/api/v1/delivery/successful")
    void setDeliverySuccessful(@RequestBody UUID deliveryId);

    @PostMapping("/api/v1/delivery/failed")
    void setDeliveryFailed(@RequestBody UUID deliveryId);

    @PostMapping("/api/v1/delivery/picked")
    void pickOrderForDelivery(@RequestBody UUID deliveryId);
}
