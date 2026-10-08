package com.shopway.gateway.filter;

import com.shopway.gateway.service.RateLimiterService;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.time.Duration;

public final class RateLimitFilter {

    private RateLimitFilter() {
    }

    public static HandlerFilterFunction<ServerResponse, ServerResponse> rateLimit(
            RateLimiterService rateLimiterService,
            long capacity,
            long refillTokens,
            Duration refillDuration
    ) {

        return (request, next) -> {

            String clientIp = getClientIp(request);

            boolean allowed =
                    rateLimiterService.isAllowed(
                            clientIp,
                            capacity,
                            refillTokens,
                            refillDuration
                    );

            if (!allowed) {

                return ServerResponse
                        .status(HttpStatus.TOO_MANY_REQUESTS)
                        .header("Retry-After", "60")
                        .body(
                                "Too many requests. Please try again later."
                        );
            }

            return next.handle(request);
        };
    }

    private static String getClientIp(
            ServerRequest request
    ) {

        String forwardedFor =
                request.headers()
                        .firstHeader("X-Forwarded-For");

        if (forwardedFor != null &&
                !forwardedFor.isBlank()) {

            return forwardedFor
                    .split(",")[0]
                    .trim();
        }

        return request.remoteAddress()
                .map(address ->
                        address.getAddress()
                                .getHostAddress()
                )
                .orElse("unknown");
    }
}