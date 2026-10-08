package com.shopway.gateway.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private final Map<String, Bucket> buckets =
            new ConcurrentHashMap<>();

    public boolean isAllowed(
            String key,
            long capacity,
            long refillTokens,
            Duration refillDuration
    ) {

        String bucketKey = key
                + ":"
                + capacity
                + ":"
                + refillTokens
                + ":"
                + refillDuration;

        Bucket bucket = buckets.computeIfAbsent(
                bucketKey,
                ignored -> createBucket(
                        capacity,
                        refillTokens,
                        refillDuration
                )
        );

        return bucket.tryConsume(1);
    }

    private Bucket createBucket(
            long capacity,
            long refillTokens,
            Duration refillDuration
    ) {

        Refill refill = Refill.intervally(
                refillTokens,
                refillDuration
        );

        Bandwidth limit = Bandwidth.classic(
                capacity,
                refill
        );

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}