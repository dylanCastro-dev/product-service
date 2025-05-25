package com.nttdata.product.utils;

import org.openapitools.model.BankProductResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BankProductResponse> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Error de validación: {}", e.getMessage());
        return ResponseEntity
                .badRequest()
                .body(new BankProductResponse()
                        .status(400)
                        .message("Error de validación: " + e.getMessage())
                        .products(null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BankProductResponse> handleGeneralException(Exception e) {
        log.error("Error inesperado: ", e);
        return ResponseEntity
                .status(500)
                .body(new BankProductResponse()
                        .status(500)
                        .message("Error interno del sistema")
                        .products(null));
    }
}
