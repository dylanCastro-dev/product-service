package com.nttdata.product.controller;

import com.nttdata.product.service.DebitCardService;
import com.nttdata.product.utils.Constants;
import com.nttdata.product.utils.DebitCardMapper;
import com.nttdata.product.utils.Utils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.openapitools.api.DebitCardsApi;
import org.openapitools.model.CardTemplateResponse;
import org.openapitools.model.CardBody;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import static com.nttdata.product.utils.DebitCardMapper.toResponse;


@RestController
@RequiredArgsConstructor
public class DebitCardController implements DebitCardsApi {
    private static final Logger log = LoggerFactory.getLogger(DebitCardController.class);
    private final DebitCardService service;

    /**
     * Registra una nueva tarjeta de débito.
     */
    @Override
    public Mono<ResponseEntity<CardTemplateResponse>> createDebitCard
    (@RequestBody Mono<CardBody> request, ServerWebExchange exchange) {
        return request
                .doOnNext(req -> log.debug("Request recibido: {}", req))
                .doOnNext(Utils::validateCardBody)
                .map(DebitCardMapper::toDebitCard)
                .flatMap(service::createDebitCard)
                .map(card -> toResponse(card, 201, Constants.SUCCESS_CREATE_CARD))
                .map(ResponseEntity::ok);
    }

    /**
     * Obtiene una tarjeta de débito por su ID.
     */
    @Override
    public Mono<ResponseEntity<CardTemplateResponse>> getDebitCardById(String id, ServerWebExchange exchange) {
        return service.getById(id)
                .map(card -> toResponse(card, 200, Constants.SUCCESS_FIND_CARD))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new CardTemplateResponse()
                                .status(404)
                                .message(Constants.ERROR_FIND_CARD)
                                .cards(null)));
    }
}