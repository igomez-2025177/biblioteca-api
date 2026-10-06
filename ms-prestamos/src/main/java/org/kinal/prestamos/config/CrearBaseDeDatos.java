package org.kinal.prestamos.config;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CrearBaseDeDatos implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        Environment env = event.getEnvironment();

        String url = env.getProperty("spring.datasource.url");
        String usuario = env.getProperty("spring.datasource.username");
        String password = env.getProperty("spring.datasource.password");

        if (url == null || !url.startsWith("jdbc:postgresql://")) {
            return;
        }

        int ultimaBarra = url.lastIndexOf('/');
        String nombreBd = url.substring(ultimaBarra + 1).split("\\?")[0];
        String urlServidor = url.substring(0, ultimaBarra + 1) + "postgres";

        try (Connection conexion = DriverManager.getConnection(urlServidor, usuario, password);
             PreparedStatement consulta = conexion.prepareStatement(
                     "SELECT 1 FROM pg_database WHERE datname = ?")) {

            consulta.setString(1, nombreBd);

            try (ResultSet rs = consulta.executeQuery()) {
                if (rs.next()) {
                    return;
                }
            }

            try (Statement st = conexion.createStatement()) {
                st.executeUpdate("CREATE DATABASE \"" + nombreBd.replace("\"", "") + "\"");
                System.out.println(">>> Base de datos '" + nombreBd + "' creada");
            }

        } catch (SQLException e) {
            // si otro microservicio la creo al mismo tiempo, o no hay permisos, solo avisamos
            System.err.println(">>> No se pudo verificar/crear la base de datos: " + e.getMessage());
        }
    }
}