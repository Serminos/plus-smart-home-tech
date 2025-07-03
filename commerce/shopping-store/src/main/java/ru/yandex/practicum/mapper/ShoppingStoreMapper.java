package ru.yandex.practicum.mapper;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.dto.shoppingStore.ProductDto;
import ru.yandex.practicum.model.Product;

import java.util.List;

@Slf4j
public class ShoppingStoreMapper {
    public static Product mapToProduct(ProductDto productDto) {
        Product product = new Product();
        product.setProductName(productDto.getProductName());
        product.setDescription(productDto.getDescription());
        product.setImageSrc(productDto.getImageSrc());
        product.setQuantityState(productDto.getQuantityState());
        product.setProductState(productDto.getProductState());
        product.setProductCategory(productDto.getProductCategory());
        product.setPrice(productDto.getPrice());
        log.debug("Результат маппинга dto в product: {}", product);
        return product;
    }

    public static ProductDto mapToProductDto(Product product) {
        ProductDto productDto = new ProductDto();
        productDto.setProductId(product.getProductId().toString());
        productDto.setProductName(product.getProductName());
        productDto.setDescription(product.getDescription());
        productDto.setImageSrc(product.getImageSrc());
        productDto.setQuantityState(product.getQuantityState());
        productDto.setProductState(product.getProductState());
        productDto.setProductCategory(product.getProductCategory());
        productDto.setPrice(product.getPrice());
        log.debug("Результат маппинга product в productDto: {}", productDto);
        return productDto;
    }

    public static List<ProductDto> mapToProductDto(List<Product> products) {
        return products.stream().map(ShoppingStoreMapper::mapToProductDto).toList();
    }
}
