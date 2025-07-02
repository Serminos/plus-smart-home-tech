package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.dto.shoppingCart.CartState;

import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "shopping_carts")
@Getter
@Setter
@ToString
public class ShoppingCart {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "shopping_cart_id", updatable = false, nullable = false)
    private UUID shoppingCartId;

    private String username;

    @Enumerated(EnumType.STRING)
    @Column(name = "cart_state")
    private CartState cartState;

    @ElementCollection
    @CollectionTable(name = "products_in_shopping_carts", joinColumns = @JoinColumn(name = "shopping_cart_id"))
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity")
    private Map<UUID, Integer> products;
}
