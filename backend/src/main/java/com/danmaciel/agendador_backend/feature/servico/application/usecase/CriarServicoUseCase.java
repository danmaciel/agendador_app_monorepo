package com.danmaciel.agendador_backend.feature.servico.application.usecase;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.danmaciel.agendador_backend.feature.servico.application.dto.ServicoRequest;
import com.danmaciel.agendador_backend.feature.servico.application.dto.ServicoResponse;
import com.danmaciel.agendador_backend.feature.servico.domain.entity.Servico;
import com.danmaciel.agendador_backend.feature.servico.domain.repository.ServicoRepository;
import com.danmaciel.agendador_backend.shared.exception.BusinessException;

@Component
public class CriarServicoUseCase {

    private final ServicoRepository servicoRepository;

    public CriarServicoUseCase(ServicoRepository servicoRepository) {
        this.servicoRepository = servicoRepository;
    }

    @Transactional
    public ServicoResponse execute(ServicoRequest request) {
        if (servicoRepository.existsByNome(request.nome())) {
            throw new BusinessException("Nome do serviço já está em uso");
        }

        Servico servico = new Servico(
                request.nome(),
                request.descricao(),
                request.valor(),
                request.tempoExecucao());
        servico.setCreatedAt(LocalDateTime.now());
        servico.setUpdatedAt(LocalDateTime.now());
        servico = servicoRepository.save(servico);
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
