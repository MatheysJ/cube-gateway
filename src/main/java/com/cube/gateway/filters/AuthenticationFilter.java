package com.cube.gateway.filters;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;

@Slf4j
@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RouteValidator validator;

    @Value("${token.secret}")
    private String secret;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        log.info("Gateway filter started");
        return (exchange, chain) -> {
            if (validator.isSecured.test(exchange.getRequest())) {
                log.info("Route is secured");
                List<String> authHeaders = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION);

                try {
                    if (authHeaders == null || authHeaders.isEmpty()) {
                        log.error("To access this resource, you must be authenticated.");
                        throw new SecurityException("To access this resource, you must be authenticated.");
                    }

                    String authHeader = authHeaders.getFirst();

                    if (!authHeader.startsWith("Bearer ")) {

                        throw new SecurityException("Invalid or expired token.");
                    }

                    String token = authHeader.substring(7);

                    Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

                    Claims claims = Jwts.parserBuilder()
                            .setSigningKey(key)
                            .build()
                            .parseClaimsJws(token)
                            .getBody();

                    String username = claims.getSubject();

                    exchange = exchange.mutate()
                            .request(builder -> builder.header("customer_id", username))
                            .build();

                } catch (ExpiredJwtException e) {
                    log.error("Token is expired");
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                } catch (JwtException e) {
                    log.error("Invalid token with error: ", e);
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }

            }

            log.info("Gateway filter end");

            return chain.filter(exchange);
        };
    };

    public static class Config {}
}
