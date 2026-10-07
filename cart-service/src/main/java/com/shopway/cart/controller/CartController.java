package com.shopway.cart.controller;

import com.shopway.cart.dto.AddToCartRequest;
import com.shopway.cart.dto.UpdateCartItemRequest;
import com.shopway.cart.model.Cart;
import com.shopway.cart.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Cart> getCart(
            @PathVariable String userId) {

        return ResponseEntity.ok(
                cartService.getCart(userId)
        );
    }

    @PostMapping("/{userId}/items")
    public ResponseEntity<Cart> addItem(
            @PathVariable String userId,
            @Valid @RequestBody AddToCartRequest request) {

        return ResponseEntity.ok(
                cartService.addItem(
                        userId,
                        request
                )
        );
    }

    @PutMapping("/{userId}/items/{productId}")
    public ResponseEntity<Cart> updateItem(
            @PathVariable String userId,
            @PathVariable String productId,
            @Valid @RequestBody UpdateCartItemRequest request) {

        return ResponseEntity.ok(
                cartService.updateItem(
                        userId,
                        productId,
                        request
                )
        );
    }

    @DeleteMapping("/{userId}/items/{productId}")
    public ResponseEntity<Cart> removeItem(
            @PathVariable String userId,
            @PathVariable String productId) {

        return ResponseEntity.ok(
                cartService.removeItem(
                        userId,
                        productId
                )
        );
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> clearCart(
            @PathVariable String userId) {

        cartService.clearCart(userId);

        return ResponseEntity.noContent().build();
    }
}
