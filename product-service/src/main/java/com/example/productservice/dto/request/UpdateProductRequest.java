package com.example.productservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateProductRequest(

                @NotBlank @Size(max = 200) String name,

                @Size(max = 1000) String description,

                @NotNull @DecimalMin(value = "0.01") @Digits(integer = 17, fraction = 2) BigDecimal price,

                @NotBlank @Size(min = 3, max = 3) String currency,

                @NotNull @PositiveOrZero Integer stockQuantity,

                @NotNull Long version

) {
}
