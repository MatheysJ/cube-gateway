package com.cube.gateway.exceptions;

import com.cube.gateway.enums.ExceptionCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ExceptionController {

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ExceptionResponse> handleUnauthorizedException(SecurityException ex) {
        log.warn("UnauthorizedException thrown with message: [{}]", ex.getMessage());

        ExceptionResponse response = buildExceptionResponse(new UnauthorizedException(ExceptionCode.UNAUTHORIZED));

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ExceptionResponse> handleUnknownException(RuntimeException ex) {
        log.error("Unhandled error thrown with message: [{}]", ex.getMessage());

        ExceptionResponse response = buildExceptionResponse(new InternalException(ExceptionCode.UNKNOWN));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private ExceptionResponse buildExceptionResponse(BusinessException ex) {
        return ExceptionResponse.builder().code(ex.getCode()).message(ex.getMessage()).build();
    }
}
