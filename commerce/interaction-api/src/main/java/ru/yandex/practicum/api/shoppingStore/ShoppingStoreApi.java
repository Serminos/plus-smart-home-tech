package ru.yandex.practicum.api.shoppingStore;

import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.shoppingStore.ProductCategory;
import ru.yandex.practicum.dto.shoppingStore.ProductDto;
import ru.yandex.practicum.dto.shoppingStore.SetProductQuantityStateRequest;

import java.util.List;
import java.util.UUID;

public interface ShoppingStoreApi {

    @GetMapping("/api/v1/shopping-store")
    List<ProductDto> findAllByProductCategory(
            @RequestParam(name = "category") ProductCategory productCategory,
            Pageable pageable);

    @PutMapping("/api/v1/shopping-store")
    ProductDto createProduct(@Valid @RequestBody ProductDto productDto);

    @GetMapping("/api/v1/shopping-store/{productId}")
    ProductDto findProductById(@PathVariable("productId") String productId);


    @PostMapping("/api/v1/shopping-store")
    ProductDto updateProduct(@Valid @RequestBody ProductDto productDto);

    @PostMapping("/api/v1/shopping-store/removeProductFromStore")
    Boolean removeProductFromStore(@RequestBody UUID productId);

    @PostMapping("/api/v1/shopping-store/quantityState")
    Boolean setProductQuantityState(@Valid @RequestBody SetProductQuantityStateRequest quantityStateRequest);
}
