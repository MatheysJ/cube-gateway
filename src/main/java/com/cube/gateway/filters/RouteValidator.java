package com.cube.gateway.filters;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {

    public static final List<String> openEndpoints = List.of(
            "/v1/auth/register",
            "/v1/auth/login",
            "/v1/auth/validate"
    );

    public static final List<String> webHooksEndpoints = List.of(
            "/v1/order/status"
    );

    public Predicate<ServerHttpRequest> isSecured =
            request -> openEndpoints.stream().noneMatch(uri -> request.getURI().getPath().contains(uri));

    public Predicate<ServerHttpRequest> isWebHook =
            request -> webHooksEndpoints.stream().anyMatch(uri -> request.getURI().getPath().contains(uri));
}
