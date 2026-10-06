package org.kinal.prestamos.service;

import lombok.RequiredArgsConstructor;
import org.kinal.prestamos.entity.EstadoPrestamo;
import org.kinal.prestamos.repository.PrestamoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Todos los dias a las 00:05 pasa a ATRASADO los prestamos vencidos.
 * La sancion del usuario NO se pone aqui: segun el enunciado se aplica
 * cuando intenta hacer un nuevo prestamo.
 */
@Component
@RequiredArgsConstructor
public class PrestamoScheduler {

    private static final Logger log = LoggerFactory.getLogger(PrestamoScheduler.class);

    private final PrestamoRepository prestamoRepository;

    @Scheduled(cron = "0 5 0 * * *")
    @Transactional
    public void marcarAtrasados() {
        int cambiados = prestamoRepository.marcarVencidos(
                EstadoPrestamo.ACTIVO, EstadoPrestamo.ATRASADO, LocalDate.now());
        log.info("Prestamos marcados como ATRASADO: {}", cambiados);
    }
}