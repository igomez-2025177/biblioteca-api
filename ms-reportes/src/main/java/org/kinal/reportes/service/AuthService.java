package org.kinal.reportes.service;

import lombok.RequiredArgsConstructor;
import org.kinal.reportes.dto.AuthResponse;
import org.kinal.reportes.dto.LoginRequest;
import org.kinal.reportes.entity.Usuario;
import org.kinal.reportes.exception.ResourceNotFoundException;
import org.kinal.reportes.repository.UsuarioRepository;
import org.kinal.reportes.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Login propio de este microservicio, para que se pueda probar
 * sin tener levantado ms-usuarios. El registro solo vive en ms-usuarios.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        return AuthResponse.of(usuario, jwtService.generarToken(usuario), jwtService.getExpirationMs());
    }
}