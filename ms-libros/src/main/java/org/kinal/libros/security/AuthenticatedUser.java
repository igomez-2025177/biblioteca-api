package org.kinal.libros.security;

public record AuthenticatedUser(Long id, String email, String rol) {
}