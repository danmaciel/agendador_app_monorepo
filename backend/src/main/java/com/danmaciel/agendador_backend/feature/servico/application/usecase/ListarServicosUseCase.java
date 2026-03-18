package com.danmaciel.agendador_backend.feature.servico.application.usecase;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.danmaciel.agendador_backend.feature.servico.application.dto.ServicoResponse;
import com.danmaciel.agendador_backend.feature.servico.domain.entity.Servico;
import com.danmaciel.agendador_backend.feature.servico.domain.repository.ServicoRepository;

@Component
public class ListarServicosUseCase {

    private final ServicoRepository servicoRepository;

    public ListarServicosUseCase(ServicoRepository servicoRepository) {
        this.servicoRepository = servicoRepository;
    }

    public Page<ServicoResponse> execute(Pageable pageable) {
        return servicoRepository.findAllAtivos(pageable)
                .map(this::toResponse);
    }

    private ServicoResponse toResponse(Servico servico) {
        return new ServicoResponse(
                servico.getId(),
                servico.getNome(),
                servico.getDescricao(),
                servico.getValor(),
                servico.getTempoExecucao()
        );
    }
}
