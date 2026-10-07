package com.shopway.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AddToCartRequest(

        @NotBlank(message = "Product ID is required")
        String productId,

        @NotBlank(message = "Product name is required")
        String productName,

        @NotNull(message = "Price is required")
        BigDecimal price,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity,

        String image
) {
}
