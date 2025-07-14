create TABLE IF NOT EXISTS warehouse_products
(
    product_id UUID PRIMARY KEY,
    fragile BOOLEAN,
    width DOUBLE PRECISION,
    height DOUBLE PRECISION,
    depth DOUBLE PRECISION,
    weight DOUBLE PRECISION,
    quantity INTEGER
);

create TABLE IF NOT EXISTS order_bookings
(
    order_id UUID PRIMARY KEY,
    delivery_id UUID,
    delivery_weight DOUBLE PRECISION NOT NULL,
    delivery_volume DOUBLE PRECISION NOT NULL,
    fragile BOOLEAN NOT NULL
);

create TABLE IF NOT EXISTS booked_products
(
    order_id UUID REFERENCES order_bookings (order_id),
    product_id UUID,
    quantity INTEGER
);