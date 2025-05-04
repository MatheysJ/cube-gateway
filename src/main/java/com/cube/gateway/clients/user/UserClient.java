package com.cube.gateway.clients.user;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("cubeMsUser")
public interface UserClient {
}
