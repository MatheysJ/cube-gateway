package com.cube.gateway.exceptions;

import com.cube.gateway.enums.ExceptionCode;

public class InternalException extends BusinessException {

    public InternalException(ExceptionCode exceptionCode) {
        super(exceptionCode);
    }

}
