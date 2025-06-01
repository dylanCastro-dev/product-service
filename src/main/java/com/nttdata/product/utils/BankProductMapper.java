package com.nttdata.product.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nttdata.product.model.BankProduct;
import com.nttdata.product.model.Details.ProductDetails;
import com.nttdata.product.model.Type.ProductStatus;
import com.nttdata.product.model.Type.ProductType;

import org.openapitools.model.BankProductBody;
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
                .status(product.getStatus().name())
                .name(product.getName())
                .balance(product.getBalance())
                .details(product.getDetails())
                .holders(product.getHolders())
                .signers(product.getSigners());
    }

    public static BankProduct toBankProduct(BankProductBody request) {
        return BankProduct.builder()
                .customerId(request.getCustomerId())
                .type(ProductType.valueOf(request.getType()))
                .status(ProductStatus.valueOf(request.getStatus()))
                .name(request.getName())
                .balance(request.getBalance())
                .details(convertRawDetails(request.getDetails(), ProductType.valueOf(request.getType())))
                .holders(request.getHolders())
                .signers(request.getSigners())
                .build();
    }

    public static ProductDetails convertRawDetails(Object raw, ProductType type) {
        Class<? extends ProductDetails> clazz = Utils.expectedDetailsByType.get(type);
        return new ObjectMapper().convertValue(raw, clazz);
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
