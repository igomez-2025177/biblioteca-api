package org.kinal.libros.dto;

import jakarta.validation.constraints.NotNull;
import org.kinal.libros.entity.EstadoUsuario;

public record CambiarEstadoRequest(
        @NotNull(message = "El estado es obligatorio (ACTIVO o SANCIONADO)")
        EstadoUsuario estado
) {
}