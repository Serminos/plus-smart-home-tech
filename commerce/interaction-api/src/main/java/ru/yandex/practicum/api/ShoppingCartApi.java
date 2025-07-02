package ru.yandex.practicum.api;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.shoppingCart.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.shoppingCart.ShoppingCartDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;
public interface ShoppingCartApi {

    @GetMapping("/api/v1/shopping-cart")
    ShoppingCartDto getUserShoppingCart(@RequestParam(name = "username") String username);
    @PutMapping("/api/v1/shopping-cart")
    ShoppingCartDto addProductInShoppingCart(@RequestParam(name = "username") String username,
                                             @RequestBody Map<UUID, Integer> productsMap);
    @DeleteMapping("/api/v1/shopping-cart")
    void deactivateUserShoppingCart(@RequestParam(name = "username") String username);

    @PostMapping("/api/v1/shopping-cart/remove")
    ShoppingCartDto removeProductFromShoppingCart(
            @RequestParam(name = "username") String username,
            @RequestBody List<UUID> productsId);

    @PostMapping("/api/v1/shopping-cart/change-quantity")
    ShoppingCartDto changeProductQuantityInShoppingCart(
            @RequestParam(name = "username") String username,
            @Valid @RequestBody ChangeProductQuantityRequest changeProductQuantityRequest);
}
