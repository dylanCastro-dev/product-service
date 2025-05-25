package com.nttdata.product.controller;

import com.nttdata.product.service.BankProductService;
import com.nttdata.product.utils.BankProductMapper;
import com.nttdata.product.utils.Constants;
import com.nttdata.product.utils.Utils;
import lombok.RequiredArgsConstructor;
import org.openapitools.api.ProductsApi;
import org.openapitools.model.BankProductBody;
import org.openapitools.model.BankProductResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import static com.nttdata.product.utils.BankProductMapper.*;

@RestController
@RequiredArgsConstructor
public class BankProductController implements ProductsApi {
    private static final Logger log = LoggerFactory.getLogger(BankProductController.class);

    private final BankProductService service;

    @Override
    public Mono<ResponseEntity<BankProductResponse>> getAllProducts(ServerWebExchange exchange) {
        return service.getAll()
                .collectList()
                .map(products -> toResponse(products, 200, Constants.SUCCESS_FIND_LIST_PRODUCT))
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<BankProductResponse>> getProductById(String id, ServerWebExchange exchange) {
        return service.getById(id)
                .map(product -> toResponse(product, 200, Constants.SUCCESS_FIND_PRODUCT))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new BankProductResponse()
                                .status(404)
                                .message(Constants.ERROR_FIND_PRODUCT)
                                .products(null)));
    }


    @Override
    public Mono<ResponseEntity<BankProductResponse>> createProduct(
            @RequestBody Mono<BankProductBody> request, ServerWebExchange exchange) {

        return request
                .doOnNext(req -> log.debug("Request recibido: {}", req))
                .doOnNext(Utils::validateBankProductBody)
                .map(BankProductMapper::toBankProduct)
                .flatMap(service::create)
                .map(product -> toResponse(product, 201, Constants.SUCCESS_CREATE_PRODUCT))
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<BankProductResponse>> updateProduct(
            String id,
            Mono<BankProductBody> request,
            ServerWebExchange exchange) {

        return request
                .doOnNext(req -> log.debug("Request recibido: {}", req))
                .doOnNext(Utils::validateBankProductBody)
                .map(BankProductMapper::toBankProduct)
                .flatMap(product -> service.update(id, product))
                .map(product -> toResponse(product, 200, Constants.SUCCESS_UPDATE_PRODUCT))
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<BankProductResponse>> deleteProduct(String id, ServerWebExchange exchange) {
        return service.delete(id)
                .map(product -> toResponse(200, Constants.SUCCESS_DELETE_PRODUCT))
                .map(ResponseEntity::ok);
    }
}
