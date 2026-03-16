package com.danmaciel.agendador_backend.shared.exception;

import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
public class AgendamentoConflitoException extends RuntimeException {

    private LocalDate dataSugerida;

    public AgendamentoConflitoException(LocalDate dataSugerida) {
        super(determinarMensagem(dataSugerida));
        this.dataSugerida = dataSugerida;
    }

    private static String determinarMensagem(LocalDate dataSugerida) {
        if (dataSugerida != null) {
            return "Você já possui agendamento nesta semana. Data sugerida: " + dataSugerida;
        }
        return "Já existe agendamento para este horário";
    }

    public LocalDate getDataSugerida() {
        return dataSugerida;
    }
}
