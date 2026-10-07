package org.kinal.reportes.dto;

public record UsuarioRankingResponse(
        Long usuarioId,
        String nombre,
        String email,
        String estado,
        long totalPrestamos,
        long prestamosAbiertos
) {
}