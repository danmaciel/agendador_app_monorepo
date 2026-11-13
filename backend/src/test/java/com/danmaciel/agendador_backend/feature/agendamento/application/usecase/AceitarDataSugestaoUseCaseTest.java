package com.danmaciel.agendador_backend.feature.agendamento.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.danmaciel.agendador_backend.feature.agendamento.application.dto.AgendamentoRequest;
import com.danmaciel.agendador_backend.feature.agendamento.domain.entity.Agendamento;
import com.danmaciel.agendador_backend.feature.agendamento.domain.entity.StatusAgendamento;
import com.danmaciel.agendador_backend.feature.agendamento.domain.repository.AgendamentoRepository;
import com.danmaciel.agendador_backend.feature.agendamento.infrastructure.lock.AgendaWriteLock;
import com.danmaciel.agendador_backend.feature.servico.domain.entity.Servico;
import com.danmaciel.agendador_backend.feature.servico.domain.repository.ServicoRepository;
import com.danmaciel.agendador_backend.feature.usuario.domain.entity.Usuario;
import com.danmaciel.agendador_backend.feature.usuario.domain.repository.UsuarioRepository;
import com.danmaciel.agendador_backend.shared.exception.BusinessException;

@ExtendWith(MockitoExtension.class)
class AceitarDataSugestaoUseCaseTest {

    @Mock private AgendamentoRepository agendamentoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ServicoRepository servicoRepository;
    @Mock private AgendaWriteLock agendaWriteLock;

    private AceitarDataSugestaoUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new AceitarDataSugestaoUseCase(
                agendamentoRepository, usuarioRepository, servicoRepository, agendaWriteLock);
    }

    @Test
    void deveRejeitarMultiplosPendentesSemAlterarNenhumAgendamento() {
        Usuario usuario = usuario();
        Servico servico = servico();
        LocalDate data = LocalDate.now().plusWeeks(2);
        Agendamento primeiro = agendamento(usuario, data, LocalTime.of(9, 0), 10L);
        Agendamento segundo = agendamento(usuario, data.plusDays(1), LocalTime.of(10, 0), 11L);
        AgendamentoRequest request = new AgendamentoRequest(1L, data, LocalTime.of(9, 0), Set.of(1L));
        prepararBusca(usuario, servico, data, List.of(primeiro, segundo));

        BusinessException erro = assertThrows(BusinessException.class, () -> useCase.execute(request));

        assertEquals("Há mais de um agendamento pendente nesta semana; não é possível identificar qual deve ser atualizado",
                erro.getMessage());
        verify(agendamentoRepository, never()).save(any(Agendamento.class));
        assertEquals(Set.of(), primeiro.getServicos());
        assertEquals(Set.of(), segundo.getServicos());
    }

    @Test
    void deveManterAceiteQuandoExisteUmUnicoPendente() {
        Usuario usuario = usuario();
        Servico servico = servico();
        LocalDate data = LocalDate.now().plusWeeks(2);
        Agendamento pendente = agendamento(usuario, data, LocalTime.of(9, 0), 10L);
        AgendamentoRequest request = new AgendamentoRequest(1L, data, LocalTime.of(9, 0), Set.of(1L));
        prepararBusca(usuario, servico, data, List.of(pendente));
        when(agendamentoRepository.findByDataAndAtivoTrue(data)).thenReturn(List.of(pendente));
        when(agendamentoRepository.save(pendente)).thenReturn(pendente);

        var resposta = useCase.execute(request);

        assertEquals(10L, resposta.id());
        assertEquals(Set.of(servico), pendente.getServicos());
        assertEquals(30, pendente.getTempoTotal());
        verify(agendamentoRepository).save(pendente);
    }

    private void prepararBusca(Usuario usuario, Servico servico, LocalDate data,
            List<Agendamento> encontrados) {
        LocalDate inicio = data.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate fim = data.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(servicoRepository.findAllByIdAndAtivoTrue(Set.of(1L))).thenReturn(Set.of(servico));
        when(agendamentoRepository.findByUsuarioIdAndDataBetweenAndStatusAndAtivoTrue(
                1L, inicio, fim, StatusAgendamento.PENDENTE)).thenReturn(encontrados);
    }

    private Usuario usuario() {
        Usuario usuario = new Usuario("cliente", "senha", "Cliente");
        usuario.setId(1L);
        return usuario;
    }

    private Servico servico() {
        Servico servico = new Servico("Corte", "Corte", new BigDecimal("50.00"), 30);
        servico.setId(1L);
        return servico;
    }

    private Agendamento agendamento(Usuario usuario, LocalDate data, LocalTime hora, Long id) {
        Agendamento agendamento = new Agendamento(usuario, data, hora);
        agendamento.setId(id);
        return agendamento;
    }
}
