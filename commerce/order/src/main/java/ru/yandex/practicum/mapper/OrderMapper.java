package ru.yandex.practicum.mapper;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.order.OrderState;
import ru.yandex.practicum.dto.shoppingCart.ShoppingCartDto;
import ru.yandex.practicum.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.model.Order;

import java.util.List;
import java.util.UUID;

@Slf4j
public class OrderMapper {
    public static Order mapToOrder(
            String username,
            CreateNewOrderRequest newOrderRequest,
            BookedProductsDto bookedProducts) {
        ShoppingCartDto shoppingCart = newOrderRequest.getShoppingCart();
        Order order = new Order();
        order.setUsername(username);
        order.setShoppingCartId(UUID.fromString(shoppingCart.getShoppingCartId()));
        order.setProducts(shoppingCart.getProducts());
        order.setState(OrderState.NEW);
        order.setDeliveryWeight(bookedProducts.getDeliveryWeight());
        order.setDeliveryVolume(bookedProducts.getDeliveryVolume());
        order.setFragile(bookedProducts.getFragile());
        log.info("Результат маппинга: {}", order);
        return order;
    }

    public static OrderDto mapToOrderDto(Order order) {
        OrderDto orderDto = new OrderDto();
        orderDto.setOrderId(order.getOrderId());
        orderDto.setUsername(order.getUsername());
        orderDto.setShoppingCartId(order.getShoppingCartId());
        orderDto.setProducts(order.getProducts());
        orderDto.setPaymentId(order.getPaymentId());
        orderDto.setDeliveryId(order.getDeliveryId());
        orderDto.setState(order.getState());
        orderDto.setDeliveryWeight(order.getDeliveryWeight());
        orderDto.setDeliveryVolume(order.getDeliveryVolume());
        orderDto.setFragile(order.getFragile());
        orderDto.setTotalPrice(order.getTotalPrice());
        orderDto.setDeliveryPrice(order.getDeliveryPrice());
        orderDto.setProductPrice(order.getProductPrice());
        log.info("Результат маппинга: {}", orderDto);
        return orderDto;
    }

    public static List<OrderDto> mapToOrderDto(List<Order> orders) {
        return orders.stream().map(OrderMapper::mapToOrderDto).toList();
    }
}
