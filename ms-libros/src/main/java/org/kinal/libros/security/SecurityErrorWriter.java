package org.kinal.libros.security;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

final class SecurityErrorWriter {

    private SecurityErrorWriter() {
    }

    static void escribir(HttpServletResponse response, int status, String error,
                         String mensaje, String path) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        String json = "{"
                + "\"timestamp\":\"" + LocalDateTime.now() + "\","
                + "\"status\":" + status + ","
                + "\"error\":\"" + limpiar(error) + "\","
                + "\"message\":\"" + limpiar(mensaje) + "\","
                + "\"path\":\"" + limpiar(path) + "\","
                + "\"details\":{}"
                + "}";
        response.getWriter().write(json);
    }

    private static String limpiar(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}