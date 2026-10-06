package org.kinal.prestamos;

import org.kinal.prestamos.config.CrearBaseDeDatos;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PrestamosApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(PrestamosApplication.class);
        // crea biblioteca_db si no existe, antes de conectarse
        app.addListeners(new CrearBaseDeDatos());
        app.run(args);
    }
}