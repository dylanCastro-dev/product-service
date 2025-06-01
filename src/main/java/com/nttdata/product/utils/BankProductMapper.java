package com.nttdata.product.utils;

import com.nttdata.product.model.BankProduct;
import com.nttdata.product.model.Type.ProductType;
import org.openapitools.model.BankProductBody;
import org.openapitools.model.BankProductResponse;
import org.openapitools.model.BankProductResponse;
import org.openapitools.model.TemplateResponse;

import java.util.List;
import java.util.stream.Collectors;

public class BankProductMapper {
    public static BankProductResponse toBankProductResponse(BankProduct product) {
        return new BankProductResponse()
                .id(product.getId())
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

    public static TemplateResponse toResponse(BankProduct product, int status, String message) {
        return new TemplateResponse()
                .status(status)
                .message(message)
                .addProductsItem(toBankProductResponse(product));
    }

    public static TemplateResponse toResponse(List<BankProduct> lstProduct, int status, String message) {
        List<BankProductResponse> products = lstProduct.stream()
                .map(BankProductMapper::toBankProductResponse)
                .collect(Collectors.toList());

        return new TemplateResponse()
                .status(status)
                .message(message)
                .products(products);
    }

    public static TemplateResponse toResponse(int status, String message) {
        return new TemplateResponse()
                .status(status)
                .message(message)
                .products(null);
    }
}
