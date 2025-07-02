package ru.yandex.practicum.feignClient;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.api.warehouse.WarehouseApi;

@FeignClient(name = "warehouse")
public interface WarehouseFeignClient extends WarehouseApi {
}
