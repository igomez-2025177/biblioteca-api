package org.kinal.usuarios.dto;

import jakarta.validation.constraints.NotNull;
import org.kinal.usuarios.entity.EstadoUsuario;

public record CambiarEstadoRequest(
        @NotNull(message = "El estado es obligatorio (ACTIVO o SANCIONADO)")
        EstadoUsuario estado
) {
}