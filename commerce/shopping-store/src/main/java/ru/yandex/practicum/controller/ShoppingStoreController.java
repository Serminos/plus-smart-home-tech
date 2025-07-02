package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.api.shoppingStore.ShoppingStoreApi;
import ru.yandex.practicum.dto.shoppingStore.ProductCategory;
import ru.yandex.practicum.dto.shoppingStore.ProductDto;
import ru.yandex.practicum.dto.shoppingStore.SetProductQuantityStateRequest;
import ru.yandex.practicum.service.ShoppingStoreService;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ShoppingStoreController implements ShoppingStoreApi {
    private final ShoppingStoreService storeService;

    @Override
    public List<ProductDto> findAllByProductCategory(ProductCategory productCategory, Pageable pageable) {
        log.debug("Получение списка товаров по типу в пагинированном виде: {}, {}", productCategory, pageable);
        return storeService.findAllByProductCategory(productCategory, pageable);
    }

    @Override
    public ProductDto createProduct(ProductDto productDto) {
        log.debug("Создание нового товара в ассортименте: {}", productDto);
        return storeService.createProduct(productDto);
    }

    @Override
    public ProductDto updateProduct(ProductDto productDto) {
        log.debug("Обновление товара в ассортименте, например уточнение описания, характеристик и т.д.: {}", productDto);
        return storeService.updateProduct(productDto);
    }

    @Override
    public Boolean removeProductFromStore(UUID productId) {
        log.debug("Удалить товар из ассортимента магазина. Функция для менеджерского состава.: {}", productId);
        return storeService.removeProductFromStore(productId);
    }

    @Override
    public Boolean setProductQuantityState(SetProductQuantityStateRequest quantityStateRequest) {
        log.debug("Установка статуса по товару. API вызывается со стороны склада: {}", quantityStateRequest);
        return storeService.setProductQuantityState(quantityStateRequest);
    }

    @Override
    public ProductDto findProductById(String productId) {
        log.debug("Получить сведения по товару из БД: {}", productId);
        return storeService.findProductById(productId);
    }
}