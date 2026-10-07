package com.shopway.catalog.repository;

import com.shopway.catalog.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProductRepository
        extends MongoRepository<Product, String> {

    List<Product> findByCategory(String category);

    List<Product> findByBrand(String brand);

    List<Product> findByActiveTrue();

    List<Product> findByCategoryAndActiveTrue(String category);

    boolean existsByNameIgnoreCase(String name);
}