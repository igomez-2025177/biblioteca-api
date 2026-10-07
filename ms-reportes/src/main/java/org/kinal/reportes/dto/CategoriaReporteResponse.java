package org.kinal.reportes.dto;

public record CategoriaReporteResponse(
        String categoria,
        long totalPrestamos,
        long prestamosAbiertos
) {
}