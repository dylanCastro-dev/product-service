package com.nttdata.product.service;

import com.nttdata.product.model.DebitCard;
import reactor.core.publisher.Mono;

public interface DebitCardService {

    /**
     * Crea una nueva tarjeta de débito.
     */
    Mono<DebitCard> createDebitCard(DebitCard card);

    /**
     * Obtiene una tarjeta por su ID.
     */
    Mono<DebitCard> getById(String id);
}
