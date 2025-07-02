package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.shoppingStore.ProductCategory;
import ru.yandex.practicum.dto.shoppingStore.ProductDto;
import ru.yandex.practicum.dto.shoppingStore.ProductState;
import ru.yandex.practicum.dto.shoppingStore.SetProductQuantityStateRequest;
import ru.yandex.practicum.exception.ProductNotFoundException;
import ru.yandex.practicum.mapper.ShoppingStoreMapper;
import ru.yandex.practicum.model.Product;
import ru.yandex.practicum.repository.ShoppingStoreRepository;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShoppingStoreServiceImp implements ShoppingStoreService {
    private final ShoppingStoreRepository storeRepository;

    @Override
    public ProductDto createProduct(ProductDto productDto) {
        if (productDto.getProductId() != null) {
            throw new IllegalArgumentException("При создании нового товара, поле productId должно быть null");
        }
        Product product = ShoppingStoreMapper.mapToProduct(productDto);
        product = storeRepository.save(product);
        log.debug("Сохранен новый товар в БД: {}", product);
        return ShoppingStoreMapper.mapToProductDto(product);
    }

    @Override
    public ProductDto findProductById(String productId) {
        UUID id = UUID.fromString(productId);
        Product product = storeRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Товар не найден: " + id));
        return ShoppingStoreMapper.mapToProductDto(product);
    }

    @Override
    public List<ProductDto> findAllByProductCategory(ProductCategory productCategory, Pageable pageable) {
        List<Product> products = storeRepository.findAllByProductCategory(productCategory, pageable);
        if (products.isEmpty()) {
            throw new ProductNotFoundException("Не найден товары с категорией: " + productCategory);
        }
        return ShoppingStoreMapper.mapToProductDto(products);
    }

    @Override
    public ProductDto updateProduct(ProductDto productDto) {
        if (productDto.getProductId() == null || productDto.getProductId().isBlank()) {
            throw new IllegalArgumentException("При обновлении товара, поле productId не должно быть null или пустым");
        }
        UUID productId = UUID.fromString(productDto.getProductId());
        Product productOld = storeRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Товар не найден: " + productId));
        log.info("Старый товар: {}", productOld);

        Product productNew = ShoppingStoreMapper.mapToProduct(productDto);
        productNew.setProductId(productId);

        Product result = storeRepository.save(productNew);
        log.info("Новый товар: {}", result);
        return ShoppingStoreMapper.mapToProductDto(result);
    }

    @Override
    public Boolean removeProductFromStore(UUID productId) {
        Product product = storeRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Товар не найден: " + productId));
        log.info("Старый товар: {}", product);

        if (!product.getProductState().equals(ProductState.DEACTIVATE)) {
            product.setProductState(ProductState.DEACTIVATE);
            product = storeRepository.save(product);
            log.info("Удаленный товар: {}", product);
        }
        return product.getProductState().equals(ProductState.DEACTIVATE);
    }

    @Override
    public Boolean setProductQuantityState(SetProductQuantityStateRequest quantityStateRequest) {
        Product product = storeRepository.findById(quantityStateRequest.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Товар не найден: " +
                        quantityStateRequest.getProductId()));
        log.info("Старый товар: {}", product);

        if (!product.getQuantityState().equals(quantityStateRequest.getQuantityState())) {
            product.setQuantityState(quantityStateRequest.getQuantityState());
            product = storeRepository.save(product);
            log.info("Товар с обновленным QuantityState: {}", product);
        }
        return product.getQuantityState().equals(quantityStateRequest.getQuantityState());
    }
}