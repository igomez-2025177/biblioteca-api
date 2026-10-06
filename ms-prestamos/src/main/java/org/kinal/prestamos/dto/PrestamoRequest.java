package org.kinal.prestamos.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PrestamoRequest(
        @NotNull(message = "El usuarioId es obligatorio")
        @Positive(message = "El usuarioId debe ser mayor a 0")
        Long usuarioId,

        @NotNull(message = "El libroId es obligatorio")
        @Positive(message = "El libroId debe ser mayor a 0")
        Long libroId
) {
}