package com.danmaciel.agendador_backend.feature.agendamento.infrastructure.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.danmaciel.agendador_backend.feature.agendamento.application.dto.AgendamentoRequest;
import com.danmaciel.agendador_backend.feature.agendamento.application.dto.AgendamentoResponse;
import com.danmaciel.agendador_backend.feature.agendamento.application.dto.AprovarAgendamentoRequest;
import com.danmaciel.agendador_backend.feature.agendamento.application.dto.HistoricoResponse;
import com.danmaciel.agendador_backend.feature.agendamento.application.usecase.AceitarDataSugestaoUseCase;
import com.danmaciel.agendador_backend.feature.agendamento.application.usecase.AprovarAgendamentoUseCase;
import com.danmaciel.agendador_backend.feature.agendamento.application.usecase.AtualizarAgendamentoUseCase;
import com.danmaciel.agendador_backend.feature.agendamento.application.usecase.BuscarAgendamentoPorIdUseCase;
import com.danmaciel.agendador_backend.feature.agendamento.application.usecase.BuscarHistoricoUseCase;
import com.danmaciel.agendador_backend.feature.agendamento.application.usecase.CriarAgendamentoForcadoUseCase;
import com.danmaciel.agendador_backend.feature.agendamento.application.usecase.CriarAgendamentoUseCase;
import com.danmaciel.agendador_backend.feature.agendamento.application.usecase.DeletarAgendamentoUseCase;
import com.danmaciel.agendador_backend.feature.agendamento.application.usecase.GerarGraficoSemanalUseCase;
import com.danmaciel.agendador_backend.feature.agendamento.application.usecase.ListarAgendamentosUseCase;
import com.danmaciel.agendador_backend.feature.agendamento.application.usecase.RejeitarAgendamentoUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/agendamento")
@Tag(name = "Agendamentos", description = "Endpoints para gerenciamento de agendamentos")
public class AgendamentoController {

    private final CriarAgendamentoUseCase criarAgendamentoUseCase;
    private final CriarAgendamentoForcadoUseCase criarAgendamentoForcadoUseCase;
    private final AceitarDataSugestaoUseCase aceitarSugestaoUseCase;
    private final BuscarAgendamentoPorIdUseCase buscarAgendamentoPorIdUseCase;
    private final BuscarHistoricoUseCase buscarHistoricoUseCase;
    private final ListarAgendamentosUseCase listarAgendamentosUseCase;
    private final AtualizarAgendamentoUseCase atualizarAgendamentoUseCase;
    private final DeletarAgendamentoUseCase deletarAgendamentoUseCase;
    private final AprovarAgendamentoUseCase aprovarAgendamentoUseCase;
    private final RejeitarAgendamentoUseCase rejeitarAgendamentoUseCase;
    private final GerarGraficoSemanalUseCase gerarGraficoSemanalUseCase;

    public AgendamentoController(CriarAgendamentoUseCase criarAgendamentoUseCase,
            CriarAgendamentoForcadoUseCase criarAgendamentoForcadoUseCase,
            AceitarDataSugestaoUseCase aceitarSugestaoUseCase,
            BuscarAgendamentoPorIdUseCase buscarAgendamentoPorIdUseCase,
            BuscarHistoricoUseCase buscarHistoricoUseCase,
            ListarAgendamentosUseCase listarAgendamentosUseCase,
            AtualizarAgendamentoUseCase atualizarAgendamentoUseCase,
            DeletarAgendamentoUseCase deletarAgendamentoUseCase,
            AprovarAgendamentoUseCase aprovarAgendamentoUseCase,
            RejeitarAgendamentoUseCase rejeitarAgendamentoUseCase,
            GerarGraficoSemanalUseCase gerarGraficoSemanalUseCase) {
        this.criarAgendamentoUseCase = criarAgendamentoUseCase;
        this.criarAgendamentoForcadoUseCase = criarAgendamentoForcadoUseCase;
        this.aceitarSugestaoUseCase = aceitarSugestaoUseCase;
        this.buscarAgendamentoPorIdUseCase = buscarAgendamentoPorIdUseCase;
        this.buscarHistoricoUseCase = buscarHistoricoUseCase;
        this.listarAgendamentosUseCase = listarAgendamentosUseCase;
        this.atualizarAgendamentoUseCase = atualizarAgendamentoUseCase;
        this.deletarAgendamentoUseCase = deletarAgendamentoUseCase;
        this.aprovarAgendamentoUseCase = aprovarAgendamentoUseCase;
        this.rejeitarAgendamentoUseCase = rejeitarAgendamentoUseCase;
        this.gerarGraficoSemanalUseCase = gerarGraficoSemanalUseCase;
    }

    @PostMapping
    @Operation(summary = "Criar agendamento", description = "Cria um novo agendamento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Agendamento criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<AgendamentoResponse> criar(@Valid @RequestBody AgendamentoRequest request) {
        return ResponseEntity.ok(criarAgendamentoUseCase.execute(request));
    }

