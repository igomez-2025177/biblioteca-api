package org.kinal.libros.dto;

import jakarta.validation.constraints.NotNull;
import org.kinal.libros.entity.Rol;

public record CambiarRolRequest(
        @NotNull(message = "El rol es obligatorio (ADMIN, BIBLIOTECARIO o LECTOR)")
        Rol rol
) {
}