package org.kinal.reportes.security;

public record AuthenticatedUser(Long id, String email, String rol) {
}