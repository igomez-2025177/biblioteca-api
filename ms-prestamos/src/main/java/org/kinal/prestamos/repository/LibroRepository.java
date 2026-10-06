package org.kinal.prestamos.repository;

import jakarta.persistence.LockModeType;
import org.kinal.prestamos.entity.Libro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LibroRepository extends JpaRepository<Libro, Long> {

    // si titulo o categoria vienen vacios ("") el filtro deja pasar todo
    Page<Libro> findByActivoTrueAndTituloContainingIgnoreCaseAndCategoriaContainingIgnoreCase(
            String titulo, String categoria, Pageable pageable);

    Optional<Libro> findByIdAndActivoTrue(Long id);

    boolean existsByIsbn(String isbn);

    boolean existsByIsbnAndIdNot(String isbn, Long id);

    // bloquea la fila mientras se modifica el stock (evita choques con ms-prestamos)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Libro l WHERE l.id = :id AND l.activo = true")
    Optional<Libro> findActivoByIdForUpdate(@Param("id") Long id);
}