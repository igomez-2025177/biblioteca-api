package org.kinal.libros.dto;

import org.kinal.libros.entity.Usuario;

public record AuthResponse(
        String token,
        String tipo,
        long expiraEnMs,
        Long usuarioId,
        String nombre,
        String email,
        String rol,
        String estado
) {
    public static AuthResponse of(Usuario usuario, String token, long expiraEnMs) {
        return new AuthResponse(
                token,
                "Bearer",
                expiraEnMs,
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol().name(),
                usuario.getEstado().name()
        );
    }
}