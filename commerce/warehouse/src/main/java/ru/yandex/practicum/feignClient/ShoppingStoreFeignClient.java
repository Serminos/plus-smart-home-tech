package ru.yandex.practicum.feignClient;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.api.shoppingStore.ShoppingStoreApi;

@FeignClient(name = "shopping-store")
public interface ShoppingStoreFeignClient extends ShoppingStoreApi {
}