package org.kinal.prestamos.repository;

import jakarta.persistence.LockModeType;
import org.kinal.prestamos.entity.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LibroRepository extends JpaRepository<Libro, Long> {

    // SELECT ... FOR UPDATE: dos bibliotecarios no pueden prestar
    // el ultimo ejemplar al mismo tiempo
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Libro l WHERE l.id = :id")
    Optional<Libro> findByIdForUpdate(@Param("id") Long id);
}