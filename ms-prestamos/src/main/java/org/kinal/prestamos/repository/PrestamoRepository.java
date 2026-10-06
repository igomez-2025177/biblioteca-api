package org.kinal.prestamos.repository;

import jakarta.persistence.LockModeType;
import org.kinal.prestamos.entity.EstadoPrestamo;
import org.kinal.prestamos.entity.Prestamo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/*
 * @EntityGraph trae usuario y libro en el mismo SELECT (JOIN),
 * asi no se hace una consulta extra por cada prestamo (problema N+1).
 */
public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    long countByUsuarioIdAndEstadoIn(Long usuarioId, Collection<EstadoPrestamo> estados);

    List<Prestamo> findByUsuarioIdAndEstadoInAndFechaDevolucionEsperadaBefore(
            Long usuarioId, Collection<EstadoPrestamo> estados, LocalDate fecha);

    @EntityGraph(attributePaths = {"usuario", "libro"})
    Page<Prestamo> findByUsuarioId(Long usuarioId, Pageable pageable);

    @EntityGraph(attributePaths = {"usuario", "libro"})
    Page<Prestamo> findByEstadoInAndFechaDevolucionEsperadaBefore(
            Collection<EstadoPrestamo> estados, LocalDate fecha, Pageable pageable);

    @EntityGraph(attributePaths = {"usuario", "libro"})
    Page<Prestamo> findByEstado(EstadoPrestamo estado, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"usuario", "libro"})
    Page<Prestamo> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"usuario", "libro"})
    Optional<Prestamo> findConDetalleById(Long id);

    // evita que dos devoluciones del mismo prestamo se procesen al mismo tiempo
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Prestamo p WHERE p.id = :id")
    Optional<Prestamo> findByIdForUpdate(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Prestamo p SET p.estado = :nuevo WHERE p.estado = :actual AND p.fechaDevolucionEsperada < :hoy")
    int marcarVencidos(@Param("actual") EstadoPrestamo actual,
                       @Param("nuevo") EstadoPrestamo nuevo,
                       @Param("hoy") LocalDate hoy);
}