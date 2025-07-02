package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.shoppingCart.ShoppingCartDto;
import ru.yandex.practicum.dto.shoppingStore.QuantityState;
import ru.yandex.practicum.dto.shoppingStore.SetProductQuantityStateRequest;
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
        UUID productId = UUID.fromString(newProductInWarehouseRequest.getProductId());
        if (warehouseRepository.findById(productId).isPresent()) {
            throw new SpecifiedProductAlreadyInWarehouseException("Товар уже есть на складе: " + productId);
        }
        WarehouseProduct newProduct = WarehouseMapper.mapToWarehouseProduct(newProductInWarehouseRequest);
        warehouseRepository.save(newProduct);
    }

    @Override
    public BookedProductsDto checkProductQuantityInWarehouse(ShoppingCartDto shoppingCartDto) {
        Map<UUID, Integer> productsInCart = shoppingCartDto.getProducts();
        List<WarehouseProduct> warehouseProducts = warehouseRepository.findAllById(productsInCart.keySet());
        Map<UUID, WarehouseProduct> warehouseMap = warehouseProducts.stream()
                .collect(Collectors.toMap(WarehouseProduct::getProductId, Function.identity()));

        // Проверка наличия
        Set<UUID> missingIds = productsInCart.keySet().stream()
                .filter(id -> !warehouseMap.containsKey(id))
                .collect(Collectors.toSet());
        if (!missingIds.isEmpty()) {
            throw new NoSpecifiedProductInWarehouseException("Отсутствуют товары: " + missingIds);
        }

        // Проверка количества
        List<UUID> insufficientIds = productsInCart.keySet().stream()
                .filter(id -> productsInCart.get(id) > warehouseMap.get(id).getQuantity())
                .collect(Collectors.toList());
        if (!insufficientIds.isEmpty()) {
            throw new ProductInShoppingCartLowQuantityInWarehouse("Недостаточно товаров: " + insufficientIds);
        }

        return bookingProducts(warehouseProducts);
    }

    @Override
    public void addProductInWarehouse(AddProductToWarehouseRequest addProductRequest) {
        UUID productId = UUID.fromString(addProductRequest.getProductId());
        WarehouseProduct product = warehouseRepository.findById(productId).orElseThrow(
                () -> new NoSpecifiedProductInWarehouseException("Товар уже есть на складе: " + productId));
        log.info("Товар из склада: {}", product);
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
            if (productsInCart.get(id) < warehouseProductsMap.get(id).getQuantity()) {
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
        double totalWeight = 0.0;
        double totalVolume = 0.0;
        boolean fragile = false;

        for (WarehouseProduct product : products) {
            totalWeight += product.getWeight();
            totalVolume += product.getWidth() * product.getHeight() * product.getDepth();
            if (product.getFragile()) {
                fragile = true;
            }
        }
        return new BookedProductsDto(totalWeight, totalVolume, fragile);
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
        storeFeignClient.setProductQuantityState(new SetProductQuantityStateRequest(product.getProductId(), quantityState));
    }
}