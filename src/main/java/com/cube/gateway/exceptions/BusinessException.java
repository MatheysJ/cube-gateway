package com.cube.gateway.exceptions;

import com.cube.gateway.enums.ExceptionCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final String code;

    public BusinessException(ExceptionCode exceptionCode) {
        super(exceptionCode.getMessage());

        this.code = exceptionCode.name();
    }

}
