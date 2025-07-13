package ru.yandex.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.dto.shoppingStore.ProductCategory;
import ru.yandex.practicum.model.Product;

import java.util.List;
import java.util.UUID;

@Repository
public interface ShoppingStoreRepository extends JpaRepository<Product, UUID> {
    List<Product> findAllByProductCategory(ProductCategory productCategory, Pageable pageable);
}