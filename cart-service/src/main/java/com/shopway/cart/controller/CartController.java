package com.shopway.cart.controller;

import com.shopway.cart.dto.AddToCartRequest;
import com.shopway.cart.dto.UpdateCartItemRequest;
import com.shopway.cart.model.Cart;
import com.shopway.cart.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private static final String GUEST_CART_COOKIE = "guest_cart_id";

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // =========================================================
    // GUEST CART
    // =========================================================

    @GetMapping("/guest")
    public ResponseEntity<Cart> getGuestCart(
            @CookieValue(
                    value = GUEST_CART_COOKIE,
                    required = false
            )
            String guestCartId) {

        GuestCart guestCart =
                getOrCreateGuestCart(guestCartId);

        Cart cart =
                cartService.getGuestCart(
                        guestCart.guestCartId()
                );

        return ResponseEntity.ok()
                .header(
                        "Set-Cookie",
                        guestCart.cookie().toString()
                )
                .body(cart);
    }

    @PostMapping("/guest/items")
    public ResponseEntity<Cart> addGuestItem(
            @CookieValue(
                    value = GUEST_CART_COOKIE,
                    required = false
            )
            String guestCartId,

            @Valid
            @RequestBody
            AddToCartRequest request) {

        GuestCart guestCart =
                getOrCreateGuestCart(guestCartId);

        Cart cart =
                cartService.addGuestItem(
                        guestCart.guestCartId(),
                        request
                );

        return ResponseEntity.ok()
                .header(
                        "Set-Cookie",
                        guestCart.cookie().toString()
                )
                .body(cart);
    }

    @PutMapping("/guest/items/{productId}")
    public ResponseEntity<Cart> updateGuestItem(
            @CookieValue(
                    value = GUEST_CART_COOKIE,
                    required = false
            )
            String guestCartId,

            @PathVariable String productId,

            @Valid
            @RequestBody
            UpdateCartItemRequest request) {

        GuestCart guestCart =
                getOrCreateGuestCart(guestCartId);

        Cart cart =
                cartService.updateGuestItem(
                        guestCart.guestCartId(),
                        productId,
                        request
                );

        return ResponseEntity.ok()
                .header(
                        "Set-Cookie",
                        guestCart.cookie().toString()
                )
                .body(cart);
    }

    @DeleteMapping("/guest/items/{productId}")
    public ResponseEntity<Cart> removeGuestItem(
            @CookieValue(
                    value = GUEST_CART_COOKIE,
                    required = false
            )
            String guestCartId,

            @PathVariable String productId) {

        if (guestCartId == null ||
                guestCartId.isBlank()) {

            return ResponseEntity.ok(
                    Cart.builder().build()
            );
        }

        Cart cart =
                cartService.removeGuestItem(
                        guestCartId,
                        productId
                );

        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/guest")
    public ResponseEntity<Void> clearGuestCart(
            @CookieValue(
                    value = GUEST_CART_COOKIE,
                    required = false
            )
            String guestCartId) {

        if (guestCartId != null &&
                !guestCartId.isBlank()) {

            cartService.clearGuestCart(
                    guestCartId
            );
        }

        ResponseCookie deleteCookie =
                deleteGuestCartCookie();

        return ResponseEntity.noContent()
                .header(
                        "Set-Cookie",
                        deleteCookie.toString()
                )
                .build();
    }

    // =========================================================
    // AUTHENTICATED CART
    // =========================================================

    @GetMapping
    public ResponseEntity<Cart> getUserCart(
            @RequestHeader("X-User-Id")
            String userId) {

        return ResponseEntity.ok(
                cartService.getUserCart(userId)
        );
    }

    @PostMapping("/items")
    public ResponseEntity<Cart> addUserItem(
            @RequestHeader("X-User-Id")
            String userId,

            @Valid
            @RequestBody
            AddToCartRequest request) {

        return ResponseEntity.ok(
                cartService.addUserItem(
                        userId,
                        request
                )
        );
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<Cart> updateUserItem(
            @RequestHeader("X-User-Id")
            String userId,

            @PathVariable String productId,

            @Valid
            @RequestBody
            UpdateCartItemRequest request) {

        return ResponseEntity.ok(
                cartService.updateUserItem(
                        userId,
                        productId,
                        request
                )
        );
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Cart> removeUserItem(
            @RequestHeader("X-User-Id")
            String userId,

            @PathVariable String productId) {

        return ResponseEntity.ok(
                cartService.removeUserItem(
                        userId,
                        productId
                )
        );
    }

    @DeleteMapping
    public ResponseEntity<Void> clearUserCart(
            @RequestHeader("X-User-Id")
            String userId) {

        cartService.clearUserCart(userId);

        return ResponseEntity.noContent().build();
    }

    // =========================================================
    // MERGE GUEST CART → USER CART
    // =========================================================

    @PostMapping("/merge")
    public ResponseEntity<Cart> mergeGuestCart(
            @RequestHeader("X-User-Id")
            String userId,

            @CookieValue(
                    value = GUEST_CART_COOKIE,
                    required = false
            )
            String guestCartId) {

        /*
         * No guest cart exists.
         *
         * Simply return the user's current cart.
         */
        if (guestCartId == null ||
                guestCartId.isBlank()) {

            return ResponseEntity.ok(
                    cartService.getUserCart(userId)
            );
        }

        Cart cart =
                cartService.mergeGuestCart(
                        userId,
                        guestCartId
                );

        /*
         * Guest cart has now been merged.
         *
         * Delete the guest cart cookie so that
         * the browser no longer identifies itself
         * as having a guest cart.
         */
        ResponseCookie deleteCookie =
                deleteGuestCartCookie();

        return ResponseEntity.ok()
                .header(
                        "Set-Cookie",
                        deleteCookie.toString()
                )
                .body(cart);
    }

    // =========================================================
    // COOKIE HELPERS
    // =========================================================

    private GuestCart getOrCreateGuestCart(
            String guestCartId) {

        boolean newCart =
                guestCartId == null ||
                        guestCartId.isBlank();

        if (newCart) {
            guestCartId =
                    UUID.randomUUID().toString();
        }

        ResponseCookie cookie =
                ResponseCookie
                        .from(
                                GUEST_CART_COOKIE,
                                guestCartId
                        )
                        .httpOnly(true)
                        .secure(false) // true in production HTTPS
                        .sameSite("Lax")
                        .path("/api/cart")
                        .maxAge(
                                Duration.ofDays(30)
                        )
                        .build();

        return new GuestCart(
                guestCartId,
                cookie
        );
    }

    private ResponseCookie deleteGuestCartCookie() {

        return ResponseCookie
                .from(
                        GUEST_CART_COOKIE,
                        ""
                )
                .httpOnly(true)
                .secure(false) // true in production HTTPS
                .sameSite("Lax")
                .path("/api/cart")
                .maxAge(Duration.ZERO)
                .build();
    }

    // =========================================================
    // INTERNAL RECORD
    // =========================================================

    private record GuestCart(
            String guestCartId,
            ResponseCookie cookie
    ) {
    }
}

