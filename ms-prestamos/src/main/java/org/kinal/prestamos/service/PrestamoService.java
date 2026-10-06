package org.kinal.prestamos.service;

import lombok.RequiredArgsConstructor;
import org.kinal.prestamos.dto.PageResponse;
import org.kinal.prestamos.dto.PrestamoRequest;
import org.kinal.prestamos.dto.PrestamoResponse;
import org.kinal.prestamos.entity.EstadoPrestamo;
import org.kinal.prestamos.entity.EstadoUsuario;
import org.kinal.prestamos.entity.Libro;
import org.kinal.prestamos.entity.Prestamo;
import org.kinal.prestamos.entity.Usuario;
import org.kinal.prestamos.exception.BusinessRuleException;
import org.kinal.prestamos.exception.ResourceNotFoundException;
import org.kinal.prestamos.repository.LibroRepository;
import org.kinal.prestamos.repository.PrestamoRepository;
import org.kinal.prestamos.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PrestamoService {

    public static final int MAX_PRESTAMOS_ACTIVOS = 3;
    public static final int DIAS_PLAZO = 14;

    // ATRASADO tambien cuenta como prestamo abierto (el libro no ha regresado)
    private static final List<EstadoPrestamo> ABIERTOS = List.of(EstadoPrestamo.ACTIVO, EstadoPrestamo.ATRASADO);

    private final PrestamoRepository prestamoRepository;
    private final UsuarioRepository usuarioRepository;
    private final LibroRepository libroRepository;

    /**
     * Registra la salida de un libro. Todo pasa en una sola transaccion:
     * si algo falla, no se descuenta stock ni se crea el prestamo.
     *
     * noRollbackFor: cuando detectamos que el usuario tiene un prestamo vencido,
     * lo pasamos a SANCIONADO y lanzamos la excepcion. Sin esto, el rollback
     * desharia la sancion. Las demas validaciones fallan antes de modificar nada.
     */
    @Transactional(noRollbackFor = BusinessRuleException.class)
    public PrestamoResponse registrarPrestamo(PrestamoRequest request) {
        LocalDate hoy = LocalDate.now();

        Usuario usuario = usuarioRepository.findByIdForUpdate(request.usuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", request.usuarioId()));

        // 1. usuario sancionado no puede prestar
        if (usuario.getEstado() == EstadoUsuario.SANCIONADO) {
            throw new BusinessRuleException("El usuario " + usuario.getEmail()
                    + " esta SANCIONADO y no puede realizar prestamos");
        }

        // 2. si tiene algun prestamo vencido, se sanciona en este momento
        List<Prestamo> vencidos = prestamoRepository
                .findByUsuarioIdAndEstadoInAndFechaDevolucionEsperadaBefore(usuario.getId(), ABIERTOS, hoy);
        if (!vencidos.isEmpty()) {
            vencidos.forEach(p -> p.setEstado(EstadoPrestamo.ATRASADO));
            usuario.setEstado(EstadoUsuario.SANCIONADO);
            throw new BusinessRuleException("El usuario tiene " + vencidos.size()
                    + " prestamo(s) vencido(s). Su estado cambio a SANCIONADO");
        }

        // 3. maximo 3 prestamos abiertos
        long abiertos = prestamoRepository.countByUsuarioIdAndEstadoIn(usuario.getId(), ABIERTOS);
        if (abiertos >= MAX_PRESTAMOS_ACTIVOS) {
            throw new BusinessRuleException("El usuario ya tiene " + abiertos
                    + " prestamos activos. El maximo es " + MAX_PRESTAMOS_ACTIVOS);
        }

        // 4. el libro debe existir, estar activo y tener ejemplares
        Libro libro = libroRepository.findByIdForUpdate(request.libroId())
                .filter(Libro::getActivo)
                .orElseThrow(() -> new ResourceNotFoundException("Libro", request.libroId()));

        if (libro.getStockDisponible() <= 0) {
            throw new BusinessRuleException("No hay ejemplares disponibles de '" + libro.getTitulo() + "'");
        }

        // todo bien: se descuenta stock y se crea el prestamo
        libro.setStockDisponible(libro.getStockDisponible() - 1);

        Prestamo prestamo = new Prestamo();
        prestamo.setUsuario(usuario);
        prestamo.setLibro(libro);
        prestamo.setFechaPrestamo(hoy);
        prestamo.setFechaDevolucionEsperada(hoy.plusDays(DIAS_PLAZO));
        prestamo.setEstado(EstadoPrestamo.ACTIVO);

        return PrestamoResponse.of(prestamoRepository.save(prestamo));
    }

    /**
     * Registra la entrega del libro: suma 1 al stock y marca DEVUELTO.
     */
    @Transactional
    public PrestamoResponse registrarDevolucion(Long prestamoId) {
        Prestamo prestamo = prestamoRepository.findByIdForUpdate(prestamoId)
                .orElseThrow(() -> new ResourceNotFoundException("Prestamo", prestamoId));

        if (prestamo.getEstado() == EstadoPrestamo.DEVUELTO) {
            throw new BusinessRuleException("Este prestamo ya fue devuelto el " + prestamo.getFechaDevolucionReal());
        }

        Libro libro = libroRepository.findByIdForUpdate(prestamo.getLibro().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Libro", prestamo.getLibro().getId()));

        libro.setStockDisponible(Math.min(libro.getStockDisponible() + 1, libro.getStockTotal()));

        prestamo.setFechaDevolucionReal(LocalDate.now());
        prestamo.setEstado(EstadoPrestamo.DEVUELTO);

        return PrestamoResponse.of(prestamo);
    }

    @Transactional(readOnly = true)
    public PageResponse<PrestamoResponse> misPrestamos(Long usuarioId, Pageable pageable) {
        return mapear(prestamoRepository.findByUsuarioId(usuarioId, pageable));
    }

    @Transactional(readOnly = true)
    public PageResponse<PrestamoResponse> atrasados(Pageable pageable) {
        return mapear(prestamoRepository.findByEstadoInAndFechaDevolucionEsperadaBefore(
                ABIERTOS, LocalDate.now(), pageable));
    }

    @Transactional(readOnly = true)
    public PageResponse<PrestamoResponse> listar(EstadoPrestamo estado, Pageable pageable) {
        Page<Prestamo> pagina = estado == null
                ? prestamoRepository.findAll(pageable)
                : prestamoRepository.findByEstado(estado, pageable);
        return mapear(pagina);
    }

    @Transactional(readOnly = true)
    public PrestamoResponse obtener(Long id) {
        return PrestamoResponse.of(prestamoRepository.findConDetalleById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prestamo", id)));
    }

    private PageResponse<PrestamoResponse> mapear(Page<Prestamo> pagina) {
        return PageResponse.of(pagina.map(PrestamoResponse::of));
    }
}