package com.shopway.gateway.filter;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

@Component
public class CartUserIdentityFilter
        implements HandlerFilterFunction<
        ServerResponse,
        ServerResponse> {

    @Override
    public ServerResponse filter(
            ServerRequest request,
            HandlerFunction<ServerResponse> next
    ) throws Exception {

        /*
         * Never trust X-User-Id coming from the client.
         *
         * Remove it before doing anything else.
         */
        ServerRequest requestWithoutUserId =
                ServerRequest.from(request)
                        .headers(headers ->
                                headers.remove("X-User-Id")
                        )
                        .build();

        /*
         * Get the authenticated principal.
         */
        Authentication authentication =
                requestWithoutUserId
                        .servletRequest()
                        .getUserPrincipal() != null
                        ? (Authentication)
                        requestWithoutUserId
                                .servletRequest()
                                .getUserPrincipal()
                        : null;

        /*
         * GUEST REQUEST
         *
         * No JWT means this is a guest request.
         *
         * Do not add X-User-Id.
         *
         * The Cart Service will identify the guest
         * using the guest_cart_id cookie.
         */
        if (!(authentication
                instanceof JwtAuthenticationToken jwtAuthentication)) {

            return next.handle(requestWithoutUserId);
        }

        /*
         * AUTHENTICATED REQUEST
         *
         * Extract userId from the already validated JWT.
         */
        String userId =
                jwtAuthentication
                        .getToken()
                        .getClaimAsString("userId");

        /*
         * A valid JWT without userId cannot be used
         * for authenticated cart operations.
         */
        if (userId == null || userId.isBlank()) {

            return ServerResponse
                    .status(401)
                    .body("Missing userId claim");
        }

        /*
         * Add the trusted user ID.
         *
         * This value came from the validated JWT,
         * NOT from the client request.
         */
        ServerRequest modifiedRequest =
                ServerRequest
                        .from(requestWithoutUserId)
                        .header(
                                "X-User-Id",
                                userId
                        )
                        .build();

        return next.handle(modifiedRequest);
    }
}

