package ru.yandex.practicum.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.delivery.DeliveryDto;
import ru.yandex.practicum.dto.delivery.DeliveryState;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.warehouse.ShippedToDeliveryRequest;
import ru.yandex.practicum.exception.NoDeliveryFoundException;
import ru.yandex.practicum.exception.NoOrderFoundException;
import ru.yandex.practicum.exception.NotEnoughInfoInOrderToCalculateException;
import ru.yandex.practicum.feignClient.OrderFeignClient;
import ru.yandex.practicum.feignClient.WarehouseFeignClient;
import ru.yandex.practicum.mapper.DeliveryMapper;
import ru.yandex.practicum.model.Address;
import ru.yandex.practicum.model.Delivery;
import ru.yandex.practicum.repository.AddressRepository;
import ru.yandex.practicum.repository.DeliveryRepository;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryService {
    private final DeliveryRepository deliveryRepository;
    private final AddressRepository addressRepository;
    private final OrderFeignClient orderFeign;
    private final WarehouseFeignClient warehouseFeign;

    private static final Double BASE_COST = 5.0;
    private static final Double WAREHOUSE_ADDRESS_2_SURCHARGE = 2.0;
    private static final Double FRAGILE_SURCHARGE = 0.2;
    private static final Double WEIGHT_SURCHARGE = 0.3;
    private static final Double VOLUME_SURCHARGE = 0.2;
    private static final Double ADDRESS_DELIVERY_SURCHARGE = 0.2;

    public DeliveryDto createDelivery(DeliveryDto deliveryDto) {
        Delivery delivery = DeliveryMapper.mapToDelivery(deliveryDto);
        Optional<Delivery> deliveryOpt = deliveryRepository.findByOrderId(deliveryDto.getOrderId());
        if (deliveryOpt.isPresent()) {
            log.info("Старый Delivery: {}", deliveryOpt.get());
            delivery.setDeliveryId(deliveryOpt.get().getDeliveryId());
        } else {
            log.info("Доставки для указанного заказа нет, создаем новую");
            delivery.setDeliveryId(null);
        }

        delivery.setFromAddress(findOrCreateAddress(delivery.getFromAddress()));
        if (checkEqualsAddress(delivery.getFromAddress(), delivery.getToAddress())) {
            delivery.setToAddress(delivery.getFromAddress());
            log.info("Адрес склада и адрес доставки совпадают");
        } else {
            delivery.setToAddress(findOrCreateAddress(delivery.getToAddress()));
        }

        delivery.setDeliveryState(DeliveryState.CREATED);
        delivery = deliveryRepository.save(delivery);
        log.info("Сохраняем доставку в БД: {}", delivery);
        return DeliveryMapper.mapToDeliveryDto(delivery);
    }

    public Double calculateDelivery(OrderDto orderDto) {
        if (!validOrder(orderDto)) {
            throw new NotEnoughInfoInOrderToCalculateException("Недостаточно данных для расчета стоимости доставки");
        }
        Delivery delivery = findDeliveryById(orderDto.getDeliveryId());
        Address fromAddress = delivery.getFromAddress();
        Address toAddress = delivery.getToAddress();
        Double cost = BASE_COST;

        switch (fromAddress.getCity()) {
            case "ADDRESS_1":
                cost += cost;
                break;
            case "ADDRESS_2":
                cost += cost * WAREHOUSE_ADDRESS_2_SURCHARGE;
                break;
            default:
                break;
        }
        if (orderDto.getFragile()) {
            cost += cost * FRAGILE_SURCHARGE;
        }
        cost += orderDto.getDeliveryWeight() * WEIGHT_SURCHARGE;
        cost += orderDto.getDeliveryVolume() * VOLUME_SURCHARGE;
        if (!(fromAddress.getCountry().equals(toAddress.getCountry())
                && fromAddress.getCity().equals(toAddress.getCity())
                && fromAddress.getStreet().equals(toAddress.getStreet()))) {
            cost += cost * ADDRESS_DELIVERY_SURCHARGE;
        }
        log.info("Стоимость доставки: {}", cost);
        return cost;
    }

    public void setDeliverySuccessful(UUID deliveryId) {
        Delivery delivery = findDeliveryById(deliveryId);
        try {
            OrderDto orderDto = orderFeign.deliveryOrder(delivery.getOrderId());
            log.info("Обновляем статус в сервисе заказов: {}", orderDto);
        } catch (FeignException e) {
            log.info("Сбой при обновлении статуса в сервисе заказов: {}", e.getMessage());
            throw new NoOrderFoundException(e.getMessage());
        }
        delivery.setDeliveryState(DeliveryState.DELIVERED);
        delivery = deliveryRepository.save(delivery);
        log.info("Обновляем доставку в БД: {}", delivery);
    }

    public void setDeliveryFailed(UUID deliveryId) {
        Delivery delivery = findDeliveryById(deliveryId);
        try {
            OrderDto orderDto = orderFeign.deliveryOrderFailed(delivery.getOrderId());
            log.info("Обновляем статус в сервисе заказов: {}", orderDto);
        } catch (FeignException e) {
            log.info("Сбой при обновлении статуса в сервисе заказов: {}", e.getMessage());
            throw new NoOrderFoundException(e.getMessage());
        }
        delivery.setDeliveryState(DeliveryState.FAILED);
        delivery = deliveryRepository.save(delivery);
        log.info("Обновляем доставку в БД: {}", delivery);
    }

    public void pickOrderForDelivery(UUID deliveryId) {
        Delivery delivery = findDeliveryById(deliveryId);
        ShippedToDeliveryRequest shippedRequest = new ShippedToDeliveryRequest();
        shippedRequest.setDeliveryId(deliveryId);
        shippedRequest.setOrderId(delivery.getOrderId());
        try {
            warehouseFeign.shippedProductsToTheWarehouse(shippedRequest);
            log.info("Обновляем статус на складе и передаем заказ в доставку: {}", shippedRequest);
        } catch (FeignException e) {
            log.info("Сбой при обновлении статуса в сервисе склада: {}", e.getMessage());
            throw new NoOrderFoundException(e.getMessage());
        }
        delivery.setDeliveryState(DeliveryState.IN_PROGRESS);
        delivery = deliveryRepository.save(delivery);
        log.info("Обновляем доставку в БД: {}", delivery);
    }

    private Address findOrCreateAddress(Address address) {
        Optional<Address> addressOpt = addressRepository.findByCountryAndCityAndStreetAndHouseAndFlat(
                address.getCountry(), address.getCity(), address.getStreet(), address.getHouse(), address.getFlat());
        if (addressOpt.isPresent()) {
            log.info("Старый адрес: {}", addressOpt.get());
            return addressOpt.get();
        }
        Address result = addressRepository.save(address);
        log.info("Создаем новый адрес: {}", result);
        return result;
    }

    private Boolean checkEqualsAddress(Address a1, Address a2) {
        return Objects.equals(a1.getCountry(), a2.getCountry()) &&
                Objects.equals(a1.getCity(), a2.getCity()) &&
                Objects.equals(a1.getStreet(), a2.getStreet()) &&
                Objects.equals(a1.getHouse(), a2.getHouse()) &&
                Objects.equals(a1.getFlat(), a2.getFlat());
    }

    private Delivery findDeliveryById(UUID deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new NoDeliveryFoundException("Не найдена доставка с id: " + deliveryId));
        log.info("Находим доставку в БД: {}", delivery);
        return delivery;
    }

    private Boolean validOrder(OrderDto dto) {
        return dto.getDeliveryWeight() != null
                && dto.getDeliveryVolume() != null
                && dto.getFragile() != null;
    }
}
