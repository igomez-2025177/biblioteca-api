package org.kinal.usuarios;

import org.kinal.usuarios.config.CrearBaseDeDatos;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UsuariosApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(UsuariosApplication.class);
        app.addListeners(new CrearBaseDeDatos());
        app.run(args);
    }
}