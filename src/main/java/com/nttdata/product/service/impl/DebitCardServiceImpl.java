package com.nttdata.product.service.impl;

import com.nttdata.product.model.DebitCard;
import com.nttdata.product.repository.DebitCardRepository;
import com.nttdata.product.service.DebitCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class DebitCardServiceImpl implements DebitCardService {
    private final DebitCardRepository repository;

    @Override
    public Mono<DebitCard> createDebitCard(DebitCard card) {
        return repository.save(card);
    }


    @Override
    public Mono<DebitCard> getById(String id) {
        return repository.findById(id);
    }
}
