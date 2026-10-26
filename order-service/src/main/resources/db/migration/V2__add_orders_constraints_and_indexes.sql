ALTER TABLE orders
    ADD CONSTRAINT chk_orders_total_amount_positive
        CHECK (total_amount > 0);

ALTER TABLE orders
    ADD CONSTRAINT chk_orders_currency_length
        CHECK (char_length(currency) = 3);

CREATE INDEX idx_orders_customer_id
    ON orders(customer_id);

CREATE INDEX idx_orders_status
    ON orders(status);