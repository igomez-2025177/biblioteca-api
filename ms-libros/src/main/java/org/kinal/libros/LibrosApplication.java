package org.kinal.libros;

import org.kinal.libros.config.CrearBaseDeDatos;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LibrosApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(LibrosApplication.class);
        // crea biblioteca_db si no existe, antes de conectarse
        app.addListeners(new CrearBaseDeDatos());
        app.run(args);
    }
}