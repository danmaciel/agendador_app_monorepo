package com.danmaciel.agendador_backend.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class LoginInvalidoException extends RuntimeException {
    public LoginInvalidoException() {
        super("Login ou senha inválidos");
    }
}
