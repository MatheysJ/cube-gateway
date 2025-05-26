package com.cube.gateway.exceptions;

import com.cube.gateway.enums.ExceptionCode;

public class UnauthorizedException extends BusinessException {
    public UnauthorizedException(ExceptionCode exceptionCode) {
        super(exceptionCode);
    }
}

