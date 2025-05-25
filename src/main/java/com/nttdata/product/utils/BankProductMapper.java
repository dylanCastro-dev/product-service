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
}
