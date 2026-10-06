package org.kinal.usuarios.service;

import lombok.RequiredArgsConstructor;
import org.kinal.usuarios.dto.PageResponse;
import org.kinal.usuarios.dto.UsuarioCreateRequest;
import org.kinal.usuarios.dto.UsuarioResponse;
import org.kinal.usuarios.entity.EstadoUsuario;
import org.kinal.usuarios.entity.Rol;
import org.kinal.usuarios.entity.Usuario;
import org.kinal.usuarios.exception.BusinessRuleException;
import org.kinal.usuarios.exception.ResourceNotFoundException;
import org.kinal.usuarios.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public PageResponse<UsuarioResponse> listar(Rol rol, EstadoUsuario estado, Pageable pageable) {
        Page<Usuario> pagina;
        if (rol != null && estado != null) {
            pagina = usuarioRepository.findByRolAndEstado(rol, estado, pageable);
        } else if (rol != null) {
            pagina = usuarioRepository.findByRol(rol, pageable);
        } else if (estado != null) {
            pagina = usuarioRepository.findByEstado(estado, pageable);
        } else {
            pagina = usuarioRepository.findAll(pageable);
        }
        return PageResponse.of(pagina.map(UsuarioResponse::of));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        return UsuarioResponse.of(buscar(id));
    }

    @Transactional
    public UsuarioResponse crear(UsuarioCreateRequest request) {
        String email = request.email().trim().toLowerCase();
        if (usuarioRepository.existsByEmail(email)) {
            throw new BusinessRuleException("Ya existe un usuario con el email " + email);
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.nombre().trim());
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setRol(request.rol());
        usuario.setEstado(EstadoUsuario.ACTIVO);

        return UsuarioResponse.of(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse cambiarRol(Long id, Rol nuevoRol, Long idAdminActual) {
        if (id.equals(idAdminActual)) {
            throw new BusinessRuleException("No puedes cambiar tu propio rol");
        }
        Usuario usuario = buscar(id);
        usuario.setRol(nuevoRol);
        return UsuarioResponse.of(usuario);
    }

    /**
     * Sirve para levantar una sancion (SANCIONADO -> ACTIVO) o sancionar a mano.
     */
    @Transactional
    public UsuarioResponse cambiarEstado(Long id, EstadoUsuario nuevoEstado, Long idAdminActual) {
        if (id.equals(idAdminActual)) {
            throw new BusinessRuleException("No puedes cambiar tu propio estado");
        }
        Usuario usuario = buscar(id);
        usuario.setEstado(nuevoEstado);
        return UsuarioResponse.of(usuario);
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
    }
}