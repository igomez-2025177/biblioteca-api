package org.kinal.reportes.dto;

public record LibroRankingResponse(
        Long libroId,
        String isbn,
        String titulo,
        String categoria,
        long totalPrestamos
) {
}