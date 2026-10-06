package org.kinal.prestamos.service;

import lombok.RequiredArgsConstructor;
import org.kinal.prestamos.dto.LibroRequest;
import org.kinal.prestamos.dto.LibroResponse;
import org.kinal.prestamos.dto.PageResponse;
import org.kinal.prestamos.entity.Libro;
import org.kinal.prestamos.exception.BusinessRuleException;
import org.kinal.prestamos.exception.ResourceNotFoundException;
import org.kinal.prestamos.repository.LibroRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LibroService {

    private final LibroRepository libroRepository;

    @Transactional(readOnly = true)
    public PageResponse<LibroResponse> listar(String titulo, String categoria, Pageable pageable) {
        String filtroTitulo = titulo == null ? "" : titulo.trim();
        String filtroCategoria = categoria == null ? "" : categoria.trim();

        return PageResponse.of(
                libroRepository
                        .findByActivoTrueAndTituloContainingIgnoreCaseAndCategoriaContainingIgnoreCase(
                                filtroTitulo, filtroCategoria, pageable)
                        .map(LibroResponse::of)
        );
    }

    @Transactional(readOnly = true)
    public LibroResponse obtener(Long id) {
        return LibroResponse.of(
                libroRepository.findByIdAndActivoTrue(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Libro", id))
        );
    }

    @Transactional
    public LibroResponse crear(LibroRequest request) {
        String isbn = request.isbn().trim();
        if (libroRepository.existsByIsbn(isbn)) {
            throw new BusinessRuleException("Ya existe un libro con el ISBN " + isbn);
        }

        Libro libro = new Libro();
        copiarDatos(libro, request);
        libro.setStockTotal(request.stockTotal());
        libro.setStockDisponible(request.stockTotal()); // nuevo: todo disponible
        libro.setActivo(true);

        return LibroResponse.of(libroRepository.save(libro));
    }

    /**
     * Si cambia el stock total, el disponible se recalcula respetando
     * los ejemplares que estan prestados en ese momento.
     */
    @Transactional
    public LibroResponse actualizar(Long id, LibroRequest request) {
        Libro libro = libroRepository.findActivoByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Libro", id));

        String isbn = request.isbn().trim();
        if (libroRepository.existsByIsbnAndIdNot(isbn, id)) {
            throw new BusinessRuleException("Ya existe otro libro con el ISBN " + isbn);
        }

        int prestados = libro.getPrestados();
        if (request.stockTotal() < prestados) {
            throw new BusinessRuleException("El stock total no puede ser menor a los "
                    + prestados + " ejemplares que estan prestados");
        }

        copiarDatos(libro, request);
        libro.setStockTotal(request.stockTotal());
        libro.setStockDisponible(request.stockTotal() - prestados);

        return LibroResponse.of(libro);
    }

    /**
     * Eliminacion logica. No se deja eliminar si hay ejemplares prestados.
     */
    @Transactional
    public void eliminar(Long id) {
        Libro libro = libroRepository.findActivoByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Libro", id));

        if (libro.getPrestados() > 0) {
            throw new BusinessRuleException("No se puede eliminar: tiene "
                    + libro.getPrestados() + " ejemplares prestados");
        }
        libro.setActivo(false);
    }

    private void copiarDatos(Libro libro, LibroRequest request) {
        libro.setIsbn(request.isbn().trim());
        libro.setTitulo(request.titulo().trim());
        libro.setAutor(request.autor().trim());
        libro.setCategoria(request.categoria().trim());
    }
}