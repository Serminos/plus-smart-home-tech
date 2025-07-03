package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.shoppingCart.CartState;
import ru.yandex.practicum.dto.shoppingCart.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.shoppingCart.ShoppingCartDto;
import ru.yandex.practicum.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.exception.NoProductsInShoppingCartException;
import ru.yandex.practicum.exception.NotAuthorizedUserException;
import ru.yandex.practicum.feignClient.WarehouseFeignClient;
import ru.yandex.practicum.mapper.ShoppingCartMapper;
import ru.yandex.practicum.model.ShoppingCart;
import ru.yandex.practicum.repository.ShoppingCartRepository;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShoppingCartServiceImp implements ShoppingCartService {
    private final ShoppingCartRepository cartRepository;
    private final WarehouseFeignClient warehouseFeignClient;

    @Override
    public ShoppingCartDto addProductInShoppingCart(String username, Map<UUID, Integer> productsMap) {
        checkUsername(username);
        if (productsMap == null || productsMap.isEmpty()) {
            throw new IllegalArgumentException("productsMap не может быть null или пустым");
        }
        ShoppingCart shoppingCart = getActiveShoppingCartByUserName(username);
        shoppingCart.getProducts().putAll(productsMap);
        log.info("Добавили новые позиции продуктов в корзину: {}", shoppingCart);
        BookedProductsDto bookedProductsDto = warehouseFeignClient
                .checkProductQuantityInWarehouse(ShoppingCartMapper.mapToShoppingCartDto(shoppingCart));
        log.info("Проверили наличие продуктов на складе: {}", bookedProductsDto);
        shoppingCart = cartRepository.save(shoppingCart);
        log.info("Сохранили корзину в БД: {}", shoppingCart);
        return ShoppingCartMapper.mapToShoppingCartDto(shoppingCart);
    }

    @Override
    public ShoppingCartDto getUserShoppingCart(String username) {
        checkUsername(username);
        ShoppingCart shoppingCart = getActiveShoppingCartByUserName(username);
        return ShoppingCartMapper.mapToShoppingCartDto(shoppingCart);
    }

    @Override
    public void deactivateUserShoppingCart(String username) {
        checkUsername(username);
        ShoppingCart shoppingCart = getActiveShoppingCartByUserName(username);
        shoppingCart.setCartState(CartState.DEACTIVATE);
        shoppingCart = cartRepository.save(shoppingCart);
        log.info("Корзина деактивирована {}", shoppingCart);
    }

    @Override
    public ShoppingCartDto removeProductFromShoppingCart(String username, List<UUID> productsId) {
        checkUsername(username);
        if (productsId == null || productsId.isEmpty()) {
            throw new IllegalArgumentException("Список удаляемых продуктов не должен быть null или пустым");
        }
        ShoppingCart shoppingCart = getActiveShoppingCartByUserName(username);
        if (shoppingCart.getProducts().isEmpty()) {
            throw new NoProductsInShoppingCartException("Корзина уже пуста");
        }
        for (UUID id : productsId) {
            shoppingCart.getProducts().remove(id);
        }
        shoppingCart = cartRepository.save(shoppingCart);
        log.info("Обновленная корзина: {}", shoppingCart);
        return ShoppingCartMapper.mapToShoppingCartDto(shoppingCart);
    }

    @Override
    public ShoppingCartDto changeProductQuantityInShoppingCart(String username,
                                                               ChangeProductQuantityRequest changeQuantityRequest) {
        checkUsername(username);
        ShoppingCart shoppingCart = getActiveShoppingCartByUserName(username);
        if (!shoppingCart.getProducts().containsKey(changeQuantityRequest.getProductId())) {
            throw new NoProductsInShoppingCartException("В корзине нет товара с id: " + changeQuantityRequest.getProductId());
        }
        shoppingCart.getProducts().put(changeQuantityRequest.getProductId(), changeQuantityRequest.getNewQuantity());
        BookedProductsDto bookedProductsDto = warehouseFeignClient
                .checkProductQuantityInWarehouse(ShoppingCartMapper.mapToShoppingCartDto(shoppingCart));
        log.info("Проверили наличие продуктов на складе: {}", bookedProductsDto);
        shoppingCart = cartRepository.save(shoppingCart);
        log.info("Обновленная корзина: {}", shoppingCart);
        return ShoppingCartMapper.mapToShoppingCartDto(shoppingCart);
    }

    private void checkUsername(String username) {
        log.info("Проверка имени пользователя: {}", username);
        if (username == null || username.isBlank()) {
            throw new NotAuthorizedUserException("Имя пользователя не может быть null или пустым");
        }
    }

    private ShoppingCart getActiveShoppingCartByUserName(String username) {
        Optional<ShoppingCart> shoppingCartOpt = cartRepository.findByUsernameAndCartState(username, CartState.ACTIVE);
        ShoppingCart shoppingCart;
        if (shoppingCartOpt.isEmpty()) {
            log.info("У пользователя с именем: {} нет активной корзины", username);
            shoppingCart = new ShoppingCart();
            shoppingCart.setUsername(username);
            shoppingCart.setCartState(CartState.ACTIVE);
            shoppingCart.setProducts(new HashMap<>());
            shoppingCart = cartRepository.save(shoppingCart);
            log.info("Новая корзина пользователя: {}", shoppingCart);
        } else {
            shoppingCart = shoppingCartOpt.get();
            log.info("Корзина пользователя: {}", shoppingCart);
        }
        return shoppingCart;
    }
}
