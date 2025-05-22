package com.nttdata.product.service;

import com.nttdata.product.model.BankProduct;
import com.nttdata.product.model.CustomerDTO;
import com.nttdata.product.model.CustomerType;
import com.nttdata.product.model.ProductType;
import com.nttdata.product.repository.BankProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class BankProductService {

    private final BankProductRepository repository;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("http://localhost:8080") // URL de customer-service
            .build();

    public Flux<BankProduct> getAll() {
        return repository.findAll();
    }

    public Mono<BankProduct> getById(String id) {
        return repository.findById(id);
    }

    public Mono<BankProduct> create(BankProduct product) {
        return webClient.get()
                .uri("/customers/{id}", product.getCustomerId())
                .retrieve()
                .bodyToMono(CustomerDTO.class)
                .flatMap(customer -> validateBusinessRules(customer, product));
    }

    private Mono<BankProduct> validateBusinessRules(CustomerDTO customer, BankProduct product) {
        if (isPersonalCustomerWithPersonalLoan(customer, product)) {
            return validateSinglePersonalLoan(product);
        }

        if (isRestrictedProductForBusiness(customer, product)) {
            return Mono.error(new IllegalArgumentException("Los clientes comerciales no pueden tener cuentas de ahorro o de plazo fijo."));
        }

        if (isBusinessCurrentAccount(customer, product)) {
            return validateBusinessAccountHolders(product);
        }

        if (isUniquePassiveAccountForPersonal(customer, product)) {
            return validateUniquePassiveAccount(product);
        }

        // Allowed cases
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

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<String>> handleBadRequest(IllegalArgumentException ex) {
        return Mono.just(ResponseEntity.badRequest().body(ex.getMessage()));
    }

    private boolean isPersonalCustomerWithPersonalLoan(CustomerDTO customer, BankProduct product) {
        return customer.getType() == CustomerType.PERSONAL &&
                product.getType() == ProductType.CREDITO_PERSONAL;
    }

    private Mono<BankProduct> validateSinglePersonalLoan(BankProduct product) {
        return repository.findByCustomerId(product.getCustomerId())
                .filter(p -> p.getType() == ProductType.CREDITO_PERSONAL)
                .hasElements()
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalArgumentException(
                                "Los clientes personales sólo pueden tener un producto de crédito personal."));
                    }
                    return repository.save(product);
                });
    }

    private boolean isRestrictedProductForBusiness(CustomerDTO customer, BankProduct product) {
        return customer.getType() == CustomerType.BUSINESS &&
                (product.getType() == ProductType.AHORRO || product.getType() == ProductType.PLAZO_FIJO);
    }

    private boolean isBusinessCurrentAccount(CustomerDTO customer, BankProduct product) {
        return customer.getType() == CustomerType.BUSINESS &&
                product.getType() == ProductType.CORRIENTE;
    }

    private Mono<BankProduct> validateBusinessAccountHolders(BankProduct product) {
        if (product.getHolders() == null || product.getHolders().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Las cuentas corrientes comerciales deben tener al menos un titular."));
        }
        return repository.save(product);
    }

    private boolean isUniquePassiveAccountForPersonal(CustomerDTO customer, BankProduct product) {
        return customer.getType() == CustomerType.PERSONAL &&
                (product.getType() == ProductType.AHORRO || product.getType() == ProductType.CORRIENTE);
    }

    private Mono<BankProduct> validateUniquePassiveAccount(BankProduct product) {
        return repository.findByCustomerId(product.getCustomerId())
                .filter(p -> p.getType() == product.getType())
                .hasElements()
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalArgumentException(
                                "Personal customers can only have one " + product.getType().name() + " account."));
                    }
                    return repository.save(product);
                });
    }
}
