package com.shopway.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.time.Duration;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.filter.Bucket4jFilterFunctions.rateLimit;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouterFunction<ServerResponse> rateLimitedRoutes() {

        return route("catalog-rate-limited")

                .GET("/api/catalog/**", http())

                .before(uri("http://localhost:8087"))

                .filter(
                        rateLimit(config ->
                                config
                                        .setCapacity(100)
                                        .setPeriod(Duration.ofMinutes(1))
                                        .setKeyResolver(request ->
                                                getClientIp(request)
                                        )
                        )
                )

                .build();
    }

    private String getClientIp(
            org.springframework.web.servlet.function.ServerRequest request
    ) {

        String forwardedFor =
                request.headers()
                        .firstHeader("X-Forwarded-For");

        if (forwardedFor != null &&
                !forwardedFor.isBlank()) {

            return forwardedFor.split(",")[0].trim();
        }

        return request.remoteAddress()
                .map(address ->
                        address.getAddress()
                                .getHostAddress()
                )
                .orElse("unknown");
    }
}