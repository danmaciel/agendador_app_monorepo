package com.danmaciel.agendador_backend.feature.usuario.infrastructure.controller;

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

import com.danmaciel.agendador_backend.feature.usuario.application.dto.UsuarioRequest;
import com.danmaciel.agendador_backend.feature.usuario.application.dto.UsuarioResponse;
import com.danmaciel.agendador_backend.feature.usuario.application.usecase.CriarUsuarioUseCase;
import com.danmaciel.agendador_backend.feature.usuario.application.usecase.DeletarUsuarioUseCase;
import com.danmaciel.agendador_backend.feature.usuario.application.usecase.ListarUsuariosUseCase;
import com.danmaciel.agendador_backend.feature.usuario.application.usecase.PromoverUsuarioUseCase;
import com.danmaciel.agendador_backend.feature.usuario.application.usecase.BuscarUsuarioPorIdUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/usuario")
@Tag(name = "Usuários", description = "Endpoints para gerenciamento de usuários")
public class UsuarioController {

    private final CriarUsuarioUseCase criarUsuarioUseCase;
    private final BuscarUsuarioPorIdUseCase buscarUsuarioPorIdUseCase;
    private final ListarUsuariosUseCase listarUsuariosUseCase;
    private final PromoverUsuarioUseCase promoverUsuarioUseCase;
    private final DeletarUsuarioUseCase deletarUsuarioUseCase;

    public UsuarioController(CriarUsuarioUseCase criarUsuarioUseCase,
            BuscarUsuarioPorIdUseCase buscarUsuarioPorIdUseCase,
            ListarUsuariosUseCase listarUsuariosUseCase,
            PromoverUsuarioUseCase promoverUsuarioUseCase,
            DeletarUsuarioUseCase deletarUsuarioUseCase) {
        this.criarUsuarioUseCase = criarUsuarioUseCase;
        this.buscarUsuarioPorIdUseCase = buscarUsuarioPorIdUseCase;
        this.listarUsuariosUseCase = listarUsuariosUseCase;
        this.promoverUsuarioUseCase = promoverUsuarioUseCase;
        this.deletarUsuarioUseCase = deletarUsuarioUseCase;
    }

    @PostMapping
    @Operation(summary = "Criar usuário", description = "Cria um novo usuário no sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<UsuarioResponse> criar(@Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(criarUsuarioUseCase.execute(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por ID", description = "Retorna os dados de um usuário específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<UsuarioResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(buscarUsuarioPorIdUseCase.execute(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Listar usuários", description = "Lista todos os usuários com paginação")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de usuários retornada com sucesso")
    })
    public ResponseEntity<Page<UsuarioResponse>> listarTodos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        PageRequest pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(listarUsuariosUseCase.execute(pageable));
    }

    @PutMapping("/{id}/promover-admin")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Promover usuário para admin", description = "Promove um usuário ao papel de administrador")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuário promovido com sucesso"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<UsuarioResponse> promoverParaAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(promoverUsuarioUseCase.execute(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Deletar usuário", description = "Remove um usuário do sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Usuário deletado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        deletarUsuarioUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
