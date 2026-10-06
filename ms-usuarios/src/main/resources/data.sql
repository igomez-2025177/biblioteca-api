    -- =====================================================
    -- Carga inicial (PostgreSQL)
    -- Se puede correr las veces que sea, no duplica nada.
    --
    -- Credenciales:
    --   admin@biblioteca.com          / Admin123!
    --   bibliotecario@biblioteca.com  / Biblio123!
    --   lector@biblioteca.com         / Lector123!
    --   lector1..lector1000@biblioteca.com / Lector123!
    -- =====================================================

    INSERT INTO roles (nombre, descripcion) VALUES
        ('ADMIN', 'Control total del catalogo, usuarios y reportes'),
        ('BIBLIOTECARIO', 'Registra entregas, devoluciones y consulta prestamos'),
        ('LECTOR', 'Consulta el catalogo y su propio historial')
    ON CONFLICT (nombre) DO NOTHING;

    INSERT INTO usuarios (nombre, email, password, estado, rol) VALUES
        ('Administrador', 'admin@biblioteca.com', '$2a$10$53HD4xjOIn.LIA6FccbSg.kC6F1psWA57fkDd/KSMAHEahrmSOUI6', 'ACTIVO', 'ADMIN'),
        ('Bibliotecario General', 'bibliotecario@biblioteca.com', '$2a$10$KlTJTthJYLPdW7efbjRs5e2hvkR2zN2/fYwuBF92ekL8C/KZ7jor.', 'ACTIVO', 'BIBLIOTECARIO'),
        ('Lector Demo', 'lector@biblioteca.com', '$2a$10$dW0ch0lYtjJp70HTpJhP0OwrPb0KTs6c2SDajtoJMy.FlzqWNCtKy', 'ACTIVO', 'LECTOR')
    ON CONFLICT (email) DO NOTHING;

    -- 1000 lectores para pruebas de carga
    INSERT INTO usuarios (nombre, email, password, estado, rol)
    SELECT 'Lector ' || g,
           'lector' || g || '@biblioteca.com',
           '$2a$10$dW0ch0lYtjJp70HTpJhP0OwrPb0KTs6c2SDajtoJMy.FlzqWNCtKy',
           'ACTIVO',
           'LECTOR'
    FROM generate_series(1, 1000) AS g
    ON CONFLICT (email) DO NOTHING;

    INSERT INTO libros (isbn, titulo, autor, categoria, stock_total, stock_disponible, activo) VALUES
        ('978-84-376-0494-7', 'Cien años de soledad', 'Gabriel García Márquez', 'Literatura', 5, 5, TRUE),
        ('978-84-206-0807-5', 'Don Quijote de la Mancha', 'Miguel de Cervantes', 'Literatura', 4, 4, TRUE),
        ('978-84-9759-209-1', 'El señor presidente', 'Miguel Ángel Asturias', 'Literatura', 5, 5, TRUE),
        ('978-84-7888-445-2', 'El Principito', 'Antoine de Saint-Exupéry', 'Literatura', 1, 1, TRUE),
        ('978-0-13-235088-4', 'Clean Code', 'Robert C. Martin', 'Programacion', 3, 3, TRUE),
        ('978-0-13-468599-1', 'Effective Java', 'Joshua Bloch', 'Programacion', 3, 3, TRUE),
        ('978-1-61729-875-6', 'Spring in Action', 'Craig Walls', 'Programacion', 4, 4, TRUE),
        ('978-0-262-03384-8', 'Introduction to Algorithms', 'Thomas H. Cormen', 'Programacion', 2, 2, TRUE),
        ('978-0-07-352332-3', 'Database System Concepts', 'Abraham Silberschatz', 'Bases de Datos', 3, 3, TRUE),
        ('978-0-13-359414-0', 'Computer Networking', 'James Kurose', 'Redes', 3, 3, TRUE)
    ON CONFLICT (isbn) DO NOTHING;

    -- 4000 libros generados para pruebas de carga
    INSERT INTO libros (isbn, titulo, autor, categoria, stock_total, stock_disponible, activo)
    SELECT 'GEN-' || LPAD(g::text, 6, '0'),
           'Libro de prueba ' || g,
           'Autor ' || (g % 250 + 1),
           (ARRAY['Programacion', 'Matematicas', 'Historia', 'Literatura', 'Ciencia', 'Redes', 'Bases de Datos', 'Arte'])[g % 8 + 1],
           5,
           5,
           TRUE
    FROM generate_series(1, 4000) AS g
    ON CONFLICT (isbn) DO NOTHING;

    INSERT INTO prestamos (usuario_id, libro_id, fecha_prestamo, fecha_devolucion_esperada, fecha_devolucion_real, estado)
    SELECT s.usuario_id, s.libro_id, s.fecha_prestamo, s.fecha_esperada, s.fecha_real, s.estado
    FROM (
        SELECT u.id AS usuario_id, l.id AS libro_id, CURRENT_DATE - 20 AS fecha_prestamo, CURRENT_DATE - 6 AS fecha_esperada, NULL::date AS fecha_real, 'ACTIVO' AS estado
        FROM usuarios u, libros l
        WHERE u.email = 'lector1@biblioteca.com' AND l.isbn = '978-84-376-0494-7'
        UNION ALL
        SELECT u.id, l.id, CURRENT_DATE - 2, CURRENT_DATE + 12, NULL::date, 'ACTIVO'
        FROM usuarios u, libros l
        WHERE u.email = 'lector2@biblioteca.com' AND l.isbn IN ('978-0-13-235088-4', '978-0-13-468599-1', '978-1-61729-875-6')
        UNION ALL
        SELECT u.id, l.id, CURRENT_DATE - 1, CURRENT_DATE + 13, NULL::date, 'ACTIVO'
        FROM usuarios u, libros l
        WHERE u.email = 'lector3@biblioteca.com' AND l.isbn = '978-84-7888-445-2'
        UNION ALL
        SELECT u.id, l.id, DATE '2025-01-01' + (g % 300), DATE '2025-01-01' + (g % 300) + 14, DATE '2025-01-01' + (g % 300) + (g % 14), 'DEVUELTO'
        FROM generate_series(1, 3000) AS g
        JOIN usuarios u ON u.email = 'lector' || (g % 1000 + 1) || '@biblioteca.com'
        JOIN libros l ON l.isbn = 'GEN-' || LPAD((CASE WHEN g % 3 = 0 THEN g % 25 + 1 ELSE (g * 7) % 4000 + 1 END)::text, 6, '0')
    ) AS s
    WHERE NOT EXISTS (SELECT 1 FROM prestamos);

    UPDATE libros l
    SET stock_disponible = l.stock_total - (
        SELECT COUNT(*) FROM prestamos p
        WHERE p.libro_id = l.id AND p.estado IN ('ACTIVO', 'ATRASADO')
    )
    WHERE l.stock_disponible <> l.stock_total - (
        SELECT COUNT(*) FROM prestamos p
        WHERE p.libro_id = l.id AND p.estado IN ('ACTIVO', 'ATRASADO')
    );