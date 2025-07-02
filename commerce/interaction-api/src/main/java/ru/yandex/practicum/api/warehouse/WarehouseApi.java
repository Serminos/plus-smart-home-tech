package ru.yandex.practicum.api.warehouse;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.dto.shoppingCart.ShoppingCartDto;
import ru.yandex.practicum.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.dto.warehouse.NewProductInWarehouseRequest;

public interface WarehouseApi {

    @PutMapping("/api/v1/warehouse")
    void addNewProductToWarehouse(@Valid @RequestBody NewProductInWarehouseRequest newProductInWarehouseRequest);

    @PostMapping("/api/v1/warehouse/check")
    BookedProductsDto checkProductQuantityInWarehouse(@Valid @RequestBody ShoppingCartDto shoppingCartDto);

    @PostMapping("/api/v1/warehouse/add")
    void addProductInWarehouse(@Valid @RequestBody AddProductToWarehouseRequest addProductToWarehouseRequest);

    @GetMapping("/api/v1/warehouse/address")
    AddressDto getAddressWarehouse();
}
