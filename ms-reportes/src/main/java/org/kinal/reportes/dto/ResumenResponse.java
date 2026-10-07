package org.kinal.reportes.dto;

public record ResumenResponse(
        long totalLibros,
        long totalEjemplares,
        long ejemplaresDisponibles,
        long ejemplaresPrestados,
        long totalUsuarios,
        long usuariosSancionados,
        long prestamosActivos,
        long prestamosVencidos,
        long prestamosDevueltos
) {
}