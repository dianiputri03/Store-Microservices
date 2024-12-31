package com.store.apigateway;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.HandlerFunction;

@Component
public class AuthenticationFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {

    private final JwtUtil jwtUtil;
    private final RouterValidator routerValidator;

    public AuthenticationFilter(JwtUtil jwtUtil, RouterValidator routerValidator) {
        this.jwtUtil = jwtUtil;
        this.routerValidator = routerValidator;
    }

    @Override
    public ServerResponse filter(ServerRequest request, HandlerFunction<ServerResponse> handlerFunction) throws Exception {
        if (routerValidator.isSecured.test(request)) {
            if (this.isAuthMissing(request)) {
                return ServerResponse.status(HttpStatus.UNAUTHORIZED).build();
            }

            final String token = this.getAuthHeader(request);
            if (token != null && token.startsWith("Bearer ")) {
                String tokenValue = token.substring(7);
                if (!jwtUtil.validateToken(tokenValue)) {
                    return ServerResponse.status(HttpStatus.UNAUTHORIZED).build();
                }
            } else {
                return ServerResponse.status(HttpStatus.UNAUTHORIZED).build();
            }
        }
        return handlerFunction.handle(request);
    }

    private boolean isAuthMissing(ServerRequest request) {
        return request.headers().firstHeader(HttpHeaders.AUTHORIZATION) == null;
    }

    private String getAuthHeader(ServerRequest request) {
        return request.headers().firstHeader(HttpHeaders.AUTHORIZATION);
    }
}