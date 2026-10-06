package org.kinal.prestamos.dto;

import org.kinal.prestamos.entity.EstadoPrestamo;
import org.kinal.prestamos.entity.Prestamo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record PrestamoResponse(
        Long id,
        Long usuarioId,
        String usuarioNombre,
        String usuarioEmail,
        Long libroId,
        String libroIsbn,
        String libroTitulo,
        LocalDate fechaPrestamo,
        LocalDate fechaDevolucionEsperada,
        LocalDate fechaDevolucionReal,
        String estado,
        long diasAtraso
) {
    public static PrestamoResponse of(Prestamo p) {
        return new PrestamoResponse(
                p.getId(),
                p.getUsuario().getId(),
                p.getUsuario().getNombre(),
                p.getUsuario().getEmail(),
                p.getLibro().getId(),
                p.getLibro().getIsbn(),
                p.getLibro().getTitulo(),
                p.getFechaPrestamo(),
                p.getFechaDevolucionEsperada(),
                p.getFechaDevolucionReal(),
                p.getEstado().name(),
                calcularAtraso(p)
        );
    }

    // si ya se devolvio, cuenta hasta la fecha real; si no, hasta hoy
    private static long calcularAtraso(Prestamo p) {
        LocalDate hasta = p.getEstado() == EstadoPrestamo.DEVUELTO && p.getFechaDevolucionReal() != null
                ? p.getFechaDevolucionReal()
                : LocalDate.now();
        long dias = ChronoUnit.DAYS.between(p.getFechaDevolucionEsperada(), hasta);
        return Math.max(dias, 0);
    }
}