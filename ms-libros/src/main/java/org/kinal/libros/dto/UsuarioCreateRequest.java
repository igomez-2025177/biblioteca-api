package org.kinal.libros.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.kinal.libros.entity.Rol;

public record UsuarioCreateRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede pasar de 100 caracteres")
        String nombre,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene formato valido")
        @Size(max = 120, message = "El email no puede pasar de 120 caracteres")
        String email,

        @NotBlank(message = "La contrasena es obligatoria")
        @Size(min = 8, max = 60, message = "La contrasena debe tener entre 8 y 60 caracteres")
        String password,

        @NotNull(message = "El rol es obligatorio (ADMIN, BIBLIOTECARIO o LECTOR)")
        Rol rol
) {
}