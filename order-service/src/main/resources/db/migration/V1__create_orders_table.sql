CREATE TABLE orders (
                        id BIGSERIAL PRIMARY KEY,

                        customer_id BIGINT NOT NULL,

                        status INTEGER NOT NULL,

                        total_amount NUMERIC(19, 2) NOT NULL,

                        currency VARCHAR(3) NOT NULL,

                        created_at TIMESTAMP WITH TIME ZONE NOT NULL,

                        updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);