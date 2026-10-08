package com.shopway.cart.service;

import com.shopway.cart.dto.AddToCartRequest;
import com.shopway.cart.dto.UpdateCartItemRequest;
import com.shopway.cart.model.Cart;
import com.shopway.cart.model.CartItem;
import com.shopway.cart.repository.CartRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class CartService {

    private final CartRepository cartRepository;

    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    // =========================================================
    // GUEST CART
    // =========================================================

    public Cart getGuestCart(String guestCartId) {

        Cart cart =
                cartRepository.findByGuestCartId(guestCartId);

        if (cart == null) {
            return Cart.builder()
                    .items(new ArrayList<>())
                    .build();
        }

        return cart;
    }

    public Cart addGuestItem(
            String guestCartId,
            AddToCartRequest request) {

        Cart cart = getGuestCart(guestCartId);

        addItemToCart(cart, request);

        cartRepository.saveGuestCart(
                guestCartId,
                cart
        );

        return cart;
    }

    public Cart updateGuestItem(
            String guestCartId,
            String productId,
            UpdateCartItemRequest request) {

        Cart cart = getGuestCart(guestCartId);

        CartItem item = findItem(
                cart,
                productId
        );

        item.setQuantity(request.quantity());

        cartRepository.saveGuestCart(
                guestCartId,
                cart
        );

        return cart;
    }

    public Cart removeGuestItem(
            String guestCartId,
            String productId) {

        Cart cart = getGuestCart(guestCartId);

        cart.getItems().removeIf(
                item ->
                        item.getProductId()
                                .equals(productId)
        );

        cartRepository.saveGuestCart(
                guestCartId,
                cart
        );

        return cart;
    }

    public void clearGuestCart(String guestCartId) {

        cartRepository.deleteByGuestCartId(
                guestCartId
        );
    }

    // =========================================================
    // USER CART
    // =========================================================

    public Cart getUserCart(String userId) {

        Cart cart =
                cartRepository.findByUserId(userId);

        if (cart == null) {
            return Cart.builder()
                    .items(new ArrayList<>())
                    .build();
        }

        return cart;
    }

    public Cart addUserItem(
            String userId,
            AddToCartRequest request) {

        Cart cart = getUserCart(userId);

        addItemToCart(cart, request);

        cartRepository.saveUserCart(
                userId,
                cart
        );

        return cart;
    }

    public Cart updateUserItem(
            String userId,
            String productId,
            UpdateCartItemRequest request) {

        Cart cart = getUserCart(userId);

        CartItem item = findItem(
                cart,
                productId
        );

        item.setQuantity(request.quantity());

        cartRepository.saveUserCart(
                userId,
                cart
        );

        return cart;
    }

    public Cart removeUserItem(
            String userId,
            String productId) {

        Cart cart = getUserCart(userId);

        cart.getItems().removeIf(
                item ->
                        item.getProductId()
                                .equals(productId)
        );

        cartRepository.saveUserCart(
                userId,
                cart
        );

        return cart;
    }

    public void clearUserCart(String userId) {

        cartRepository.deleteByUserId(userId);
    }

    // =========================================================
    // MERGE GUEST CART → USER CART
    // =========================================================

    public Cart mergeGuestCart(
            String userId,
            String guestCartId) {

        Cart userCart =
                getUserCart(userId);

        Cart guestCart =
                cartRepository.findByGuestCartId(
                        guestCartId
                );

        /*
         * Guest cart may already have expired.
         *
         * In that case there is nothing to merge.
         */
        if (guestCart == null) {
            return userCart;
        }

        /*
         * Add every guest item into
         * the user's cart.
         *
         * If the product already exists,
         * quantities are combined.
         */
        for (CartItem guestItem :
                guestCart.getItems()) {

            mergeItem(
                    userCart,
                    guestItem
            );
        }

        /*
         * Save the merged user cart.
         */
        cartRepository.saveUserCart(
                userId,
                userCart
        );

        /*
         * Guest cart is no longer needed.
         */
        cartRepository.deleteByGuestCartId(
                guestCartId
        );

        return userCart;
    }

    // =========================================================
    // INTERNAL HELPERS
    // =========================================================

    private void addItemToCart(
            Cart cart,
            AddToCartRequest request) {

        CartItem existingItem =
                cart.getItems()
                        .stream()
                        .filter(item ->
                                item.getProductId()
                                        .equals(
                                                request.productId()
                                        )
                        )
                        .findFirst()
                        .orElse(null);

        if (existingItem != null) {

            existingItem.setQuantity(
                    existingItem.getQuantity()
                            + request.quantity()
            );

        } else {

            CartItem item =
                    CartItem.builder()
                            .productId(
                                    request.productId()
                            )
                            .productName(
                                    request.productName()
                            )
                            .price(
                                    request.price()
                            )
                            .quantity(
                                    request.quantity()
                            )
                            .image(
                                    request.image()
                            )
                            .build();

            cart.getItems().add(item);
        }
    }

    private void mergeItem(
            Cart userCart,
            CartItem guestItem) {

        CartItem existingItem =
                userCart.getItems()
                        .stream()
                        .filter(item ->
                                item.getProductId()
                                        .equals(
                                                guestItem.getProductId()
                                        )
                        )
                        .findFirst()
                        .orElse(null);

        if (existingItem != null) {

            existingItem.setQuantity(
                    existingItem.getQuantity()
                            + guestItem.getQuantity()
            );

        } else {

            CartItem copiedItem =
                    CartItem.builder()
                            .productId(
                                    guestItem.getProductId()
                            )
                            .productName(
                                    guestItem.getProductName()
                            )
                            .price(
                                    guestItem.getPrice()
                            )
                            .quantity(
                                    guestItem.getQuantity()
                            )
                            .image(
                                    guestItem.getImage()
                            )
                            .build();

            userCart.getItems().add(
                    copiedItem
            );
        }
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

