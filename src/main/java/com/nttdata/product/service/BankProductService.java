package com.nttdata.product.service;

import com.nttdata.product.model.BankProduct;
import com.nttdata.product.repository.BankProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class BankProductService {

    private final BankProductRepository repository;

    public Flux<BankProduct> getAll() {
        return repository.findAll();
    }

    public Mono<BankProduct> getById(String id) {
        return repository.findById(id);
    }

    public Mono<BankProduct> create(BankProduct product) {
        return repository.save(product);
    }

    public Mono<BankProduct> update(String id, BankProduct product) {
        return repository.findById(id)
                .flatMap(existing -> {
                    existing.setType(product.getType());
                    existing.setBalance(product.getBalance());
                    existing.setCustomerId(product.getCustomerId());
                    existing.setMaintenanceFee(product.getMaintenanceFee());
                    existing.setMonthlyLimit(product.getMonthlyLimit());
                    existing.setName(product.getName());
                    return repository.save(existing);
                });
    }

    public Mono<Void> delete(String id) {
        return repository.deleteById(id);
    }
}
