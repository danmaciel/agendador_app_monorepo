package com.danmaciel.agendador_backend.feature.agendamento.application.usecase;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.danmaciel.agendador_backend.feature.agendamento.application.dto.AgendamentoRequest;
import com.danmaciel.agendador_backend.feature.agendamento.application.dto.AgendamentoResponse;
import com.danmaciel.agendador_backend.feature.agendamento.domain.entity.Agendamento;
import com.danmaciel.agendador_backend.feature.agendamento.domain.entity.StatusAgendamento;
import com.danmaciel.agendador_backend.feature.agendamento.domain.repository.AgendamentoRepository;
import com.danmaciel.agendador_backend.feature.servico.application.dto.ServicoResponse;
import com.danmaciel.agendador_backend.feature.servico.domain.entity.Servico;
import com.danmaciel.agendador_backend.feature.servico.domain.repository.ServicoRepository;
import com.danmaciel.agendador_backend.feature.usuario.domain.entity.Usuario;
import com.danmaciel.agendador_backend.feature.usuario.domain.repository.UsuarioRepository;
import com.danmaciel.agendador_backend.shared.exception.AgendamentoConflitoException;
import com.danmaciel.agendador_backend.shared.exception.RecursoNaoEncontradoException;

@Component
public class CriarAgendamentoUseCase {

    private static final Logger log = LoggerFactory.getLogger(CriarAgendamentoUseCase.class);

    private final AgendamentoRepository agendamentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ServicoRepository servicoRepository;

    public CriarAgendamentoUseCase(AgendamentoRepository agendamentoRepository,
            UsuarioRepository usuarioRepository, ServicoRepository servicoRepository) {
        this.agendamentoRepository = agendamentoRepository;
        this.usuarioRepository = usuarioRepository;
        this.servicoRepository = servicoRepository;
    }

    @Transactional
    public AgendamentoResponse execute(AgendamentoRequest request) {
        log.info("Tentativa de criar agendamento para usuário {} na data {} horário {}", 
                request.usuarioId(), request.data(), request.horario());
        
        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> {
                    log.error("Usuário não encontrado: {}", request.usuarioId());
                    return new RecursoNaoEncontradoException("Usuário não encontrado");
                });

        Set<Servico> servicos = new HashSet<>(servicoRepository.findAllByIdAndAtivoTrue(new HashSet<>(request.servicoIds())));
        if (servicos.size() != request.servicoIds().size()) {
            log.error("Serviços não encontrados para agendamento");
            throw new RecursoNaoEncontradoException("Um ou mais serviços não encontrados");
        }

        int tempoTotal = servicos.stream()
                .mapToInt(Servico::getTempoExecucao)
                .sum();

        List<Agendamento> agendamentosDia = agendamentoRepository.findByDataAndAtivoTrue(request.data());
        validarConflitoHorario(agendamentosDia, request.horario(), tempoTotal);

        LocalDate dataInicioSemana = request.data().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate dataFimSemana = request.data().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        List<Agendamento> agendamentosSemana = agendamentoRepository
                .findByUsuarioIdAndDataBetweenAndStatusAndAtivoTrue(usuario.getId(), dataInicioSemana, dataFimSemana, StatusAgendamento.PENDENTE);

        if (!agendamentosSemana.isEmpty()) {
            LocalDate dataSugerida = agendamentosSemana.get(0).getData();
            log.warn("Conflito de agendamento detectado para usuário {} na semana de {}", 
                    usuario.getLogin(), dataInicioSemana);
            throw new AgendamentoConflitoException(dataSugerida);
        }

        Agendamento agendamento = new Agendamento(usuario, request.data(), request.horario());
        agendamento.setServicos(servicos);
        agendamento.setTempoTotal(tempoTotal);

        agendamento = agendamentoRepository.save(agendamento);
        
        log.info("Agendamento criado com sucesso: ID {} para usuário {}", 
                agendamento.getId(), usuario.getLogin());
        
        return toResponse(agendamento);
    }

    private void validarConflitoHorario(List<Agendamento> agendamentosDia, LocalTime horarioInicio, int duracao) {
        LocalTime horarioFim = horarioInicio.plusMinutes(duracao);

        boolean haConflito = agendamentosDia.stream()
                .anyMatch(ag -> existeConflito(horarioInicio, horarioFim, ag));

        if (haConflito) {
            log.warn("Conflito de horário detectado para data {} horário {}", 
                    agendamentosDia.isEmpty() ? null : agendamentosDia.get(0).getData(), horarioInicio);
            throw new AgendamentoConflitoException(null);
        }
    }

    private boolean existeConflito(LocalTime novoInicio, LocalTime novoFim, Agendamento existente) {
        LocalTime existenteInicio = existente.getHorario();
        LocalTime existenteFim = existenteInicio.plusMinutes(existente.getTempoTotal());
        
        boolean sobrepoe = novoInicio.isBefore(existenteFim) && novoFim.isAfter(existenteInicio);
        
        boolean novoCabeEntre = (novoInicio.isAfter(existenteInicio) || novoInicio.equals(existenteInicio))
                             && novoFim.isBefore(existenteFim);
        
        boolean novoEngloba = novoInicio.isBefore(existenteInicio) && novoFim.isAfter(existenteFim);
        
        return sobrepoe || novoCabeEntre || novoEngloba;
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
                agendamento.getTempoTotal());
    }
}
