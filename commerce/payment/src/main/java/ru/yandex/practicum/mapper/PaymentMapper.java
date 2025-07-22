package ru.yandex.practicum.mapper;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.payment.PaymentDto;
import ru.yandex.practicum.model.PaymentEntity;

@Slf4j
public class PaymentMapper {
    public static PaymentEntity mapToPaymentEntity(OrderDto orderDto) {
        PaymentEntity entity = new PaymentEntity();
        entity.setOrderId(orderDto.getOrderId());
        entity.setTotalPrice(orderDto.getTotalPrice());
        entity.setDeliveryPrice(orderDto.getDeliveryPrice());
        entity.setProductPrice(orderDto.getProductPrice());
        log.info("Результат маппинга в PaymentEntity: {}", entity);
        return entity;
    }

    public static PaymentDto mapToPaymentDto(PaymentEntity entity) {
        PaymentDto dto = new PaymentDto();
        dto.setPaymentId(entity.getPaymentId());
        dto.setTotalPayment(entity.getTotalPrice());
        dto.setDeliveryTotal(entity.getDeliveryPrice());
        dto.setFeeTotal(entity.getTotalPrice() - entity.getDeliveryPrice() - entity.getProductPrice());
        log.info("Результат маппинга в PaymentDto: {}", dto);
        return dto;
    }
}
