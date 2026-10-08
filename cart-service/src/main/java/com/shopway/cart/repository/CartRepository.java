package com.shopway.cart.repository;

import com.shopway.cart.model.Cart;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
public class CartRepository {

    private static final String USER_CART_KEY_PREFIX =
            "cart:user:";

    private static final String GUEST_CART_KEY_PREFIX =
            "cart:guest:";

    private static final Duration CART_TTL =
            Duration.ofDays(30);

    private final RedisTemplate<String, Cart> redisTemplate;

    public CartRepository(
            RedisTemplate<String, Cart> redisTemplate) {

        this.redisTemplate = redisTemplate;
    }

    // =========================================================
    // USER CART
    // =========================================================

    public Cart findByUserId(String userId) {

        return redisTemplate
                .opsForValue()
                .get(buildUserKey(userId));
    }

    public void saveUserCart(
            String userId,
            Cart cart) {

        redisTemplate
                .opsForValue()
                .set(
                        buildUserKey(userId),
                        cart,
                        CART_TTL
                );
    }

    public void deleteByUserId(String userId) {

        redisTemplate.delete(
                buildUserKey(userId)
        );
    }

    // =========================================================
    // GUEST CART
    // =========================================================

    public Cart findByGuestCartId(
            String guestCartId) {

        return redisTemplate
                .opsForValue()
                .get(
                        buildGuestKey(guestCartId)
                );
    }

    public void saveGuestCart(
            String guestCartId,
            Cart cart) {

        redisTemplate
                .opsForValue()
                .set(
                        buildGuestKey(guestCartId),
                        cart,
                        CART_TTL
                );
    }

    public void deleteByGuestCartId(
            String guestCartId) {

        redisTemplate.delete(
                buildGuestKey(guestCartId)
        );
    }

    // =========================================================
    // REDIS KEYS
    // =========================================================

    private String buildUserKey(String userId) {

        return USER_CART_KEY_PREFIX + userId;
    }

    private String buildGuestKey(String guestCartId) {

        return GUEST_CART_KEY_PREFIX + guestCartId;
    }
}

