package com.shopway.cart.service;

import com.shopway.cart.dto.AddToCartRequest;
import com.shopway.cart.dto.UpdateCartItemRequest;
import com.shopway.cart.model.Cart;
import com.shopway.cart.model.CartItem;
import com.shopway.cart.repository.CartRepository;
import org.springframework.stereotype.Service;

@Service
public class CartService {

    private final CartRepository cartRepository;

    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public Cart getCart(String userId) {

        Cart cart = cartRepository.findByUserId(userId);

        if (cart == null) {

            cart = Cart.builder()
                    .userId(userId)
                    .build();

        }

        return cart;
    }

    public Cart addItem(
            String userId,
            AddToCartRequest request) {

        Cart cart = getCart(userId);

        CartItem existingItem = cart.getItems()
                .stream()
                .filter(item ->
                        item.getProductId()
                                .equals(request.productId())
                )
                .findFirst()
                .orElse(null);

        if (existingItem != null) {

            existingItem.setQuantity(
                    existingItem.getQuantity()
                            + request.quantity()
            );

        } else {

            CartItem item = CartItem.builder()
                    .productId(request.productId())
                    .productName(request.productName())
                    .price(request.price())
                    .quantity(request.quantity())
                    .image(request.image())
                    .build();

            cart.getItems().add(item);
        }

        cartRepository.save(cart);

        return cart;
    }

    public Cart updateItem(
            String userId,
            String productId,
            UpdateCartItemRequest request) {

        Cart cart = getCart(userId);

        CartItem item = findItem(
                cart,
                productId
        );

        item.setQuantity(request.quantity());

        cartRepository.save(cart);

        return cart;
    }

    public Cart removeItem(
            String userId,
            String productId) {

        Cart cart = getCart(userId);

        cart.getItems().removeIf(
                item ->
                        item.getProductId()
                                .equals(productId)
        );

        cartRepository.save(cart);

        return cart;
    }

    public void clearCart(String userId) {

        cartRepository.deleteByUserId(userId);
    }

    private CartItem findItem(
            Cart cart,
            String productId) {

        return cart.getItems()
                .stream()
                .filter(item ->
                        item.getProductId()
                                .equals(productId)
                )
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Product not found in cart: "
                                        + productId
                        )
                );
    }
}

