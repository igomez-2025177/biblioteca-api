package org.kinal.libros.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.kinal.libros.dto.CambiarEstadoRequest;
import org.kinal.libros.dto.CambiarRolRequest;
import org.kinal.libros.dto.PageResponse;
import org.kinal.libros.dto.UsuarioCreateRequest;
import org.kinal.libros.dto.UsuarioResponse;
import org.kinal.libros.entity.EstadoUsuario;
import org.kinal.libros.entity.Rol;
import org.kinal.libros.security.AuthenticatedUser;
import org.kinal.libros.service.UsuarioService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    // cualquier usuario logueado puede ver sus propios datos
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> miPerfil(@AuthenticationPrincipal AuthenticatedUser usuario) {
        return ResponseEntity.ok(usuarioService.obtener(usuario.id()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<UsuarioResponse>> listar(
            @RequestParam(name = "rol", required = false) Rol rol,
            @RequestParam(name = "estado", required = false) EstadoUsuario estado,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(usuarioService.listar(rol, estado, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> obtener(@PathVariable("id") Long id) {
        return ResponseEntity.ok(usuarioService.obtener(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crear(request));
    }

    @PatchMapping("/{id}/rol")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> cambiarRol(@PathVariable("id") Long id,
                                                      @Valid @RequestBody CambiarRolRequest request,
                                                      @AuthenticationPrincipal AuthenticatedUser admin) {
        return ResponseEntity.ok(usuarioService.cambiarRol(id, request.rol(), admin.id()));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> cambiarEstado(@PathVariable("id") Long id,
                                                         @Valid @RequestBody CambiarEstadoRequest request,
                                                         @AuthenticationPrincipal AuthenticatedUser admin) {
        return ResponseEntity.ok(usuarioService.cambiarEstado(id, request.estado(), admin.id()));
    }
}