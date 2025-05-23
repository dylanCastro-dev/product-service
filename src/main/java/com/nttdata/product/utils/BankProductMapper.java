package com.nttdata.product.utils;

import com.nttdata.product.model.BankProduct;
import com.nttdata.product.model.Type.ProductType;
import org.openapitools.model.BankProductBody;
import org.openapitools.model.BankProductResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class BankProductMapper {
    public static BankProductBody toBankProductBody(BankProduct product) {
        return new BankProductBody()
                .customerId(product.getCustomerId())
                .type(product.getType().name())
                .name(product.getName())
                .balance(product.getBalance())
                .maintenanceFee(product.getMaintenanceFee())
                .monthlyLimit(product.getMonthlyLimit())
                .creditLimit(product.getCreditLimit())
                .holders(product.getHolders())
                .signers(product.getSigners())
                .allowedTransactionDay(product.getAllowedTransactionDay());
    }

    public static BankProduct toBankProduct(BankProductBody request) {
        return BankProduct.builder()
                .customerId(request.getCustomerId())
                .type(ProductType.valueOf(request.getType()))
                .name(request.getName())
                .balance(request.getBalance())
                .maintenanceFee(request.getMaintenanceFee())
                .monthlyLimit(request.getMonthlyLimit())
                .creditLimit(request.getCreditLimit())
                .holders(request.getHolders())
                .signers(request.getSigners())
                .allowedTransactionDay(request.getAllowedTransactionDay())
                .build();
    }

    public static BankProductResponse toResponse(BankProduct product, int status, String message) {
        return new BankProductResponse()
                .status(status)
                .message(message)
                .addProductsItem(toBankProductBody(product));
    }

    public static BankProductResponse toResponse(List<BankProduct> lstProduct, int status, String message) {
        List<BankProductBody> products = lstProduct.stream()
                .map(BankProductMapper::toBankProductBody)
                .collect(Collectors.toList());

        return new BankProductResponse()
                .status(status)
                .message(message)
                .products(products);
    }

    public static BankProductResponse toResponse(int status, String message) {
        return new BankProductResponse()
                .status(status)
                .message(message)
                .products(null);
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

        if (body.getAllowedTransactionDay() == null ||
                body.getAllowedTransactionDay() < 1 || body.getAllowedTransactionDay() > 31) {
            errors.append("allowedTransactionDay debe estar entre 1 y 31. ");
        }

        // Validar arrays requeridos no vacíos
        if (body.getHolders() == null || body.getHolders().isEmpty()) {
            errors.append("holders no puede estar vacío. ");
        }

        if (body.getSigners() == null || body.getSigners().isEmpty()) {
            errors.append("signers no puede estar vacío. ");
        }

        // Lanzar excepción si se detectaron errores
        if (errors.length() > 0) {
            throw new IllegalArgumentException(errors.toString().trim());
        }
    }


}
