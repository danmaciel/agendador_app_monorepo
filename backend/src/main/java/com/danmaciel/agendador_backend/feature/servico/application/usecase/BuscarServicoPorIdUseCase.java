package com.danmaciel.agendador_backend.feature.servico.application.usecase;

import org.springframework.stereotype.Component;

import com.danmaciel.agendador_backend.feature.servico.application.dto.ServicoResponse;
import com.danmaciel.agendador_backend.feature.servico.domain.entity.Servico;
import com.danmaciel.agendador_backend.feature.servico.domain.repository.ServicoRepository;
import com.danmaciel.agendador_backend.shared.exception.RecursoNaoEncontradoException;

@Component
public class BuscarServicoPorIdUseCase {

    private final ServicoRepository servicoRepository;

    public BuscarServicoPorIdUseCase(ServicoRepository servicoRepository) {
        this.servicoRepository = servicoRepository;
    }

    public ServicoResponse execute(Long id) {
        Servico servico = servicoRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Serviço não encontrado"));
        return toResponse(servico);
    }

    private ServicoResponse toResponse(Servico servico) {
        return new ServicoResponse(
                servico.getId(),
                servico.getNome(),
                servico.getDescricao(),
                servico.getValor(),
                servico.getTempoExecucao());
    }
}
