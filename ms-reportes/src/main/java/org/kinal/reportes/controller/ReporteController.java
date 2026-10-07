package org.kinal.reportes.controller;

import org.kinal.reportes.dto.CategoriaReporteResponse;
import org.kinal.reportes.dto.LibroRankingResponse;
import org.kinal.reportes.dto.PageResponse;
import org.kinal.reportes.dto.ResumenResponse;
import org.kinal.reportes.dto.UsuarioRankingResponse;
import org.kinal.reportes.dto.UsuarioSancionadoResponse;
import org.kinal.reportes.service.ReporteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// todos los reportes son solo para ADMIN (por eso el @PreAuthorize va en la clase)
@RestController
@RequestMapping("/api/v1/reportes")
@PreAuthorize("hasRole('ADMIN')")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    // numeros generales: libros, ejemplares, usuarios y prestamos
    @GetMapping("/resumen")
    public ResponseEntity<ResumenResponse> resumen() {
        return ResponseEntity.ok(reporteService.resumen());
    }

    // ?top=5 para pedir solo los 5 primeros (por defecto 10, maximo 100)
    @GetMapping("/libros-mas-prestados")
    public ResponseEntity<List<LibroRankingResponse>> librosMasPrestados(
            @RequestParam(name = "top", defaultValue = "10") int top) {
        return ResponseEntity.ok(reporteService.librosMasPrestados(top));
    }

    @GetMapping("/usuarios-con-mas-prestamos")
    public ResponseEntity<List<UsuarioRankingResponse>> usuariosConMasPrestamos(
            @RequestParam(name = "top", defaultValue = "10") int top) {
        return ResponseEntity.ok(reporteService.usuariosConMasPrestamos(top));
    }

    @GetMapping("/prestamos-por-categoria")
    public ResponseEntity<List<CategoriaReporteResponse>> prestamosPorCategoria() {
        return ResponseEntity.ok(reporteService.prestamosPorCategoria());
    }

    // paginado igual que los demas listados
    @GetMapping("/usuarios-sancionados")
    public ResponseEntity<PageResponse<UsuarioSancionadoResponse>> usuariosSancionados(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return ResponseEntity.ok(reporteService.usuariosSancionados(page, size));
    }
}