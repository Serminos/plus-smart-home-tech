create TABLE IF NOT EXISTS shopping_carts
(
    shopping_cart_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR,
    cart_state VARCHAR
);

create TABLE IF NOT EXISTS products_in_shopping_carts
(
    shopping_cart_id UUID REFERENCES shopping_carts (shopping_cart_id),
    product_id UUID,
    quantity INTEGER
);