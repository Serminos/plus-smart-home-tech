package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.shoppingCart.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.shoppingCart.ShoppingCartDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ShoppingCartService {
    ShoppingCartDto addProductInShoppingCart(String username, Map<UUID, Integer> productsMap);

    ShoppingCartDto getUserShoppingCart(String username);

    void deactivateUserShoppingCart(String username);

    ShoppingCartDto removeProductFromShoppingCart(String username, List<UUID> productsId);

    ShoppingCartDto changeProductQuantityInShoppingCart(String username,
                                                        ChangeProductQuantityRequest changeProductQuantityRequest);
}
