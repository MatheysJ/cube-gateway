package com.cube.gateway.factory;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;

public class TokenFactory {
    static private final String secretMock = "cube-gateway-test-secret-with-32-bytes!";
    static private final String otherSecretMock = "another-secret-that-is-also-32-bytes-long";
    static private final String asaasAccessTokenMock = "asaas-webhook-test-token";
    static private final String customerIdMock = "123";
    static private final String asaasCustomerIdMock = "cus_456";

    static public String getMockOfSecret() {
        return secretMock;
    }

    static public String getMockOfAsaasAccessToken() {
        return asaasAccessTokenMock;
    }

    static public String getMockOfCustomerId() {
        return customerIdMock;
    }

    static public String getMockOfAsaasCustomerId() {
        return asaasCustomerIdMock;
    }

    static public String getMockOfValidToken() {
        return buildToken(secretMock, Instant.now().plusSeconds(1800));
    }

    static public String getMockOfExpiredToken() {
        return buildToken(secretMock, Instant.now().minusSeconds(60));
    }

    static public String getMockOfTokenSignedWithOtherSecret() {
        return buildToken(otherSecretMock, Instant.now().plusSeconds(1800));
    }

    static private String buildToken(String secret, Instant expiresAt) {
        Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setSubject(customerIdMock)
                .claim("asaas_customer_id", asaasCustomerIdMock)
                .setExpiration(Date.from(expiresAt))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
