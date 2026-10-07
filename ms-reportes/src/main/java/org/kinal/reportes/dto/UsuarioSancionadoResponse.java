package org.kinal.reportes.dto;

public record UsuarioSancionadoResponse(
        Long usuarioId,
        String nombre,
        String email,
        long prestamosVencidos
) {
}