package com.nttdata.product.service.impl;

import com.nttdata.product.controller.BankProductController;
import com.nttdata.product.model.BankProduct;
import com.nttdata.product.model.Dto.CustomerDTO;
import com.nttdata.product.model.Type.CustomerType;
import com.nttdata.product.model.Type.ProductType;
import com.nttdata.product.repository.BankProductRepository;
import com.nttdata.product.service.BankProductService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class BankProductServiceImpl implements BankProductService {
    private static final Logger log = LoggerFactory.getLogger(BankProductServiceImpl.class);
    private final BankProductRepository repository;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("http://localhost:8080") // URL de customer-service
            .build();

    @Override
    public Flux<BankProduct> getAll() {
        return repository.findAll();
    }

    @Override
    public Mono<BankProduct> getById(String id) {
        return repository.findById(id);
    }

    @Override
    public Mono<BankProduct> create(BankProduct product) {
        return webClient.get()
                .uri("/customers/{id}", product.getCustomerId())
                .retrieve()
                .bodyToMono(CustomerDTO.class)
                .flatMap(customer -> validateRules(customer, product));
    }

    private Mono<BankProduct> validateRules(CustomerDTO customer, BankProduct product) {
        // Valida si es cliente personal con tarjeta de credito
        if (isPersonalCustomerWithPersonalLoan(customer, product)) {
            // Un cliente personal solo puede tener un crédito personal
            return validateSinglePersonalLoan(product);
        }

        // Valida si es cliente es empresarial con cuenta de ahorro o plazo fijo
        if (isRestrictedProductForBusiness(customer, product)) {
            return Mono.error(new IllegalArgumentException("Los clientes comerciales no pueden tener cuentas de ahorro o de plazo fijo."));
        }

        // Valida si es cliente empresarial con cuenta corriente
        if (isBusinessCurrentAccount(customer, product)) {
            // Las cuentas corrientes empresariales deben tener al menos un titular definido
            return validateBusinessAccountHolders(product);
        }

        // Valida si es cliente es personal con cuenta de ahorro o corriente
        if (isUniquePassiveAccountForPersonal(customer, product)) {
            // Un cliente personal no puede tener más de un producto pasivo del mismo tipo
            return validateUniquePassiveAccount(product);
        }

        // Si cumple todas las reglas, se guarda el producto
        return repository.save(product);
    }

    @Override
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

    @Override
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
                                "Los clientes personales solo pueden tener una cuenta de tipo " + product.getType().name() + "."));
                    }
                    return repository.save(product);
                });
    }
}
