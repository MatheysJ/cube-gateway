package com.cube.gateway.filters;

import org.springframework.http.server.ServerHttpRequest;
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

    public Predicate<ServerHttpRequest> isSecured =
            request -> openEndpoints.stream().noneMatch(uri -> request.getURI().getPath().contains(uri));
}
