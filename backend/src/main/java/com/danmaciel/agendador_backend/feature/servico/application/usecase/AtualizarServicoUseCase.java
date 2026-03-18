package com.danmaciel.agendador_backend.feature.servico.application.usecase;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.danmaciel.agendador_backend.feature.servico.application.dto.ServicoRequest;
import com.danmaciel.agendador_backend.feature.servico.application.dto.ServicoResponse;
import com.danmaciel.agendador_backend.feature.servico.domain.entity.Servico;
import com.danmaciel.agendador_backend.feature.servico.domain.repository.ServicoRepository;
import com.danmaciel.agendador_backend.shared.exception.BusinessException;
import com.danmaciel.agendador_backend.shared.exception.RecursoNaoEncontradoException;

@Component
public class AtualizarServicoUseCase {

    private final ServicoRepository servicoRepository;

    public AtualizarServicoUseCase(ServicoRepository servicoRepository) {
        this.servicoRepository = servicoRepository;
    }

    @Transactional
    public ServicoResponse execute(Long id, ServicoRequest request) {
        Servico servico = servicoRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Serviço não encontrado"));

        if (servicoRepository.existsByNomeAndAtivoTrue(request.nome()) &&
                !servico.getNome().equals(request.nome())) {
            throw new BusinessException("Nome do serviço já está em uso");
        }

        servico.setNome(request.nome());
        servico.setDescricao(request.descricao());
        servico.setValor(request.valor());
        servico.setTempoExecucao(request.tempoExecucao());
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
