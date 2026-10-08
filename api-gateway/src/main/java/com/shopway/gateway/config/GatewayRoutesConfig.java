package com.shopway.gateway.config;

import com.shopway.gateway.filter.CartUserIdentityFilter;
import com.shopway.gateway.filter.RateLimitFilter;
import com.shopway.gateway.service.RateLimiterService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.time.Duration;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.web.servlet.function.RequestPredicates.path;

@Configuration
public class GatewayRoutesConfig {

    private final RateLimiterService rateLimiterService;
    private final CartUserIdentityFilter cartUserIdentityFilter;

    public GatewayRoutesConfig(
            RateLimiterService rateLimiterService,
            CartUserIdentityFilter cartUserIdentityFilter
    ) {
        this.rateLimiterService = rateLimiterService;
        this.cartUserIdentityFilter = cartUserIdentityFilter;
    }

    @Bean
    public RouterFunction<ServerResponse> gatewayRoutes() {

        return route("auth-service")
                .route(
                        path("/api/auth/**"),
                        http()
                )
                .before(uri("http://localhost:8086"))
                .filter(
                        RateLimitFilter.rateLimit(
                                rateLimiterService,
                                20,
                                20,
                                Duration.ofMinutes(1)
                        )
                )
                .build()

                .and(
                        route("catalog-service")
                                .route(
                                        path("/api/catalog/**"),
                                        http()
                                )
                                .before(uri("http://localhost:8087"))
                                .filter(
                                        RateLimitFilter.rateLimit(
                                                rateLimiterService,
                                                100,
                                                100,
                                                Duration.ofMinutes(1)
                                        )
                                )
                                .build()
                )

                // =================================================
                // CART SERVICE
                // =================================================
                .and(
                        route("cart-service")
                                .route(
                                        path("/api/cart/**"),
                                        http()
                                )
                                .before(
                                        uri("http://localhost:8088")
                                )

                                // Rate limit first
                                .filter(
                                        RateLimitFilter.rateLimit(
                                                rateLimiterService,
                                                30,
                                                30,
                                                Duration.ofMinutes(1)
                                        )
                                )

                                // Add X-User-Id only when
                                // the request has a valid JWT.
                                //
                                // Guest requests continue without
                                // X-User-Id and use guest_cart_id cookie.
                                .filter(cartUserIdentityFilter)

                                .build()
                )

                .and(
                        route("order-service")
                                .route(
                                        path("/api/orders/**"),
                                        http()
                                )
                                .before(
                                        uri("http://localhost:8089")
                                )
                                .filter(
                                        RateLimitFilter.rateLimit(
                                                rateLimiterService,
                                                10,
                                                10,
                                                Duration.ofMinutes(1)
                                        )
                                )
                                .build()
                )

                .and(
                        route("notification-service")
                                .route(
                                        path("/api/notification/**"),
                                        http()
                                )
                                .before(
                                        uri("http://localhost:8090")
                                )
                                .filter(
                                        RateLimitFilter.rateLimit(
                                                rateLimiterService,
                                                20,
                                                20,
                                                Duration.ofMinutes(1)
                                        )
                                )
                                .build()
                );
    }
}