    @PostMapping("/criar-forcado")
    @Operation(summary = "Criar agendamento forçado", description = "Cria um agendamento ignorando validações de horário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Agendamento forçado criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<AgendamentoResponse> criarForcado(@Valid @RequestBody AgendamentoRequest request) {
        return ResponseEntity.ok(criarAgendamentoForcadoUseCase.execute(request));
    }

    @PostMapping("/aceitar-data-sugestao")
    @Operation(summary = "Aceitar data sugestiva", description = "Cria um agendamento aceitando a data sugerida pelo sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Agendamento criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<AgendamentoResponse> aceitarDataSugestiva(@Valid @RequestBody AgendamentoRequest request) {
        return ResponseEntity.ok(aceitarSugestaoUseCase.execute(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar agendamento por ID", description = "Retorna os dados de um agendamento específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Agendamento encontrado"),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado")
    })
    public ResponseEntity<AgendamentoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(buscarAgendamentoPorIdUseCase.execute(id));
    }

    @GetMapping("/meus-agendamentos")
    @Operation(summary = "Listar meus agendamentos", description = "Lista os agendamentos do usuário logado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de agendamentos retornada com sucesso")
    })
    public ResponseEntity<Page<AgendamentoResponse>> listarMeusAgendamentos(
            @RequestParam Long usuarioId,
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "data") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        PageRequest pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(listarAgendamentosUseCase.executePorUsuario(usuarioId, dataInicio, dataFim, pageable));
    }

    @GetMapping("/historico")
    @Operation(summary = "Listar histórico do cliente", description = "Lista o histórico de agendamentos do cliente com informações adicionais (valor total,区分 passado/futuro)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Histórico retornado com sucesso")
    })
    public ResponseEntity<Page<HistoricoResponse>> listarHistorico(
            @RequestParam Long usuarioId,
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "data") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        PageRequest pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(buscarHistoricoUseCase.execute(usuarioId, dataInicio, dataFim, pageable));
    }

    @GetMapping("/historico/aprovados-por-dia")
    @Operation(summary = "Listar histórico agrupado por dia", description = "Lista agendamentos APROVADOS do cliente agrupados por data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Histórico retornado com sucesso")
    })
    public ResponseEntity<Map<LocalDate, List<HistoricoResponse>>> listarHistoricoAgrupadoPorDia(
            @RequestParam Long usuarioId,
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim) {
        return ResponseEntity.ok(buscarHistoricoUseCase.buscarAgrupadosPorDia(usuarioId, dataInicio, dataFim));
    }

    @GetMapping("/pendentes")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Listar agendamentos pendentes", description = "Lista todos os agendamentos com status PENDENTE (apenas admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de agendamentos pendentes retornada com sucesso")
    })
    public ResponseEntity<Page<AgendamentoResponse>> listarPendentes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "data") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        PageRequest pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(listarAgendamentosUseCase.executePendentes(pageable));
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Listar todos os agendamentos", description = "Lista todos os agendamentos do sistema com paginação (apenas admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de agendamentos retornada com sucesso")
    })
    public ResponseEntity<Page<AgendamentoResponse>> listarTodos(
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        PageRequest pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(listarAgendamentosUseCase.executeAdmin(dataInicio, dataFim, pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar agendamento", description = "Atualiza os dados de um agendamento existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Agendamento atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado")
    })
    public ResponseEntity<AgendamentoResponse> atualizar(@PathVariable Long id,
            @Valid @RequestBody AgendamentoRequest request) {
        return ResponseEntity.ok(atualizarAgendamentoUseCase.execute(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Deletar agendamento", description = "Remove um agendamento do sistema (apenas admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Agendamento deletado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado")
    })
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        deletarAgendamentoUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/aprovar")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Aprovar agendamento", description = "Aprova um agendamento com serviços específicos (apenas admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Agendamento aprovado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado"),
            @ApiResponse(responseCode = "422", description = "Conflito de horário")
    })
    public ResponseEntity<AgendamentoResponse> aprovar(@PathVariable Long id,
            @Valid @RequestBody AprovarAgendamentoRequest request) {
        return ResponseEntity.ok(aprovarAgendamentoUseCase.execute(id, request));
    }

    @PatchMapping("/{id}/rejeitar")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Rejeitar agendamento", description = "Rejeita um agendamento (apenas admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Agendamento rejeitado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado")
    })
    public ResponseEntity<AgendamentoResponse> rejeitar(@PathVariable Long id) {
        return ResponseEntity.ok(rejeitarAgendamentoUseCase.execute(id));
    }

    @GetMapping("/relatorio")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Gerar relatório semanal", description = "Gera um gráfico semanal de agendamentos (apenas admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso")
    })
    public ResponseEntity<Map<String, Object>> graficoSemanal(
            @RequestParam LocalDate dataInicio,
            @RequestParam LocalDate dataFim) {
        return ResponseEntity.ok(gerarGraficoSemanalUseCase.execute(dataInicio, dataFim));
    }
}
