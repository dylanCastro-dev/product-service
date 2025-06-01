package com.nttdata.product.utils;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.nttdata.product.model.Details.CurrentAccount;
import com.nttdata.product.model.Details.FixedTermAccount;
import com.nttdata.product.model.Details.ProductDetails;
import com.nttdata.product.model.Details.SavingsAccount;
import com.nttdata.product.model.Details.CreditProduct;
import com.nttdata.product.model.Type.ProductStatus;
import com.nttdata.product.model.Type.ProductType;
import org.openapitools.model.BankProductBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
                String allowedTypes = Arrays.stream(ProductType.values())
                        .map(Enum::name)
                        .collect(Collectors.joining(", "));
                errors.append("type debe ser uno de los valores permitidos: ").append(allowedTypes).append(". ");
            }
        }

        if (body.getStatus() == null || body.getStatus().trim().isEmpty()) {
            errors.append("type es obligatorio. ");
        } else {
            try {
                ProductStatus.valueOf(body.getStatus().trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                String allowedTypes = Arrays.stream(ProductStatus.values())
                        .map(Enum::name)
                        .collect(Collectors.joining(", "));
                errors.append("type debe ser uno de los valores permitidos: ").append(allowedTypes).append(". ");
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
                        SavingsAccount savings = mapper.convertValue(body.getDetails(), SavingsAccount.class);
                        if (savings.getMaintenanceFee() == null || savings.getMaintenanceFee() < 0) {
                            errors.append("maintenanceFee debe ser mayor o igual a 0. ");
                        }

                        if (savings.getMonthlyLimit() == null || savings.getMonthlyLimit() < 0) {
                            errors.append("monthlyLimit debe ser mayor o igual a 0. ");
                        }

                        if (savings.getRequiredMonthlyAverageBalance() == null ||
                                savings.getRequiredMonthlyAverageBalance() < 0) {
                            errors.append("requiredMonthlyAverageBalance debe ser mayor o igual a 0. ");
                        }

                        if (savings.getTransactionFee() == null || savings.getTransactionFee() < 0) {
                            errors.append("transactionFee debe ser mayor o igual a 0. ");
                        }

                        if (savings.getFreeMonthlyTransactionLimit() == null ||
                                savings.getFreeMonthlyTransactionLimit() <= 0) {
                            errors.append("freeMonthlyTransactionLimit debe ser mayor a 0. ");
                        }
                        break;
                    case CREDIT:
                        CreditProduct credit = mapper.convertValue(body.getDetails(), CreditProduct.class);
                        if (credit.getCreditLimit() == null || credit.getCreditLimit().compareTo(BigDecimal.ZERO) < 0) {
                            errors.append("creditLimit debe ser mayor o igual a 0. ");
                        }
                        break;
                    case CURRENT:
                        CurrentAccount current = mapper.convertValue(body.getDetails(), CurrentAccount.class);
                        if (current.getMaintenanceFee() == null || current.getMaintenanceFee() < 0) {
                            errors.append("maintenanceFee debe ser mayor o igual a 0. ");
                        }

                        if (current.getMonthlyLimit() == null || current.getMonthlyLimit() < 0) {
                            errors.append("monthlyLimit debe ser mayor o igual a 0. ");
                        }

                        if (current.getTransactionFee() == null || current.getTransactionFee() < 0) {
                            errors.append("transactionFee debe ser mayor o igual a 0. ");
                        }

                        if (current.getFreeMonthlyTransactionLimit() == null ||
                                current.getFreeMonthlyTransactionLimit() <= 0) {
                            errors.append("freeMonthlyTransactionLimit debe ser mayor a 0. ");
                        }
                        break;
                    case FIXED_TERM:
                        FixedTermAccount fixed = mapper.convertValue(body.getDetails(), FixedTermAccount.class);
                        if (fixed.getMaintenanceFee() == null || fixed.getMaintenanceFee() < 0) {
                            errors.append("maintenanceFee debe ser mayor o igual a 0. ");
                        }

                        if (fixed.getMonthlyLimit() == null || fixed.getMonthlyLimit() < 0) {
                            errors.append("monthlyLimit debe ser mayor o igual a 0. ");
                        }
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
