package com.nttdata.product.utils;

import org.openapitools.model.TemplateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<TemplateResponse> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Error de validación: {}", e.getMessage());
        return ResponseEntity
                .badRequest()
                .body(new TemplateResponse()
                        .status(400)
                        .message(String.format(Constants.ERROR_VALIDATION_MESSAGE, e.getMessage()))
                        .products(null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<TemplateResponse> handleGeneralException(Exception e) {
        log.error("Error inesperado: ", e);
        return ResponseEntity
                .status(500)
                .body(new TemplateResponse()
                        .status(500)
                        .message(Constants.ERROR_INTERNAL)
                        .products(null));
    }

    @ExceptionHandler(EmptyResultException.class)
    public ResponseEntity<TemplateResponse> handleEmptyResult(EmptyResultException e) {
        return ResponseEntity
                .status(500)
                .body(new TemplateResponse()
                        .status(404)
                        .message(e.getMessage())
                        .products(null));
    }
}
