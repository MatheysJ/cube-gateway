package com.cube.gateway.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExceptionCode {
    UNAUTHORIZED("Você precisa estar logado para acessar esse recurso"),
    UNKNOWN("Erro interno, tente novamente mais tarde");

    private final String message;
}
