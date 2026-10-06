package org.kinal.libros.repository;

import org.kinal.libros.entity.EstadoUsuario;
import org.kinal.libros.entity.Rol;
import org.kinal.libros.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<Usuario> findByRol(Rol rol, Pageable pageable);

    Page<Usuario> findByEstado(EstadoUsuario estado, Pageable pageable);

    Page<Usuario> findByRolAndEstado(Rol rol, EstadoUsuario estado, Pageable pageable);
}