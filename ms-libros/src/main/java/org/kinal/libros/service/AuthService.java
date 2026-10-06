package org.kinal.libros.service;

import lombok.RequiredArgsConstructor;
import org.kinal.libros.dto.AuthResponse;
import org.kinal.libros.dto.LoginRequest;
import org.kinal.libros.dto.RegisterRequest;
import org.kinal.libros.entity.EstadoUsuario;
import org.kinal.libros.entity.Rol;
import org.kinal.libros.entity.Usuario;
import org.kinal.libros.exception.BusinessRuleException;
import org.kinal.libros.exception.ResourceNotFoundException;
import org.kinal.libros.repository.UsuarioRepository;
import org.kinal.libros.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse registrar(RegisterRequest request) {
        String email = normalizarEmail(request.email());

        if (usuarioRepository.existsByEmail(email)) {
            throw new BusinessRuleException("Ya existe un usuario con el email " + email);
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.nombre().trim());
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setRol(Rol.LECTOR); // el registro publico siempre es LECTOR
        usuario.setEstado(EstadoUsuario.ACTIVO);

        Usuario guardado = usuarioRepository.save(usuario);
        return AuthResponse.of(guardado, jwtService.generarToken(guardado), jwtService.getExpirationMs());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = normalizarEmail(request.email());

        // si el password no coincide, esto lanza BadCredentialsException -> 401
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        return AuthResponse.of(usuario, jwtService.generarToken(usuario), jwtService.getExpirationMs());
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase();
    }
}