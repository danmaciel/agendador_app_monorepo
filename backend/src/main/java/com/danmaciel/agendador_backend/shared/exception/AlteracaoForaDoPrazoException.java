package com.danmaciel.agendador_backend.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class AlteracaoForaDoPrazoException extends RuntimeException {
    public AlteracaoForaDoPrazoException() {
        super("Esta alteração só poder ser feita via telefone! Data vencimento menor que 2 dias!");
    }
}
