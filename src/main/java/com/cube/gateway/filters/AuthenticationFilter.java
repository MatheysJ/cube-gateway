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
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

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

    @Value("${asaas.access.token}")
    private String asaasAccessToken;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        log.info("Gateway filter started");
        return (exchange, chain) -> {
            if (validator.isWebHook.test(exchange.getRequest())) {
                log.info("WebHook Route");

                try {
                    List<String> asaasTokenHeaders = exchange.getRequest().getHeaders().get("asaas-access-token");

                    if (asaasTokenHeaders == null || asaasTokenHeaders.isEmpty()) {
                        log.error("WebHook call with invalid headers.");

                        throw new SecurityException("To access this resource, you must be authenticated.");
                    }

                    String asaasToken = asaasTokenHeaders.getFirst();

                    if (!asaasAccessToken.equals(asaasToken)) {
                        log.error("WebHook call failed with invalid token!");

                        throw new SecurityException("Invalid credentials");
                    }

                } catch (Exception e) {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);

                    return exchange.getResponse().setComplete();
                }
            } else if (validator.isSecured.test(exchange.getRequest())) {
                log.info("Route is secured");

                MultiValueMap<String,HttpCookie> cookies = exchange.getRequest().getCookies();
                HttpCookie accessTokenCookie = cookies.getFirst("accessToken");

                try {
                    if (accessTokenCookie == null) {
                        log.error("To access this resource, you must be authenticated.");
                        throw new SecurityException("To access this resource, you must be authenticated.");
                    }
                    String token = accessTokenCookie.getValue();

                    Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

                    Claims claims = Jwts.parserBuilder()
                            .setSigningKey(key)
                            .build()
                            .parseClaimsJws(token)
                            .getBody();

                    String username = claims.getSubject();
                    String asaasId = claims.get("asaas_customer_id", String.class);

                    exchange = exchange.mutate()
                            .request(builder -> builder
                                    .header("customer_id", username)
                                    .header("asaas_customer_id", asaasId)
                            )
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
