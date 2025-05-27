package com.nttdata.product.utils;

import com.nttdata.product.model.Type.ProductType;
import com.nttdata.product.service.impl.BankProductServiceImpl;
import org.openapitools.model.BankProductBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;

public class Utils {
    private static final Logger log = LoggerFactory.getLogger(Utils.class);

    private static final WebClient CUSTOMER_CLIENT = WebClient.builder()
            .baseUrl("http://localhost:8080") // URL de customer-service
            .build();

    public static WebClient getCustomerService() {
        return CUSTOMER_CLIENT;
    }

    public static void validateBankProductBody(BankProductBody body) {
        StringBuilder errors = new StringBuilder();

        // Validar campos tipo String no vacíos
        if (body.getCustomerId() == null || body.getCustomerId().trim().isEmpty()) {
            errors.append("customerId es obligatorio. ");
        }

        if (body.getType() == null || body.getType().trim().isEmpty()) {
            errors.append("type es obligatorio. ");
        } else {
            try {
                ProductType.valueOf(body.getType().trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                errors.append("type debe ser uno de los valores permitidos: AHORRO, CORRIENTE, PLAZO_FIJO, CREDITO_PERSONAL, CREDITO_EMPRESARIAL, TARJETA_CREDITO. ");
            }
        }

        if (body.getName() == null || body.getName().trim().isEmpty()) {
            errors.append("name es obligatorio. ");
        }

        if (body.getBalance() == null || body.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            errors.append("balance debe ser mayor o igual a 0. ");
        }

        if (body.getMaintenanceFee() == null || body.getMaintenanceFee() < 0) {
            errors.append("maintenanceFee debe ser mayor o igual a 0. ");
        }

        if (body.getCreditLimit() == null || body.getCreditLimit().compareTo(BigDecimal.ZERO) < 0) {
            errors.append("creditLimit debe ser mayor o igual a 0. ");
        }

        if (body.getMonthlyLimit() == null || body.getMonthlyLimit() < 0) {
            errors.append("monthlyLimit debe ser mayor o igual a 0. ");
        }

        // Lanzar excepción si se detectaron errores
        if (errors.length() > 0) {
            throw new IllegalArgumentException(errors.toString().trim());
        }
    }
}
