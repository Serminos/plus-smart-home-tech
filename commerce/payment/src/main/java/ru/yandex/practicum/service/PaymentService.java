package ru.yandex.practicum.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.payment.PaymentDto;
import ru.yandex.practicum.dto.payment.PaymentState;
import ru.yandex.practicum.dto.shoppingStore.ProductDto;
import ru.yandex.practicum.exception.NoOrderFoundException;
import ru.yandex.practicum.exception.NotEnoughInfoInOrderToCalculateException;
import ru.yandex.practicum.feignClient.OrderFeign;
import ru.yandex.practicum.feignClient.ShoppingStoreFeign;
import ru.yandex.practicum.mapper.PaymentMapper;
import ru.yandex.practicum.model.PaymentEntity;
import ru.yandex.practicum.repository.PaymentRepository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderFeign orderFeign;
    private final ShoppingStoreFeign storeFeign;

    public PaymentDto createPaymentOrder(OrderDto orderDto) {
        if (orderDto.getTotalPrice() == null || orderDto.getDeliveryPrice() == null || orderDto.getProductPrice() == null) {
            throw new NotEnoughInfoInOrderToCalculateException("Недостаточно данных для оплаты заказа");
        }
        PaymentEntity paymentEntity = PaymentMapper.mapToPaymentEntity(orderDto);
        Optional<PaymentEntity> oldPaymentEntityOpt = paymentRepository.findByOrderId(orderDto.getOrderId());
        if (oldPaymentEntityOpt.isPresent()) {
            log.info("Старая сущность PaymentEntity: {}", oldPaymentEntityOpt.get());
            paymentEntity.setPaymentId(oldPaymentEntityOpt.get().getPaymentId());
        }

        paymentEntity.setPaymentState(PaymentState.PENDING);
        paymentEntity = paymentRepository.save(paymentEntity);
        log.info("Сохранили платеж в БД: {}", paymentEntity);

        return PaymentMapper.mapToPaymentDto(paymentEntity);
    }

    public Double calculateProductCost(OrderDto orderDto) {
        Map<UUID, Integer> products = orderDto.getProducts();
        if (products == null || products.isEmpty()) {
            throw new IllegalArgumentException("Список товаров не должен быть null или пустым");
        }
        Double productCost = 0.0;
        for (UUID productId : products.keySet()) {
            ProductDto product;
            try {
                product = storeFeign.findProductById(productId);
                log.info("Находим товар в магазине: {}", product);
            } catch (FeignException e) {
                throw new NotEnoughInfoInOrderToCalculateException(e.getMessage());
            }
            productCost += product.getPrice() * products.get(productId);
        }
        log.info("Стоимость товаров в заказе: {}", productCost);
        return productCost;
    }

    public Double calculateTotalCost(OrderDto orderDto) {
        if (orderDto.getProductPrice() == null || orderDto.getDeliveryPrice() == null) {
            throw new NotEnoughInfoInOrderToCalculateException("Недостаточно данных для расчета полной стоимости заказа");
        }
        Double totalCost = orderDto.getProductPrice() * 1.1 + orderDto.getDeliveryPrice();
        log.info("Итоговая стоимость заказа: {}", totalCost);
        return totalCost;
    }

    public void setPaymentFailed(UUID paymentId) {
        if (paymentId == null) {
            throw new IllegalArgumentException("Id платежа не может быть null");
        }
        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NoOrderFoundException("Не найден платеж с Id: " + paymentId));
        log.info("Находим нужный платеж: {}", payment);
        payment.setPaymentState(PaymentState.FAILED);

        try {
            OrderDto dto = orderFeign.setPaymentFailed(payment.getOrderId());
            log.info("Обновляем статус заказа: {}", dto);
        } catch (FeignException e) {
            throw new NoOrderFoundException(e.getMessage());
        }
        payment = paymentRepository.save(payment);
        log.info("Обновляем статус платежа: {}", payment);
    }
}
