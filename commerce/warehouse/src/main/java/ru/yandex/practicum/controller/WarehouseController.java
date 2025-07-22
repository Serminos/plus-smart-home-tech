package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.api.WarehouseApi;
import ru.yandex.practicum.dto.shoppingCart.ShoppingCartDto;
import ru.yandex.practicum.dto.warehouse.*;
import ru.yandex.practicum.service.WarehouseService;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class WarehouseController implements WarehouseApi {
    private final WarehouseService warehouseService;

    @Override
    public void addNewProductToWarehouse(NewProductInWarehouseRequest newProductInWarehouseRequest) {
        log.info("Добавить новый товар на склад: {}", newProductInWarehouseRequest);
        warehouseService.addNewProductToWarehouse(newProductInWarehouseRequest);
    }

    @Override
    public BookedProductsDto checkProductQuantityInWarehouse(ShoppingCartDto shoppingCartDto) {
        log.info("Предварительно проверить что количество " +
                "товаров на складе достаточно для данной корзиный продуктов: {}", shoppingCartDto);
        return warehouseService.checkProductQuantityInWarehouse(shoppingCartDto);
    }

    @Override
    public void addProductInWarehouse(AddProductToWarehouseRequest addProductToWarehouseRequest) {
        log.info("Принять товар на склад: {}", addProductToWarehouseRequest);
        warehouseService.addProductInWarehouse(addProductToWarehouseRequest);
    }

    @Override
    public AddressDto getAddressWarehouse() {
        log.info("Предоставить адрес склада для расчёта доставки");
        return warehouseService.getAddressWarehouse();
    }

    @Override
    public BookedProductsDto assemblingProductsForTheOrder(AssemblyProductsForOrderRequest assemblyRequest) {
        log.info("DTO на сборку продуктов для заказа: {}", assemblyRequest);
        return warehouseService.assemblingProductsForTheOrder(assemblyRequest);
    }

    @Override
    public void returnProductsToTheWarehouse(Map<UUID, Integer> returnedProducts) {
        log.info("Запрос на возврат товаров: {}", returnedProducts);
        warehouseService.returnProductsToTheWarehouse(returnedProducts);
    }

    @Override
    public void shippedProductsToTheWarehouse(ShippedToDeliveryRequest deliveryRequest){
        log.info("Запрос на передачу заказа в доставку: {}", deliveryRequest);
        warehouseService.shippedProductsToTheWarehouse(deliveryRequest);
    }
}

