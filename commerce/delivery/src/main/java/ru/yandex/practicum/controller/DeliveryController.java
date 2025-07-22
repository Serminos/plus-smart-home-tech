package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.api.DeliveryApi;
import ru.yandex.practicum.dto.delivery.DeliveryDto;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.service.DeliveryService;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DeliveryController implements DeliveryApi {
    private final DeliveryService deliveryService;

    @Override
    public DeliveryDto createDelivery(DeliveryDto deliveryDto) {
        log.info("Запрос на создание доставки: {}", deliveryDto);
        return deliveryService.createDelivery(deliveryDto);
    }

    @Override
    public Double calculateDelivery(OrderDto orderDto) {
        log.info("Запрос на расчет стоимости доставки: {}", orderDto);
        return deliveryService.calculateDelivery(orderDto);
    }

    @Override
    public void setDeliverySuccessful(UUID deliveryId) {
        log.info("Запрос при удачной доставки: {}", deliveryId);
        deliveryService.setDeliverySuccessful(deliveryId);
    }

    @Override
    public void setDeliveryFailed(UUID deliveryId) {
        log.info("Запрос при неудачной доставки: {}", deliveryId);
        deliveryService.setDeliveryFailed(deliveryId);
    }

    @Override
    public void pickOrderForDelivery(UUID deliveryId) {
        log.info("Запрос при передачи заказа в доставку: {}", deliveryId);
        deliveryService.pickOrderForDelivery(deliveryId);
    }
}
