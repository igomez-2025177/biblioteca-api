package org.kinal.gateway.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * API Gateway: todo entra por un solo puerto y se reenvia al microservicio que toca
 * segun el inicio de la ruta. El token JWT pasa tal cual y lo valida cada servicio.
 *
 *   /api/v1/auth/**       -> ms-usuarios  (8081)
 *   /api/v1/usuarios/**   -> ms-usuarios  (8081)
 *   /api/v1/libros/**     -> ms-libros    (8082)
 *   /api/v1/prestamos/**  -> ms-prestamos (8083)
 *   /api/v1/reportes/**   -> ms-reportes  (8084)
 */
@RestController
public class ProxyController {

    // headers que maneja el propio cliente HTTP (si se copian, Java lanza error)
    private static final Set<String> HEADERS_NO_COPIAR = Set.of(
            "host", "connection", "content-length", "transfer-encoding", "upgrade",
            "expect", "keep-alive", "te", "trailer", "proxy-connection", "http2-settings");

    private final HttpClient http = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final Map<String, String> rutas = new LinkedHashMap<>();

    public ProxyController(@Value("${gateway.url.usuarios}") String usuarios,
                           @Value("${gateway.url.libros}") String libros,
                           @Value("${gateway.url.prestamos}") String prestamos,
                           @Value("${gateway.url.reportes}") String reportes) {
        rutas.put("/api/v1/auth", usuarios);
        rutas.put("/api/v1/usuarios", usuarios);
        rutas.put("/api/v1/libros", libros);
        rutas.put("/api/v1/prestamos", prestamos);
        rutas.put("/api/v1/reportes", reportes);
    }

    @RequestMapping("/api/v1/**")
    public ResponseEntity<byte[]> reenviar(HttpServletRequest request,
                                           @RequestBody(required = false) byte[] body) throws InterruptedException {
        String path = request.getRequestURI();
        String destino = buscarDestino(path);
        if (destino == null) {
            return error(404, "Not Found", "Ninguna ruta del gateway coincide con " + path, path);
        }

        String query = request.getQueryString();
        URI uri = URI.create(destino + path + (query != null ? "?" + query : ""));

        HttpRequest.Builder peticion = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(30));

        // copia los headers del cliente (Authorization, Content-Type, etc.)
        for (String nombre : Collections.list(request.getHeaderNames())) {
            if (HEADERS_NO_COPIAR.contains(nombre.toLowerCase())) {
                continue;
            }
            for (String valor : Collections.list(request.getHeaders(nombre))) {
                peticion.header(nombre, valor);
            }
        }

        body = arreglarCodificacion(body, request.getContentType());

        HttpRequest.BodyPublisher contenido = (body == null || body.length == 0)
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofByteArray(body);
        peticion.method(request.getMethod(), contenido);

        try {
            HttpResponse<byte[]> respuesta = http.send(peticion.build(), HttpResponse.BodyHandlers.ofByteArray());

            HttpHeaders headers = new HttpHeaders();
            respuesta.headers().firstValue(HttpHeaders.CONTENT_TYPE)
                    .ifPresent(tipo -> headers.set(HttpHeaders.CONTENT_TYPE, tipo));

            return new ResponseEntity<>(respuesta.body(), headers, HttpStatusCode.valueOf(respuesta.statusCode()));

        } catch (ConnectException | HttpTimeoutException e) {
            // el microservicio de esa ruta esta apagado: los demas siguen funcionando
            return error(503, "Service Unavailable", "El servicio " + destino + " no esta disponible", path);
        } catch (IOException e) {
            return error(502, "Bad Gateway", "Error al comunicarse con " + destino, path);
        }
    }

    // curl en Windows (Git Bash, cmd) a veces manda las tildes en Latin-1 y no en UTF-8,
    // y Jackson lo toma como JSON mal formado. Si el JSON no es UTF-8 valido lo convertimos.
    private byte[] arreglarCodificacion(byte[] body, String contentType) {
        if (body == null || body.length == 0 || contentType == null
                || !contentType.toLowerCase().contains("json")) {
            return body;
        }
        try {
            StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(body));
            return body; // ya venia bien
        } catch (CharacterCodingException e) {
            String texto = new String(body, Charset.forName("windows-1252"));
            return texto.getBytes(StandardCharsets.UTF_8);
        }
    }

    private String buscarDestino(String path) {
        for (Map.Entry<String, String> ruta : rutas.entrySet()) {
            String prefijo = ruta.getKey();
            if (path.equals(prefijo) || path.startsWith(prefijo + "/")) {
                return ruta.getValue();
            }
        }
        return null;
    }

    // mismo formato de error que usan los microservicios
    private ResponseEntity<byte[]> error(int status, String error, String mensaje, String path) {
        String json = "{"
                + "\"timestamp\":\"" + LocalDateTime.now() + "\","
                + "\"status\":" + status + ","
                + "\"error\":\"" + error + "\","
                + "\"message\":\"" + mensaje.replace("\"", "'") + "\","
                + "\"path\":\"" + path.replace("\"", "'") + "\","
                + "\"details\":{}"
                + "}";
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(json.getBytes(StandardCharsets.UTF_8));
    }
}