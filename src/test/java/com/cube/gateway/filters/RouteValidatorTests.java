package com.cube.gateway.filters;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;

class RouteValidatorTests {

    private final RouteValidator routeValidator = new RouteValidator();

    @ParameterizedTest
    @ValueSource(strings = {"/v1/auth/register", "/v1/auth/login", "/v1/auth/validate"})
    void shouldNotSecureOpenEndpoints(String path) {
        Assertions.assertFalse(routeValidator.isSecured.test(MockServerHttpRequest.post(path).build()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/v1/customer/me", "/v1/order", "/v1/order/1", "/v1/auth/logout"})
    void shouldSecureEveryOtherEndpoint(String path) {
        Assertions.assertTrue(routeValidator.isSecured.test(MockServerHttpRequest.get(path).build()));
    }

    @Test
    void shouldIdentifyAsaasWebHookEndpoint() {
        Assertions.assertTrue(routeValidator.isWebHook.test(MockServerHttpRequest.post("/v1/order/status").build()));
    }

    @Test
    void shouldNotTreatOrderEndpointsAsWebHook() {
        Assertions.assertFalse(routeValidator.isWebHook.test(MockServerHttpRequest.get("/v1/order/1").build()));
    }
}
