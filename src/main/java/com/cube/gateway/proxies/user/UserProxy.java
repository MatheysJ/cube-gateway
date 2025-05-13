package com.cube.gateway.proxies.user;

import com.cube.gateway.clients.user.UserClient;
import com.cube.gateway.dtos.request.token.ValidateTokenBody;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UserProxy {

    private final UserClient userClient;

    public String getUsernameByToken(String token) {
        return userClient.validateToken(new ValidateTokenBody(token)).getUsername();
    }

}
