package org.kinal.reportes;

import org.kinal.reportes.config.CrearBaseDeDatos;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ReportesApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ReportesApplication.class);
        // crea biblioteca_db si no existe, antes de conectarse
        app.addListeners(new CrearBaseDeDatos());
        app.run(args);
    }
}