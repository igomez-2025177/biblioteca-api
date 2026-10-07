package org.kinal.reportes.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(ResourceNotFoundException ex, HttpServletRequest req) {
        return armar(HttpStatus.NOT_FOUND, ex.getMessage(), req, Map.of());
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> reglaDeNegocio(BusinessRuleException ex, HttpServletRequest req) {
        return armar(HttpStatus.CONFLICT, ex.getMessage(), req, Map.of());
    }

    // @Valid en el body
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> campos.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return armar(HttpStatus.BAD_REQUEST, "Hay campos invalidos", req, campos);
    }

    // validaciones en parametros (@RequestParam, @PathVariable)
    @ExceptionHandler({HandlerMethodValidationException.class, ConstraintViolationException.class})
    public ResponseEntity<ErrorResponse> validacionParametros(Exception ex, HttpServletRequest req) {
        return armar(HttpStatus.BAD_REQUEST, "Parametros invalidos", req, Map.of());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> jsonMalo(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return armar(HttpStatus.BAD_REQUEST, "El JSON viene mal formado o tiene un valor invalido", req, Map.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> tipoIncorrecto(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        return armar(HttpStatus.BAD_REQUEST, "Valor invalido para '" + ex.getName() + "'", req, Map.of());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> faltaParametro(MissingServletRequestParameterException ex, HttpServletRequest req) {
        return armar(HttpStatus.BAD_REQUEST, "Falta el parametro '" + ex.getParameterName() + "'", req, Map.of());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> credenciales(BadCredentialsException ex, HttpServletRequest req) {
        return armar(HttpStatus.UNAUTHORIZED, "Email o contrasena incorrectos", req, Map.of());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> autenticacion(AuthenticationException ex, HttpServletRequest req) {
        return armar(HttpStatus.UNAUTHORIZED, "No se pudo autenticar", req, Map.of());
    }

    // lo lanza @PreAuthorize cuando el rol no alcanza
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> accesoDenegado(AccessDeniedException ex, HttpServletRequest req) {
        return armar(HttpStatus.FORBIDDEN, "Tu rol no tiene permiso para esta accion", req, Map.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> integridad(DataIntegrityViolationException ex, HttpServletRequest req) {
        return armar(HttpStatus.CONFLICT, "El registro choca con datos existentes (duplicado o referencia invalida)", req, Map.of());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> rutaNoExiste(NoResourceFoundException ex, HttpServletRequest req) {
        return armar(HttpStatus.NOT_FOUND, "La ruta no existe", req, Map.of());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> metodoNoPermitido(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return armar(HttpStatus.METHOD_NOT_ALLOWED, "Metodo " + ex.getMethod() + " no permitido en esta ruta", req, Map.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> general(Exception ex, HttpServletRequest req) {
        log.error("Error no controlado en {}", req.getRequestURI(), ex);
        return armar(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno", req, Map.of());
    }

    private ResponseEntity<ErrorResponse> armar(HttpStatus status, String mensaje,
                                                HttpServletRequest req, Map<String, String> detalles) {
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                mensaje,
                req.getRequestURI(),
                detalles
        );
        return ResponseEntity.status(status).body(body);
    }
}