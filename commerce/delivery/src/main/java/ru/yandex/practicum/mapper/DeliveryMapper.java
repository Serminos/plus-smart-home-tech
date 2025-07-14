package ru.yandex.practicum.mapper;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.dto.delivery.DeliveryDto;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.model.Address;
import ru.yandex.practicum.model.Delivery;

@Slf4j
public class DeliveryMapper {
    public static Delivery mapToDelivery(DeliveryDto dto) {
        Delivery delivery = new Delivery();
        delivery.setDeliveryId(dto.getDeliveryId());
        delivery.setFromAddress(mapToAddress(dto.getFromAddress()));
        delivery.setToAddress(mapToAddress(dto.getToAddress()));
        delivery.setOrderId(dto.getOrderId());
        delivery.setDeliveryState(dto.getDeliveryState());
        log.info("Результат маппинга в Delivery: {}", delivery);
        return delivery;
    }

    public static DeliveryDto mapToDeliveryDto(Delivery delivery) {
        DeliveryDto dto = new DeliveryDto();
        dto.setDeliveryId(delivery.getDeliveryId());
        dto.setFromAddress(mapToAddressDto(delivery.getFromAddress()));
        dto.setToAddress(mapToAddressDto(delivery.getToAddress()));
        dto.setOrderId(delivery.getOrderId());
        dto.setDeliveryState(delivery.getDeliveryState());
        log.info("Результат маппинга в DeliveryDto: {}", dto);
        return dto;
    }

    public static Address mapToAddress(AddressDto dto) {
        Address address = new Address();
        address.setCountry(dto.getCountry());
        address.setCity(dto.getCity());
        address.setStreet(dto.getStreet());
        address.setHouse(dto.getHouse());
        address.setFlat(dto.getFlat());
        log.info("Результат маппинга в Address: {}", address);
        return address;
    }

    public static AddressDto mapToAddressDto(Address address) {
        AddressDto dto = new AddressDto();
        dto.setCountry(address.getCountry());
        dto.setCity(address.getCity());
        dto.setStreet(address.getStreet());
        dto.setHouse(address.getHouse());
        dto.setFlat(address.getFlat());
        log.info("Результат маппинга в AddressDto: {}", dto);
        return dto;
    }
}
