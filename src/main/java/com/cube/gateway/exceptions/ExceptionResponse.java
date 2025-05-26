package com.cube.gateway.exceptions;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExceptionResponse {
    private String code;

    private String message;
}
