package org.kinal.prestamos.security;

public record AuthenticatedUser(Long id, String email, String rol) {
}