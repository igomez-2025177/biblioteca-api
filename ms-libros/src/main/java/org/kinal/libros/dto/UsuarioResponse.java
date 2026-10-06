package org.kinal.libros.dto;

import org.kinal.libros.entity.Usuario;

import java.time.LocalDateTime;

public record UsuarioResponse(
        Long id,
        String nombre,
        String email,
        String rol,
        String estado,
        LocalDateTime fechaRegistro
) {
    public static UsuarioResponse of(Usuario u) {
        return new UsuarioResponse(
                u.getId(),
                u.getNombre(),
                u.getEmail(),
                u.getRol().name(),
                u.getEstado().name(),
                u.getFechaRegistro()
        );
    }
}