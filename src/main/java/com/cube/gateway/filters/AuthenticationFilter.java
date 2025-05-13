package com.cube.gateway.filters;

import com.cube.gateway.proxies.user.UserProxy;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RouteValidator validator;


    private ObjectProvider<UserProxy> userProxyProvider;

    public AuthenticationFilter(ObjectProvider<UserProxy> userProxyProvider) {
        super(Config.class);
        this.userProxyProvider = userProxyProvider;
    }

    @Override
    public GatewayFilter apply(Config config) {

        return (exchange, chain) -> {
            if (validator.isSecured.test((ServerHttpRequest) exchange.getRequest())) {
                List<String> authHeaders = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION);

                String username = getUsername(authHeaders);

                if (username == null) {
                    throw new SecurityException("Invalid or expired token.");
                }

                exchange = exchange.mutate()
                        .request(builder -> builder.header("X-User-Id", username))
                        .build();
            }

            return chain.filter(exchange);
        };

    }

    private String getUsername(List<String> authHeaders) {
        if (authHeaders == null || authHeaders.isEmpty()) {
            throw new SecurityException("To access this resource, you must be authenticated.");
        }

        String authHeader = authHeaders.getFirst();

        if (!authHeader.startsWith("Bearer ")) {
            throw new SecurityException("Invalid or expired token.");
        }

        UserProxy userProxy = userProxyProvider.getIfAvailable();

        String authToken = authHeader.substring(7);
        String username = userProxy.getUsernameByToken(authToken);
        return username;
    }

    public static class Config {}
}
