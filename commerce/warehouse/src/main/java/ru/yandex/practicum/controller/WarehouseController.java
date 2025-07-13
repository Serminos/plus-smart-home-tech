package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.api.WarehouseApi;
import ru.yandex.practicum.dto.shoppingCart.ShoppingCartDto;
import ru.yandex.practicum.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.dto.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.service.WarehouseService;

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
}
