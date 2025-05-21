package com.nttdata.product.repository;

import com.nttdata.product.model.BankProduct;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankProductRepository extends ReactiveMongoRepository<BankProduct, String> {
}
