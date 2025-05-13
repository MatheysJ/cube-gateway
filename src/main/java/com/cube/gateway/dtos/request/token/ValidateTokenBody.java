package com.cube.gateway.dtos.request.token;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ValidateTokenBody {

    String token;

}
