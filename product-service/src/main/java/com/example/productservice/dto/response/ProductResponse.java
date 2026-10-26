package com.example.productservice.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String currency,
        Integer stockQuantity,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}