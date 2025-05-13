package com.cube.gateway.clients.user;

import com.cube.gateway.dtos.request.token.ValidateTokenBody;
import com.cube.gateway.dtos.response.token.ValidateTokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("cubeMsUser")
public interface UserClient {

    @PostMapping("/v1/auth/validate")
    ValidateTokenResponse validateToken(@RequestBody ValidateTokenBody body);

}
