package com.nttdata.product.utils;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.nttdata.product.model.Details.CurrentAccount;
import com.nttdata.product.model.Details.FixedTermAccount;
import com.nttdata.product.model.Details.ProductDetails;
import com.nttdata.product.model.Details.SavingsAccount;
import com.nttdata.product.model.Details.CreditProduct;
import com.nttdata.product.model.Type.ProductType;
import org.openapitools.model.BankProductBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.Map;
import java.util.function.Supplier;

public class Utils {
    private static final Logger log = LoggerFactory.getLogger(Utils.class);
    public static final Map<ProductType, Class<? extends ProductDetails>> expectedDetailsByType = Map.of(
            ProductType.SAVINGS, SavingsAccount.class,
            ProductType.CURRENT, CurrentAccount.class,
            ProductType.FIXED_TERM, FixedTermAccount.class,
            ProductType.CREDIT, CreditProduct.class
    );

    private static Supplier<WebClient> customerService =
            () -> WebClient.builder()
                    .baseUrl("http://localhost:8080") // URL del customer-service
                    .build();

    public static void setCustomerService(Supplier<WebClient> supplier) {
        customerService = supplier;
    }

    public static Supplier<WebClient> getCustomerService() {
        return customerService;
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
                errors.append("type debe ser uno de los valores permitidos: " +
                        "AHORRO, CORRIENTE, PLAZO_FIJO, CREDITO. ");
            }
        }

        if (body.getName() == null || body.getName().trim().isEmpty()) {
            errors.append("name es obligatorio. ");
        }

        if (body.getBalance() == null || body.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            errors.append("balance debe ser mayor o igual a 0. ");
        }

        if (body.getDetails() != null) {
            ObjectMapper mapper = new ObjectMapper();

            try {
                switch (ProductType.valueOf(body.getType())) {
                    case SAVINGS:
                        mapper.convertValue(body.getDetails(), SavingsAccount.class);
                        break;
                    case CREDIT:
                        mapper.convertValue(body.getDetails(), CreditProduct.class);
                        break;
                    case CURRENT:
                        mapper.convertValue(body.getDetails(), CurrentAccount.class);
                        break;
                    case FIXED_TERM:
                        mapper.convertValue(body.getDetails(), FixedTermAccount.class);
                        break;
                    default:
                        errors.append("Tipo no soportado: ").append(body.getType()).append(". ");
                }
            } catch (IllegalArgumentException | ClassCastException ex) {
                errors.append("Los campos de 'details' no corresponden a la clase esperada para el tipo ")
                        .append(body.getType()).append(". ");
            }

            // Lanzar excepción si se detectaron errores
            if (errors.length() > 0) {
                throw new IllegalArgumentException(errors.toString().trim());
            }

        }

    }
}
