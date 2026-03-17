package com.danmaciel.agendador_backend.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
public class AgendaConflitoException extends RuntimeException {

    public AgendaConflitoException(String mensagem) {
        super(mensagem);
    }
}
