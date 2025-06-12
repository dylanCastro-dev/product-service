package com.nttdata.product.utils;

import org.openapitools.model.BankProductTemplateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BankProductTemplateResponse> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Error de validación: {}", e.getMessage());
        return ResponseEntity
                .badRequest()
                .body(new BankProductTemplateResponse()
                        .status(400)
                        .message(String.format(Constants.ERROR_VALIDATION_MESSAGE, e.getMessage()))
                        .products(null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BankProductTemplateResponse> handleGeneralException(Exception e) {
        log.error("Error inesperado: ", e);
        return ResponseEntity
                .status(500)
                .body(new BankProductTemplateResponse()
                        .status(500)
                        .message(Constants.ERROR_INTERNAL)
                        .products(null));
    }

    @ExceptionHandler(EmptyResultException.class)
    public ResponseEntity<BankProductTemplateResponse> handleEmptyResult(EmptyResultException e) {
        return ResponseEntity
                .status(500)
                .body(new BankProductTemplateResponse()
                        .status(404)
                        .message(e.getMessage())
                        .products(null));
    }
}
