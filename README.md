# Biblioteca Kinal

Proyecto de la Evaluación 5 de Kinal. Es una API REST para manejar una biblioteca: libros, usuarios, préstamos y devoluciones.

La hice con Spring Boot 4.1.1, Java 21, Spring Security con JWT y PostgreSQL. La separé en microservicios, así que cada parte corre por su lado.

## Cómo está armado

Son 4 microservicios y un gateway:

- **ms-usuarios** (8081): registro, login y manejo de usuarios
- **ms-libros** (8082): el catálogo de libros
- **ms-prestamos** (8083): préstamos, devoluciones y las reglas (stock, límite de 3, sanciones)
- **ms-reportes** (8084): reportes para el admin
- **ms-gateway** (8090): recibe todo y lo manda al servicio que toca según la ruta

Cada servicio es un proyecto Maven aparte, con su propio login y su propia seguridad. Si apago uno, los demás siguen funcionando. Los 4 usan la misma base (`biblioteca_db`), porque cuando se hace un préstamo hay que bajar el stock y guardar el préstamo al mismo tiempo, en una sola transacción.

El gateway lo dejé en el 8090 porque en el lab el 8080 estaba ocupado. Si se quiere otro puerto, se arranca con la variable `PORT`.

## Lo que se necesita

- Java 21
- PostgreSQL con usuario `postgres` y contraseña `admin` (si tienen otra, se cambia en el `application.properties` de cada servicio)
- IntelliJ
- Git Bash con `jq` para el script de pruebas

## Cómo correrlo

1. Abrir la carpeta `biblioteca-api` en IntelliJ.
2. En cada carpeta `ms-...`, clic derecho al `pom.xml` y **Add as Maven Project**.
3. Correr las 5 clases `...Application` (UsuariosApplication, LibrosApplication, PrestamosApplication, ReportesApplication y GatewayApplication). El orden no importa.

La base no hay que crearla. Al arrancar, cada servicio la crea si no existe y corre el `schema.sql` y el `data.sql`. Esos scripts se pueden correr varias veces sin que se dupliquen los datos.

Para probar, el script del profe:

```bash
bash pruebas/test-api5.sh
```

También dejé la colección de Postman en la carpeta `postman/`.

## Usuarios para probar

| Rol | Email | Contraseña |
|---|---|---|
| ADMIN | admin@biblioteca.com | Admin123* |
| BIBLIOTECARIO | bibliotecario@biblioteca.com | Biblio123* |
| LECTOR | lector@biblioteca.com | Lector123* |

Además el `data.sql` mete 1000 lectores (`lector1` hasta `lector1000`, todos con `Lector123*`) y más de 4000 libros, para probar que aguante carga.

Dejé algunos usuarios preparados para probar las reglas:
- `lector1` tiene un préstamo vencido, así que si pide otro queda sancionado
- `lector2` ya tiene 3 préstamos, entonces no le deja pedir el cuarto
- `lector3` tiene la única copia de *El Principito*, para probar cuando no hay stock

## Roles

- **ADMIN**: puede hacer todo (libros, usuarios, préstamos y reportes)
- **BIBLIOTECARIO**: registra préstamos y devoluciones y ve el catálogo
- **LECTOR**: ve el catálogo y sus propios préstamos

## Endpoints principales

Todo va con `/api/v1` adelante, y se puede llamar por el gateway (8090) o directo al puerto del servicio.

**Auth**
- `POST /auth/register` registra un lector
- `POST /auth/login` devuelve el token

**Libros**
- `GET /libros` lista con paginación y filtros (`?titulo=`, `?categoria=`)
- `GET /libros/{id}`
- `POST /libros`, `PUT /libros/{id}` y `DELETE /libros/{id}` (solo ADMIN; el delete no borra, solo desactiva el libro)

**Préstamos**
- `POST /prestamos` registra un préstamo (BIBLIOTECARIO o ADMIN)
- `PATCH /prestamos/{id}/devolucion` registra la devolución
- `GET /prestamos/mis-prestamos` el historial del lector que está logueado
- `GET /prestamos/atrasados` los que ya pasaron la fecha

**Usuarios** (solo ADMIN): `GET /usuarios`, `GET /usuarios/{id}`, `PATCH /usuarios/{id}/rol`, `PATCH /usuarios/{id}/estado`

**Reportes** (solo ADMIN): `/reportes/resumen`, `/reportes/libros-mas-prestados`, `/reportes/usuarios-con-mas-prestamos`, `/reportes/prestamos-por-categoria`, `/reportes/usuarios-sancionados`

## Reglas del préstamo

Cuando se registra un préstamo, el sistema revisa en este orden:

1. Si el usuario ya está sancionado, no le presta.
2. Si tiene un préstamo vencido, lo sanciona y no le presta.
3. Si ya tiene 3 préstamos activos, no le presta.
4. Si el libro no tiene ejemplares disponibles, no le presta.

Si pasa todo, baja el stock y guarda el préstamo con 14 días para devolverlo.

El préstamo y la devolución llevan `@Transactional`, así no queda un préstamo guardado sin bajar el stock. Para la sanción usé `noRollbackFor`, porque si no, al lanzar el error se deshacía también la sanción. También bloqueo el libro mientras se presta (`@Lock`), para que dos bibliotecarios no presten el último ejemplar al mismo tiempo.

## Errores

Todos los errores salen con el mismo formato, gracias al `GlobalExceptionHandler`:

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "El usuario ya tiene 3 prestamos activos",
  "path": "/api/v1/prestamos"
}
```

- 400: datos mal mandados
- 401: no hay token o está mal
- 403: el rol no tiene permiso
- 404: no existe
- 409: se rompió una regla, o el registro ya existe

## Problemas que me pasaron

- **Puerto ocupado al arrancar**: quedaba una instancia vieja corriendo. Lo arreglé con `netstat -ano | findstr :8082` y `taskkill /PID <numero> /F`.
- **"permiso denegado al esquema public"** en el lab: en pgAdmin corrí `ALTER SCHEMA public OWNER TO postgres;`
- **El script del profe daba 400 al crear el libro**: la "ó" de "Programación" llega mal desde curl en Windows. Lo arreglé en el gateway, que convierte el texto a UTF-8 antes de mandarlo.
- **Los cambios no se veían**: había que hacer Build → Rebuild Project y reiniciar el servicio.

---

Isaí Gómez · 5to Perito en Informática · Kinal · 2025-177
