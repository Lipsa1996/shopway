package com.shopway.catalog.service;

import com.shopway.catalog.dto.CreateProductRequest;
import com.shopway.catalog.dto.ProductResponse;
import com.shopway.catalog.dto.UpdateProductRequest;
import com.shopway.catalog.exception.ProductNotFoundException;
import com.shopway.catalog.model.Product;
import com.shopway.catalog.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public ProductResponse createProduct(
            CreateProductRequest request) {

        Product product = Product.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .category(request.category())
                .brand(request.brand())
                .stockQuantity(request.stockQuantity())
                .images(request.images())
                .active(true)
                .build();

        Product savedProduct = productRepository.save(product);

        return mapToResponse(savedProduct);
    }

    public List<ProductResponse> getAllProducts() {

        return productRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ProductResponse getProductById(String id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException(id));

        return mapToResponse(product);
    }

    public List<ProductResponse> getProductsByCategory(
            String category) {

        return productRepository
                .findByCategoryAndActiveTrue(category)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<ProductResponse> getProductsByBrand(
            String brand) {

        return productRepository
                .findByBrand(brand)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<ProductResponse> getActiveProducts() {

        return productRepository
                .findByActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ProductResponse updateProduct(
            String id,
            UpdateProductRequest request) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException(id));

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setCategory(request.category());
        product.setBrand(request.brand());
        product.setStockQuantity(request.stockQuantity());
        product.setImages(request.images());
        product.setActive(request.active());

        Product updatedProduct =
                productRepository.save(product);

        return mapToResponse(updatedProduct);
    }

    public void deleteProduct(String id) {

        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }

        productRepository.deleteById(id);
    }

    private ProductResponse mapToResponse(Product product) {

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory(),
                product.getBrand(),
                product.getStockQuantity(),
                product.getImages(),
                product.getActive()
        );
    }
}