package org.kinal.usuarios.security;

public record AuthenticatedUser(Long id, String email, String rol) {
}