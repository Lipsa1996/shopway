package com.shopway.gateway.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int CAPACITY = 100;

    private static final Duration REFILL_PERIOD =
            Duration.ofMinutes(1);

    private final Map<String, Bucket> buckets =
            new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String clientIp = getClientIp(request);

        Bucket bucket = buckets.computeIfAbsent(
                clientIp,
                key -> createBucket()
        );

        if (bucket.tryConsume(1)) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        response.setStatus(
                HttpStatus.TOO_MANY_REQUESTS.value()
        );

        response.setContentType(
                "application/json"
        );

        response.getWriter().write("""
                {
                    "status": 429,
                    "error": "Too Many Requests",
                    "message": "Rate limit exceeded. Please try again later."
                }
                """);
    }

    private Bucket createBucket() {

        Bandwidth limit =
                Bandwidth.classic(
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

    private String getClientIp(
            HttpServletRequest request
    ) {

        String forwardedFor =
                request.getHeader("X-Forwarded-For");

        if (forwardedFor != null &&
                !forwardedFor.isBlank()) {

            return forwardedFor
                    .split(",")[0]
                    .trim();
        }

        return request.getRemoteAddr();
    }
}