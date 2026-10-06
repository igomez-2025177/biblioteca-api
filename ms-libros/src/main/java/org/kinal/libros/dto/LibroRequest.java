package org.kinal.libros.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LibroRequest(
        @NotBlank(message = "El ISBN es obligatorio")
        @Size(max = 20, message = "El ISBN no puede pasar de 20 caracteres")
        String isbn,

        @NotBlank(message = "El titulo es obligatorio")
        @Size(max = 200, message = "El titulo no puede pasar de 200 caracteres")
        String titulo,

        @NotBlank(message = "El autor es obligatorio")
        @Size(max = 150, message = "El autor no puede pasar de 150 caracteres")
        String autor,

        @NotBlank(message = "La categoria es obligatoria")
        @Size(max = 80, message = "La categoria no puede pasar de 80 caracteres")
        String categoria,

        @NotNull(message = "El stock total es obligatorio")
        @Min(value = 0, message = "El stock no puede ser negativo")
        @Max(value = 10000, message = "El stock no puede pasar de 10000")
        Integer stockTotal
) {
}