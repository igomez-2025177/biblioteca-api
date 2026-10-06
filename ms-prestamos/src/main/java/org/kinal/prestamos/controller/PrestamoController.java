package org.kinal.prestamos.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.kinal.prestamos.dto.PageResponse;
import org.kinal.prestamos.dto.PrestamoRequest;
import org.kinal.prestamos.dto.PrestamoResponse;
import org.kinal.prestamos.entity.EstadoPrestamo;
import org.kinal.prestamos.security.AuthenticatedUser;
import org.kinal.prestamos.service.PrestamoService;
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
@RequestMapping("/api/v1/prestamos")
@RequiredArgsConstructor
public class PrestamoController {

    private final PrestamoService prestamoService;

    @PostMapping
    @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'ADMIN')")
    public ResponseEntity<PrestamoResponse> registrarPrestamo(@Valid @RequestBody PrestamoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(prestamoService.registrarPrestamo(request));
    }

    @PatchMapping("/{id}/devolucion")
    @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'ADMIN')")
    public ResponseEntity<PrestamoResponse> registrarDevolucion(@PathVariable("id") Long id) {
        return ResponseEntity.ok(prestamoService.registrarDevolucion(id));
    }

    // el id sale del token, un lector solo puede ver lo suyo
    @GetMapping("/mis-prestamos")
    @PreAuthorize("hasRole('LECTOR')")
    public ResponseEntity<PageResponse<PrestamoResponse>> misPrestamos(
            @AuthenticationPrincipal AuthenticatedUser usuario,
            @PageableDefault(size = 20, sort = "fechaPrestamo", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(prestamoService.misPrestamos(usuario.id(), pageable));
    }

    @GetMapping("/atrasados")
    @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'ADMIN')")
    public ResponseEntity<PageResponse<PrestamoResponse>> atrasados(
            @PageableDefault(size = 20, sort = "fechaDevolucionEsperada", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(prestamoService.atrasados(pageable));
    }

    // extra: consulta general de prestamos para el bibliotecario
    @GetMapping
    @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'ADMIN')")
    public ResponseEntity<PageResponse<PrestamoResponse>> listar(
            @RequestParam(name = "estado", required = false) EstadoPrestamo estado,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(prestamoService.listar(estado, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'ADMIN')")
    public ResponseEntity<PrestamoResponse> obtener(@PathVariable("id") Long id) {
        return ResponseEntity.ok(prestamoService.obtener(id));
    }
}