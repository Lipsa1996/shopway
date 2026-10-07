package com.shopway.gateway.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimiterConfig {

    private static final int CAPACITY = 100;

    private static final Duration REFILL_PERIOD =
            Duration.ofMinutes(1);

    @Bean
    public Map<String, Bucket> rateLimitBuckets() {
        return new ConcurrentHashMap<>();
    }

    public Bucket createBucket() {

        Bandwidth limit = Bandwidth.classic(
                CAPACITY,
                Refill.greedy(
                        CAPACITY,
                        REFILL_PERIOD
                )
        );

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}