package org.kinal.libros.dto;

import org.kinal.libros.entity.Libro;

public record LibroResponse(
        Long id,
        String isbn,
        String titulo,
        String autor,
        String categoria,
        Integer stockTotal,
        Integer stockDisponible,
        Integer prestados,
        boolean disponible
) {
    public static LibroResponse of(Libro l) {
        return new LibroResponse(
                l.getId(),
                l.getIsbn(),
                l.getTitulo(),
                l.getAutor(),
                l.getCategoria(),
                l.getStockTotal(),
                l.getStockDisponible(),
                l.getPrestados(),
                l.getStockDisponible() > 0
        );
    }
}