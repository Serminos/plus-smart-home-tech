package ru.yandex.practicum.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.delivery.DeliveryDto;
import ru.yandex.practicum.dto.delivery.DeliveryState;
import ru.yandex.practicum.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.order.OrderState;
import ru.yandex.practicum.dto.order.ProductReturnRequest;
import ru.yandex.practicum.dto.payment.PaymentDto;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.dto.warehouse.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.exception.NoOrderFoundException;
import ru.yandex.practicum.exception.NotAssembledOrderException;
import ru.yandex.practicum.exception.NotAuthorizedUserException;
import ru.yandex.practicum.exception.NotEnoughInfoInOrderToCalculateException;
import ru.yandex.practicum.feignClient.DeliveryFeign;
import ru.yandex.practicum.feignClient.PaymentFeign;
import ru.yandex.practicum.feignClient.WarehouseFeignClient;
import ru.yandex.practicum.mapper.OrderMapper;
import ru.yandex.practicum.model.Order;
import ru.yandex.practicum.repository.OrderRepository;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final WarehouseFeignClient warehouseClient;
    private final PaymentFeign paymentFeign;
    private final DeliveryFeign deliveryFeign;


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

        Double productCost = paymentFeign.calculateProductCost(OrderMapper.mapToOrderDto(order));
        log.info("Стоимость товаров в заказе: {}", productCost);
        order.setProductPrice(productCost);

        DeliveryDto deliveryDto = createDeliveryOrder(order.getOrderId(), newOrderRequest.getDeliveryAddress());
        order.setDeliveryId(deliveryDto.getDeliveryId());

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

    public OrderDto payOrder(UUID orderId) {
        Order order = findOrderById(orderId);
        order.setState(OrderState.PAID);
        order = orderRepository.save(order);
        log.info("Обновляем статус заказа в БД: {}", order);
        return OrderMapper.mapToOrderDto(order);
    }
    public OrderDto calculateDelivery(UUID orderId) {
        Order order = findOrderById(orderId);
        Double cost = deliveryFeign.calculateDelivery(OrderMapper.mapToOrderDto(order));
        order.setDeliveryPrice(cost);
        order = orderRepository.save(order);
        log.info("Обновляем заказ в БД: {}", order);
        return OrderMapper.mapToOrderDto(order);
    }

    public OrderDto deliveryOrder(UUID orderId) {
        Order order = findOrderById(orderId);
        order.setState(OrderState.DELIVERED);
        order = orderRepository.save(order);
        log.info("Обновляем заказ в БД: {}", order);
        return OrderMapper.mapToOrderDto(order);
    }

    public OrderDto deliveryOrderFailed(UUID orderId) {
        Order order = findOrderById(orderId);
        order.setState(OrderState.DELIVERY_FAILED);
        order = orderRepository.save(order);
        log.info("Обновляем статус заказа в БД: {}", order);
        return OrderMapper.mapToOrderDto(order);
    }

    public OrderDto completedOrder(UUID orderId) {
        Order order = findOrderById(orderId);
        order.setState(OrderState.COMPLETED);
        order = orderRepository.save(order);
        log.info("Обновляем статус заказа в БД: {}", order);
        return OrderMapper.mapToOrderDto(order);
    }


    public OrderDto returnOrder(ProductReturnRequest returnRequest) {
        if (returnRequest.getProducts().isEmpty()) {
            throw new IllegalArgumentException("Список возвращаемых продуктов не должен быть пустым");
        }
        Order order = findOrderById(returnRequest.getOrderId());
        order.setState(OrderState.PRODUCT_RETURNED);
        order = orderRepository.save(order);
        log.info("Обновляем статус заказа в БД: {}", order);

        warehouseClient.returnProductsToTheWarehouse(returnRequest.getProducts());
        log.info("Возвращаем продукты на склад");
        return OrderMapper.mapToOrderDto(order);
    }

    public OrderDto setPaymentFailed(UUID orderId) {
        Order order = findOrderById(orderId);
        order.setState(OrderState.PAYMENT_FAILED);
        order = orderRepository.save(order);
        log.info("Обновляем статус заказа в БД: {}", order);
        return OrderMapper.mapToOrderDto(order);
    }

    public OrderDto calculateTotalCost(UUID orderId) {
        Order order = findOrderById(orderId);
        if (order.getDeliveryPrice() == null || order.getProductPrice() == null) {
            throw new NotEnoughInfoInOrderToCalculateException("Недостаточно данных для рассчета полной стоимости заказа");
        }
        Double totalCost = paymentFeign.calculateTotalCost(OrderMapper.mapToOrderDto(order));
        log.info("Полная стоимость заказа: {}", totalCost);
        order.setTotalPrice(totalCost);
        order = orderRepository.save(order);
        log.info("Обновляем заказ в БД: {}", order);
        return OrderMapper.mapToOrderDto(order);
    }

    public OrderDto assembleOrder(UUID orderId) {
        Order order = findOrderById(orderId);
        AssemblyProductsForOrderRequest assemblyRequest = new AssemblyProductsForOrderRequest(orderId, order.getProducts());
        log.info("Запрос на сборку для склада: {}", assemblyRequest);
        BookedProductsDto bookedDto = warehouseClient.assemblingProductsForTheOrder(assemblyRequest);
        log.info("Ответ склада по сборке: {}", bookedDto);
        order.setState(OrderState.ASSEMBLED);
        order = orderRepository.save(order);
        log.info("Обновляем заказ в БД: {}", order);
        return OrderMapper.mapToOrderDto(order);
    }

    public OrderDto assembleOrderFailed(UUID orderId) {
        Order order = findOrderById(orderId);
        order.setState(OrderState.ASSEMBLY_FAILED);
        order = orderRepository.save(order);
        log.info("Обновляем заказ в БД: {}", order);
        order = createPaymentOrder(order);
        return OrderMapper.mapToOrderDto(order);
    }

    private Order createPaymentOrder(Order order) {
        if (!(order.getState().equals(OrderState.ASSEMBLED))) {
            throw new NotAssembledOrderException("Заказа еще не собран на складе");
        }
        if (order.getTotalPrice() == null || order.getDeliveryPrice() == null || order.getProductPrice() == null) {
            throw new NotEnoughInfoInOrderToCalculateException("Недостаточно данных для оплаты заказа");
        }
        PaymentDto paymentDto = paymentFeign.createPaymentOrder(OrderMapper.mapToOrderDto(order));
        log.info("Создали платеж в платежном сервисе: {}", paymentDto);
        order.setState(OrderState.ON_PAYMENT);
        order.setPaymentId(paymentDto.getPaymentId());
        order = orderRepository.save(order);
        log.info("Обновляем статус заказа в БД: {}", order);
        return order;
    }

    private DeliveryDto createDeliveryOrder(UUID orderId, AddressDto toAddressDelivery) {
        DeliveryDto dto = new DeliveryDto();
        AddressDto fromAddressDelivery = warehouseClient.getAddressWarehouse();
        dto.setFromAddress(fromAddressDelivery);
        dto.setToAddress(toAddressDelivery);
        dto.setOrderId(orderId);
        dto.setDeliveryState(DeliveryState.CREATED);
        dto = deliveryFeign.createDelivery(dto);
        log.info("Создаем доставку заказа в сервисе заказа: {}", dto);
        return dto;
    }

    private Order findOrderById(UUID orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId не должен быть null");
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException("Не найден заказ с id: " + orderId));
        log.info("Находим нужный заказ: {}", order);
        return order;
    }
}
