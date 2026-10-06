package org.kinal.prestamos.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        Object errorJwt = request.getAttribute(JwtAuthenticationFilter.JWT_ERROR_ATTRIBUTE);
        String mensaje = errorJwt != null
                ? errorJwt.toString()
                : "Debes iniciar sesion y enviar el token (Authorization: Bearer <token>)";

        SecurityErrorWriter.escribir(response, HttpServletResponse.SC_UNAUTHORIZED,
                "Unauthorized", mensaje, request.getRequestURI());
    }
}