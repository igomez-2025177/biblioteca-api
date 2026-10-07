package org.kinal.reportes.service;

import org.kinal.reportes.dto.CategoriaReporteResponse;
import org.kinal.reportes.dto.LibroRankingResponse;
import org.kinal.reportes.dto.PageResponse;
import org.kinal.reportes.dto.ResumenResponse;
import org.kinal.reportes.dto.UsuarioRankingResponse;
import org.kinal.reportes.dto.UsuarioSancionadoResponse;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.List;

/**
 * Los reportes son puros conteos (COUNT, SUM, GROUP BY),
 * asi que van con SQL directo: la base calcula todo
 * y no hay que cargar miles de entidades en memoria.
 */
@Service
@Transactional(readOnly = true)
public class ReporteService {

    private final NamedParameterJdbcTemplate jdbc;

    public ReporteService(DataSource dataSource) {
        this.jdbc = new NamedParameterJdbcTemplate(dataSource);
    }

    public ResumenResponse resumen() {
        String sql = """
                SELECT
                    (SELECT COUNT(*) FROM libros WHERE activo = TRUE) AS total_libros,
                    (SELECT COALESCE(SUM(stock_total), 0) FROM libros WHERE activo = TRUE) AS total_ejemplares,
                    (SELECT COALESCE(SUM(stock_disponible), 0) FROM libros WHERE activo = TRUE) AS disponibles,
                    (SELECT COUNT(*) FROM usuarios) AS total_usuarios,
                    (SELECT COUNT(*) FROM usuarios WHERE estado = 'SANCIONADO') AS sancionados,
                    (SELECT COUNT(*) FROM prestamos WHERE estado IN ('ACTIVO', 'ATRASADO')) AS activos,
                    (SELECT COUNT(*) FROM prestamos WHERE estado IN ('ACTIVO', 'ATRASADO')
                        AND fecha_devolucion_esperada < CURRENT_DATE) AS vencidos,
                    (SELECT COUNT(*) FROM prestamos WHERE estado = 'DEVUELTO') AS devueltos
                """;

        return jdbc.queryForObject(sql, new MapSqlParameterSource(), (rs, i) -> {
            long totalEjemplares = rs.getLong("total_ejemplares");
            long disponibles = rs.getLong("disponibles");
            return new ResumenResponse(
                    rs.getLong("total_libros"),
                    totalEjemplares,
                    disponibles,
                    totalEjemplares - disponibles,
                    rs.getLong("total_usuarios"),
                    rs.getLong("sancionados"),
                    rs.getLong("activos"),
                    rs.getLong("vencidos"),
                    rs.getLong("devueltos")
            );
        });
    }

    public List<LibroRankingResponse> librosMasPrestados(int top) {
        String sql = """
                SELECT l.id, l.isbn, l.titulo, l.categoria, COUNT(p.id) AS total
                FROM prestamos p
                JOIN libros l ON l.id = p.libro_id
                GROUP BY l.id, l.isbn, l.titulo, l.categoria
                ORDER BY total DESC, l.titulo
                LIMIT :top
                """;

        return jdbc.query(sql, new MapSqlParameterSource("top", limitar(top)), (rs, i) ->
                new LibroRankingResponse(
                        rs.getLong("id"),
                        rs.getString("isbn"),
                        rs.getString("titulo"),
                        rs.getString("categoria"),
                        rs.getLong("total")
                ));
    }

    public List<UsuarioRankingResponse> usuariosConMasPrestamos(int top) {
        String sql = """
                SELECT u.id, u.nombre, u.email, u.estado,
                       COUNT(p.id) AS total,
                       COUNT(p.id) FILTER (WHERE p.estado IN ('ACTIVO', 'ATRASADO')) AS abiertos
                FROM prestamos p
                JOIN usuarios u ON u.id = p.usuario_id
                GROUP BY u.id, u.nombre, u.email, u.estado
                ORDER BY total DESC, u.nombre
                LIMIT :top
                """;

        return jdbc.query(sql, new MapSqlParameterSource("top", limitar(top)), (rs, i) ->
                new UsuarioRankingResponse(
                        rs.getLong("id"),
                        rs.getString("nombre"),
                        rs.getString("email"),
                        rs.getString("estado"),
                        rs.getLong("total"),
                        rs.getLong("abiertos")
                ));
    }

    public List<CategoriaReporteResponse> prestamosPorCategoria() {
        String sql = """
                SELECT l.categoria,
                       COUNT(p.id) AS total,
                       COUNT(p.id) FILTER (WHERE p.estado IN ('ACTIVO', 'ATRASADO')) AS abiertos
                FROM prestamos p
                JOIN libros l ON l.id = p.libro_id
                GROUP BY l.categoria
                ORDER BY total DESC
                """;

        return jdbc.query(sql, new MapSqlParameterSource(), (rs, i) ->
                new CategoriaReporteResponse(
                        rs.getString("categoria"),
                        rs.getLong("total"),
                        rs.getLong("abiertos")
                ));
    }

    public PageResponse<UsuarioSancionadoResponse> usuariosSancionados(int page, int size) {
        int tamano = limitar(size);
        int pagina = Math.max(page, 0);

        long total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM usuarios WHERE estado = 'SANCIONADO'",
                new MapSqlParameterSource(), Long.class);

        String sql = """
                SELECT u.id, u.nombre, u.email,
                       COUNT(p.id) FILTER (WHERE p.estado IN ('ACTIVO', 'ATRASADO')
                           AND p.fecha_devolucion_esperada < CURRENT_DATE) AS vencidos
                FROM usuarios u
                LEFT JOIN prestamos p ON p.usuario_id = u.id
                WHERE u.estado = 'SANCIONADO'
                GROUP BY u.id, u.nombre, u.email
                ORDER BY u.id
                LIMIT :size OFFSET :offset
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("size", tamano)
                .addValue("offset", (long) pagina * tamano);

        List<UsuarioSancionadoResponse> contenido = jdbc.query(sql, params, (rs, i) ->
                new UsuarioSancionadoResponse(
                        rs.getLong("id"),
                        rs.getString("nombre"),
                        rs.getString("email"),
                        rs.getLong("vencidos")
                ));

        int totalPaginas = (int) Math.ceil((double) total / tamano);
        return new PageResponse<>(contenido, pagina, tamano, total, totalPaginas, pagina + 1 >= totalPaginas);
    }

    // entre 1 y 100 para que nadie pida un millon de filas
    private int limitar(int valor) {
        return Math.min(Math.max(valor, 1), 100);
    }
}