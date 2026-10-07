package com.shopway.catalog.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductResponse(

        String id,

        String name,

        String description,

        BigDecimal price,

        String category,

        String brand,

        Integer stockQuantity,

        List<String> images,

        Boolean active
) {
}