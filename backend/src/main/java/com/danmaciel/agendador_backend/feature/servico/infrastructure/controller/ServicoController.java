package com.danmaciel.agendador_backend.feature.servico.infrastructure.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.danmaciel.agendador_backend.feature.servico.application.dto.ServicoRequest;
import com.danmaciel.agendador_backend.feature.servico.application.dto.ServicoResponse;
import com.danmaciel.agendador_backend.feature.servico.application.usecase.AtualizarServicoUseCase;
import com.danmaciel.agendador_backend.feature.servico.application.usecase.BuscarServicoPorIdUseCase;
import com.danmaciel.agendador_backend.feature.servico.application.usecase.CriarServicoUseCase;
import com.danmaciel.agendador_backend.feature.servico.application.usecase.DeletarServicoUseCase;
import com.danmaciel.agendador_backend.feature.servico.application.usecase.ListarServicosUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/servico")
@Tag(name = "Serviços", description = "Endpoints para gerenciamento de serviços")
public class ServicoController {

    private final CriarServicoUseCase criarServicoUseCase;
    private final BuscarServicoPorIdUseCase buscarServicoPorIdUseCase;
    private final ListarServicosUseCase listarServicosUseCase;
    private final AtualizarServicoUseCase atualizarServicoUseCase;
    private final DeletarServicoUseCase deletarServicoUseCase;

    public ServicoController(CriarServicoUseCase criarServicoUseCase,
            BuscarServicoPorIdUseCase buscarServicoPorIdUseCase,
            ListarServicosUseCase listarServicosUseCase,
            AtualizarServicoUseCase atualizarServicoUseCase,
            DeletarServicoUseCase deletarServicoUseCase) {
        this.criarServicoUseCase = criarServicoUseCase;
        this.buscarServicoPorIdUseCase = buscarServicoPorIdUseCase;
        this.listarServicosUseCase = listarServicosUseCase;
        this.atualizarServicoUseCase = atualizarServicoUseCase;
        this.deletarServicoUseCase = deletarServicoUseCase;
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Criar serviço", description = "Cria um novo serviço no sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Serviço criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<ServicoResponse> criar(@Valid @RequestBody ServicoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(criarServicoUseCase.execute(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar serviço por ID", description = "Retorna os dados de um serviço específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Serviço encontrado"),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
    })
    public ResponseEntity<ServicoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(buscarServicoPorIdUseCase.execute(id));
    }

    @GetMapping
    @Operation(summary = "Listar serviços", description = "Lista todos os serviços com paginação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de serviços retornada com sucesso")
    })
    public ResponseEntity<Page<ServicoResponse>> listarTodos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        PageRequest pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(listarServicosUseCase.execute(pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Atualizar serviço", description = "Atualiza os dados de um serviço existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Serviço atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
    })
    public ResponseEntity<ServicoResponse> atualizar(@PathVariable Long id,
            @Valid @RequestBody ServicoRequest request) {
        return ResponseEntity.ok(atualizarServicoUseCase.execute(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Deletar serviço", description = "Remove um serviço do sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Serviço deletado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
    })
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        deletarServicoUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
