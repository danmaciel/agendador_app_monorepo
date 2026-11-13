package com.danmaciel.agendador_backend.feature.agendamento.application.usecase;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.danmaciel.agendador_backend.feature.agendamento.application.dto.AgendamentoRequest;
import com.danmaciel.agendador_backend.feature.agendamento.application.dto.AgendamentoResponse;
import com.danmaciel.agendador_backend.feature.agendamento.domain.AgendamentoHorarioPolicy;
import com.danmaciel.agendador_backend.feature.agendamento.domain.entity.Agendamento;
import com.danmaciel.agendador_backend.feature.agendamento.domain.entity.StatusAgendamento;
import com.danmaciel.agendador_backend.feature.agendamento.domain.repository.AgendamentoRepository;
import com.danmaciel.agendador_backend.feature.agendamento.infrastructure.lock.AgendaWriteLock;
import com.danmaciel.agendador_backend.feature.servico.application.dto.ServicoResponse;
import com.danmaciel.agendador_backend.feature.servico.domain.entity.Servico;
import com.danmaciel.agendador_backend.feature.servico.domain.repository.ServicoRepository;
import com.danmaciel.agendador_backend.feature.usuario.domain.entity.Usuario;
import com.danmaciel.agendador_backend.feature.usuario.domain.repository.UsuarioRepository;
import com.danmaciel.agendador_backend.shared.exception.RecursoNaoEncontradoException;
import com.danmaciel.agendador_backend.shared.exception.AgendamentoConflitoException;
import com.danmaciel.agendador_backend.shared.exception.BusinessException;

@Component
public class AceitarDataSugestaoUseCase {

    private final AgendamentoRepository agendamentoRepository;
    private final AgendaWriteLock agendaWriteLock;
    private final UsuarioRepository usuarioRepository;
    private final ServicoRepository servicoRepository;

    public AceitarDataSugestaoUseCase(AgendamentoRepository agendamentoRepository,
                                      UsuarioRepository usuarioRepository, ServicoRepository servicoRepository,
                                      AgendaWriteLock agendaWriteLock) {
        this.agendamentoRepository = agendamentoRepository;
        this.agendaWriteLock = agendaWriteLock;
        this.usuarioRepository = usuarioRepository;
        this.servicoRepository = servicoRepository;
    }

    @Transactional
    public AgendamentoResponse execute(AgendamentoRequest request) {
        agendaWriteLock.acquire();
        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));

        Set<Servico> servicosNovos = new HashSet<>(servicoRepository.findAllByIdAndAtivoTrue(new HashSet<>(request.servicoIds())));
        if (servicosNovos.size() != request.servicoIds().size()) {
            throw new RecursoNaoEncontradoException("Um ou mais serviços não encontrados");
        }

        LocalDate dataInicioSemana = request.data().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate dataFimSemana = request.data().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        List<Agendamento> agendamentosSemana = agendamentoRepository
                .findByUsuarioIdAndDataBetweenAndStatusAndAtivoTrue(usuario.getId(), dataInicioSemana, dataFimSemana, StatusAgendamento.PENDENTE);

        if (agendamentosSemana.isEmpty()) {
            throw new RecursoNaoEncontradoException("Nenhum agendamento encontrado nesta semana");
        }

        if (agendamentosSemana.size() > 1) {
            throw new BusinessException(
                    "Há mais de um agendamento pendente nesta semana; selecione um agendamento específico");
        }

        Agendamento agendamentoExistente = agendamentosSemana.get(0);

        // Calculate the proposed duration before mutating the managed entity.
        Set<Servico> servicosCombinados = new HashSet<>(agendamentoExistente.getServicos());
        servicosCombinados.addAll(servicosNovos);
        int tempoTotal = servicosCombinados.stream()
                .mapToInt(Servico::getTempoExecucao)
                .sum();

        AgendamentoHorarioPolicy.validarIntervaloNoMesmoDia(agendamentoExistente.getHorario(), tempoTotal);

        LocalTime inicio = agendamentoExistente.getHorario();
        LocalTime fim = inicio.plusMinutes(tempoTotal);
        Long idAtual = agendamentoExistente.getId();
        boolean conflito = agendamentoRepository
                .findByDataAndAtivoTrue(agendamentoExistente.getData()).stream()
                .filter(outro -> !outro.getId().equals(idAtual))
                .anyMatch(outro -> inicio.isBefore(outro.getHorario().plusMinutes(outro.getTempoTotal()))
                        && fim.isAfter(outro.getHorario()));
        if (conflito) {
            throw new AgendamentoConflitoException(null);
        }

        agendamentoExistente.setServicos(servicosCombinados);
        agendamentoExistente.setTempoTotal(tempoTotal);

        agendamentoExistente = agendamentoRepository.save(agendamentoExistente);
        return toResponse(agendamentoExistente);
    }

    private AgendamentoResponse toResponse(Agendamento agendamento) {
        return new AgendamentoResponse(
                agendamento.getId(),
                agendamento.getUsuario().getId(),
                agendamento.getUsuario().getNome(),
                agendamento.getServicos().stream()
                        .map(s -> new ServicoResponse(s.getId(), s.getNome(), s.getDescricao(), s.getValor(), s.getTempoExecucao()))
                        .collect(Collectors.toSet()),
                agendamento.getData(),
                agendamento.getHorario(),
                agendamento.getStatus(),
                agendamento.getTempoTotal()
        );
    }
}
