package org.kinal.prestamos.repository;

import jakarta.persistence.LockModeType;
import org.kinal.prestamos.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    // bloquea al usuario mientras se registra el prestamo:
    // si llegan 2 prestamos al mismo tiempo para el mismo lector, el segundo espera
    // y asi no se pasa del limite de 3
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM Usuario u WHERE u.id = :id")
    Optional<Usuario> findByIdForUpdate(@Param("id") Long id);
}