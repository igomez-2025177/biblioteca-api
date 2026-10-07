package org.kinal.reportes.exception;

public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String mensaje) {
        super(mensaje);
    }
}