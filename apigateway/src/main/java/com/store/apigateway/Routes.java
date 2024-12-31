package com.store.apigateway;

import org.springframework.web.servlet.function.ServerResponse;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;

@Configuration
public class Routes {

    private final AuthenticationFilter authenticationFilter;

    public Routes(AuthenticationFilter authenticationFilter) {
        this.authenticationFilter = authenticationFilter;
    }

    @Bean
    public RouterFunction<ServerResponse> routerFunction() {
        return GatewayRouterFunctions.route("api_routes")
                .GET("/api/users/**", HandlerFunctions.http("http://localhost:8080"))
                .POST("/api/users/**", HandlerFunctions.http("http://localhost:8080"))
                .PUT("/api/users/**", HandlerFunctions.http("http://localhost:8080"))
                .DELETE("/api/users/**", HandlerFunctions.http("http://localhost:8080"))
                .GET("/api/products/**", HandlerFunctions.http("http://localhost:8081"))
                .POST("/api/products/**", HandlerFunctions.http("http://localhost:8081"))
                .PUT("/api/products/**", HandlerFunctions.http("http://localhost:8081"))
                .DELETE("/api/products/**", HandlerFunctions.http("http://localhost:8081"))
                .GET("/api/transactions/**", HandlerFunctions.http("http://localhost:8083"))
                .POST("/api/transactions/**", HandlerFunctions.http("http://localhost:8083"))
                .PUT("/api/transactions/**", HandlerFunctions.http("http://localhost:8083"))
                .DELETE("/api/transactions/**", HandlerFunctions.http("http://localhost:8083"))
                .filter((request, next) -> {
                    if (request.path().equals("/api/users/login")) {
                        return next.handle(request); // Skip authentication filter for login
                    }
                    return authenticationFilter.filter(request, next);
                })
                .build();


}
}