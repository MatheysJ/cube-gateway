package com.cube.gateway.filters;

import com.cube.gateway.factory.TokenFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class AuthenticationFilterTests {

    @Mock
    private GatewayFilterChain chain;

    private GatewayFilter filter;

    @BeforeEach
    void setUp() {
        AuthenticationFilter authenticationFilter = new AuthenticationFilter();
        ReflectionTestUtils.setField(authenticationFilter, "validator", new RouteValidator());
        ReflectionTestUtils.setField(authenticationFilter, "secret", TokenFactory.getMockOfSecret());
        ReflectionTestUtils.setField(authenticationFilter, "asaasAccessToken", TokenFactory.getMockOfAsaasAccessToken());

        filter = authenticationFilter.apply(new AuthenticationFilter.Config());
    }

    @Test
    void shouldLetOpenEndpointsPassWithoutToken() {
        Mockito.when(chain.filter(Mockito.any())).thenReturn(Mono.empty());
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/v1/auth/login"));

        filter.filter(exchange, chain).block();

        Mockito.verify(chain, Mockito.times(1)).filter(Mockito.any());
        Assertions.assertNull(exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldReturnUnauthorizedWhenSecuredRouteHasNoCookie() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/v1/order"));

        filter.filter(exchange, chain).block();

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        Mockito.verifyNoInteractions(chain);
    }

    @Test
    void shouldForwardCustomerHeadersWhenTokenIsValid() {
        Mockito.when(chain.filter(Mockito.any())).thenReturn(Mono.empty());
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/v1/order")
                        .cookie(new HttpCookie("accessToken", TokenFactory.getMockOfValidToken()))
        );

        filter.filter(exchange, chain).block();

        ArgumentCaptor<ServerWebExchange> forwardedExchange = ArgumentCaptor.forClass(ServerWebExchange.class);
        Mockito.verify(chain).filter(forwardedExchange.capture());

        HttpHeaders headers = forwardedExchange.getValue().getRequest().getHeaders();
        Assertions.assertEquals(TokenFactory.getMockOfCustomerId(), headers.getFirst("customer_id"));
        Assertions.assertEquals(TokenFactory.getMockOfAsaasCustomerId(), headers.getFirst("asaas_customer_id"));
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenIsExpired() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/v1/order")
                        .cookie(new HttpCookie("accessToken", TokenFactory.getMockOfExpiredToken()))
        );

        filter.filter(exchange, chain).block();

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        Mockito.verifyNoInteractions(chain);
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenIsSignedWithAnotherSecret() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/v1/order")
                        .cookie(new HttpCookie("accessToken", TokenFactory.getMockOfTokenSignedWithOtherSecret()))
        );

        filter.filter(exchange, chain).block();

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        Mockito.verifyNoInteractions(chain);
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenIsMalformed() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/v1/order")
                        .cookie(new HttpCookie("accessToken", "not-a-jwt"))
        );

        filter.filter(exchange, chain).block();

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        Mockito.verifyNoInteractions(chain);
    }

    @Test
    void shouldLetWebHookPassWithValidAsaasToken() {
        Mockito.when(chain.filter(Mockito.any())).thenReturn(Mono.empty());
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/v1/order/status")
                        .header("asaas-access-token", TokenFactory.getMockOfAsaasAccessToken())
        );

        filter.filter(exchange, chain).block();

        Mockito.verify(chain, Mockito.times(1)).filter(Mockito.any());
    }

    @Test
    void shouldReturnUnauthorizedWhenWebHookHasNoAsaasToken() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/v1/order/status"));

        filter.filter(exchange, chain).block();

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        Mockito.verifyNoInteractions(chain);
    }

    @Test
    void shouldReturnUnauthorizedWhenWebHookHasWrongAsaasToken() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/v1/order/status")
                        .header("asaas-access-token", "wrong-token")
        );

        filter.filter(exchange, chain).block();

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        Mockito.verifyNoInteractions(chain);
    }
}
