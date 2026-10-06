package org.kinal.usuarios.dto;

import jakarta.validation.constraints.NotNull;
import org.kinal.usuarios.entity.Rol;

public record CambiarRolRequest(
        @NotNull(message = "El rol es obligatorio (ADMIN, BIBLIOTECARIO o LECTOR)")
        Rol rol
) {
}