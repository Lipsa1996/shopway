package com.shopway.cart.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    public BigDecimal getTotalAmount() {

        return items.stream()
                .map(item ->
                        item.getPrice()
                                .multiply(
                                        BigDecimal.valueOf(
                                                item.getQuantity()
                                        )
                                )
                )
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }

    public int getTotalItems() {

        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }
}

