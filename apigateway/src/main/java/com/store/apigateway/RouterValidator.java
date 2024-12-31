package com.store.apigateway;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouterValidator {
    public static final List<String> openApiEndpoints = List.of(
            "/api/users/register",
            "/api/users/login"
    );

    public Predicate<ServerRequest> isSecured =
            request -> openApiEndpoints.stream()
                    .noneMatch(uri -> request.uri().getPath().contains(uri));
}