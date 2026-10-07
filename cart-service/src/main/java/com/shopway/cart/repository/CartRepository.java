package com.shopway.cart.repository;

import com.shopway.cart.model.Cart;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
public class CartRepository {

    private static final String CART_KEY_PREFIX = "cart:";

    private static final Duration CART_TTL =
            Duration.ofDays(30);

    private final RedisTemplate<String, Cart> redisTemplate;

    public CartRepository(
            RedisTemplate<String, Cart> redisTemplate) {

        this.redisTemplate = redisTemplate;
    }

    public Cart findByUserId(String userId) {

        String key = buildKey(userId);

        return redisTemplate
                .opsForValue()
                .get(key);
    }

    public void save(Cart cart) {

        String key = buildKey(cart.getUserId());

        redisTemplate
                .opsForValue()
                .set(
                        key,
                        cart,
                        CART_TTL
                );
    }

    public void deleteByUserId(String userId) {

        redisTemplate.delete(
                buildKey(userId)
        );
    }

    private String buildKey(String userId) {

        return CART_KEY_PREFIX + userId;
    }
}