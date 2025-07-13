package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.shoppingCart.ShoppingCartDto;
import ru.yandex.practicum.dto.shoppingStore.QuantityState;
import ru.yandex.practicum.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.dto.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.exception.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.exception.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.exception.SpecifiedProductAlreadyInWarehouseException;
import ru.yandex.practicum.feignClient.ShoppingStoreFeignClient;
import ru.yandex.practicum.mapper.WarehouseMapper;
import ru.yandex.practicum.model.WarehouseProduct;
import ru.yandex.practicum.repository.WarehouseRepository;

import java.security.SecureRandom;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseServiceImp implements WarehouseService {
    private final WarehouseRepository warehouseRepository;
    private final ShoppingStoreFeignClient storeFeignClient;
    private AddressDto warehouseAddress = settingAddress();

    @Override
    public void addNewProductToWarehouse(NewProductInWarehouseRequest newProductInWarehouseRequest) {
        UUID productId = newProductInWarehouseRequest.getProductId();
        if (warehouseRepository.findById(productId).isPresent()) {
            throw new SpecifiedProductAlreadyInWarehouseException("Товар уже есть на складе: " + productId);
        }
        WarehouseProduct newProduct = WarehouseMapper.mapToWarehouseProduct(newProductInWarehouseRequest);
        warehouseRepository.save(newProduct);
    }

    @Override
    public BookedProductsDto checkProductQuantityInWarehouse(ShoppingCartDto shoppingCartDto) {
        Map<UUID, Integer> productsInCart = shoppingCartDto.getProducts();
        List<WarehouseProduct> warehouseProductsList = warehouseRepository.findAllById(productsInCart.keySet());
        log.info("Товары из корзины имеющиеся на складе: {}", warehouseProductsList);
        Map<UUID, WarehouseProduct> warehouseProductsMap = warehouseProductsList.stream()
                .collect(Collectors.toMap(WarehouseProduct::getProductId, Function.identity()));
        log.info("Создаем Map из товаров имеющиеся на складе: {}", warehouseProductsMap);
        // проверка наличия продуктов на складе
        checkAvailabilityProductsInWarehouse(productsInCart.keySet(), warehouseProductsMap.keySet());
        // проверка количества продуктов на складе
        checkQuantity(productsInCart, warehouseProductsMap);

        return bookingProducts(warehouseProductsList);
    }

    @Override
    public void addProductInWarehouse(AddProductToWarehouseRequest addProductRequest) {
        UUID productId = addProductRequest.getProductId();
        WarehouseProduct product = warehouseRepository.findById(productId).orElseThrow(
                () -> new NoSpecifiedProductInWarehouseException("Товара нет на складе: " + productId));
        product.setQuantity(product.getQuantity() + addProductRequest.getQuantity());
        product = warehouseRepository.save(product);
        log.info("Обновленный товар: {}", product);
        setProductQuantityState(product);
    }

    @Override
    public AddressDto getAddressWarehouse() {
        return warehouseAddress;
    }

    private AddressDto settingAddress() {
        String[] addresses = new String[]{"ADDRESS_1", "ADDRESS_2"};
        String currentAddresses = addresses[Random.from(new SecureRandom()).nextInt(0, 1)];

        AddressDto addressDto = new AddressDto();
        addressDto.setCountry(currentAddresses);
        addressDto.setCity(currentAddresses);
        addressDto.setStreet(currentAddresses);
        addressDto.setHouse(currentAddresses);
        addressDto.setFlat(currentAddresses);
        return addressDto;
    }

    private void checkAvailabilityProductsInWarehouse(Set<UUID> productsInCart, Set<UUID> productsInWarehouse) {
        productsInCart.removeAll(productsInWarehouse);
        log.info("Товары которых нет на складе: {}", productsInCart);
        if (!productsInCart.isEmpty()) {
            throw new NoSpecifiedProductInWarehouseException("На складе нет товаров: " + productsInCart);
        }
    }

    private void checkQuantity(Map<UUID, Integer> productsInCart, Map<UUID, WarehouseProduct> warehouseProductsMap) {
        List<UUID> notAvailabilityProducts = new ArrayList<>();
        for (UUID id : productsInCart.keySet()) {
            if (productsInCart.get(id) > warehouseProductsMap.get(id).getQuantity()) {
                notAvailabilityProducts.add(id);
            }
        }
        log.info("Товары которых не хватает на складе: {}", notAvailabilityProducts);
        if (!notAvailabilityProducts.isEmpty()) {
            throw new ProductInShoppingCartLowQuantityInWarehouse("На складе не хватает товаров: "
                    + notAvailabilityProducts);
        }
    }

    private BookedProductsDto bookingProducts(List<WarehouseProduct> products) {
        BookedProductsDto result = new BookedProductsDto(0.0, 0.0, false);
        for (WarehouseProduct product : products) {
            result.setDeliveryVolume(result.getDeliveryVolume() + product.getWeight());
            result.setDeliveryVolume(result.getDeliveryVolume()
                    + product.getWidth() * product.getHeight() * product.getDepth());
            if (product.getFragile()) {
                result.setFragile(true);
            }
        }
        log.info("Общие данные о заказе: {}", result);
        return result;
    }

    private void setProductQuantityState(WarehouseProduct product) {
        Integer quantity = product.getQuantity();
        QuantityState quantityState;
        if (quantity == 0) {
            quantityState = QuantityState.ENDED;
        } else if (quantity < 10) {
            quantityState = QuantityState.FEW;
        } else if (quantity < 100) {
            quantityState = QuantityState.ENOUGH;
        } else {
            quantityState = QuantityState.MANY;
        }
        try {
            storeFeignClient.setProductQuantityState(product.getProductId(), quantityState);
        } catch (Exception exception) {
            log.info("Товар ещё не добавили в магазин: {}", product.getProductId());
        }
    }
}