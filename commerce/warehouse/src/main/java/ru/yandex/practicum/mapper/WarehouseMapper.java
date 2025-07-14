package ru.yandex.practicum.mapper;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.dto.warehouse.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.dto.warehouse.DimensionDto;
import ru.yandex.practicum.dto.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.model.OrderBooking;
import ru.yandex.practicum.model.WarehouseProduct;

@Slf4j
public class WarehouseMapper {
    public static WarehouseProduct mapToWarehouseProduct(NewProductInWarehouseRequest newProductRequest) {
        WarehouseProduct product = new WarehouseProduct();
        DimensionDto dimension = newProductRequest.getDimension();

        product.setProductId(newProductRequest.getProductId());
        product.setFragile(newProductRequest.getFragile());
        product.setWidth(dimension.getWidth());
        product.setHeight(dimension.getHeight());
        product.setDepth(dimension.getDepth());
        product.setWeight(newProductRequest.getWeight());
        product.setQuantity(0);
        log.info("Результат маппинга в WarehouseProduct: {}", product);
        return product;
    }

    public static OrderBooking mapToOrderBooking(AssemblyProductsForOrderRequest assemblyRequest, BookedProductsDto bookedDto) {
        OrderBooking orderBooking = new OrderBooking();
        orderBooking.setOrderId(assemblyRequest.getOrderId());
        orderBooking.setProducts(assemblyRequest.getProducts());
        orderBooking.setDeliveryWeight(bookedDto.getDeliveryWeight());
        orderBooking.setDeliveryVolume(bookedDto.getDeliveryVolume());
        orderBooking.setFragile(bookedDto.getFragile());
        log.info("Результат маппинга в OrderBooking: {}", orderBooking);
        return orderBooking;
    }
}
