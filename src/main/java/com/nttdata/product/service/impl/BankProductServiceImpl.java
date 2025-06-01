package com.nttdata.product.service.impl;

import com.nttdata.product.model.BankProduct;
import com.nttdata.product.model.Dto.CustomerDTO;
import com.nttdata.product.model.Dto.CustomerResponse;
import com.nttdata.product.model.Type.CustomerType;
import com.nttdata.product.model.Type.ProductType;
import com.nttdata.product.repository.BankProductRepository;
import com.nttdata.product.service.BankProductService;
import com.nttdata.product.utils.Constants;
import com.nttdata.product.utils.EmptyResultException;
import com.nttdata.product.utils.Utils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class BankProductServiceImpl implements BankProductService {
    private static final Logger log = LoggerFactory.getLogger(BankProductServiceImpl.class);
    private final BankProductRepository repository;

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
        return Utils.getCustomerService().get().get()
                .uri("/customers/{id}", product.getCustomerId())
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response -> {
                    if (response.statusCode() == HttpStatus.NOT_FOUND) {
                        return Mono.error(new EmptyResultException(Constants.ERROR_FIND_CUSTOMER));
                    }
                    return Mono.error(new RuntimeException("Error en la solicitud: " + response.statusCode()));
                })
                .bodyToMono(CustomerResponse.class)
                .flatMap(response -> {
                    CustomerDTO customer = response.getCustomers().get(0);
                    return validateRules(customer, product);
                });
    }

    private Mono<BankProduct> validateRules(CustomerDTO customer, BankProduct product) {
        return Mono.empty()
                .then(isPassiveProduct(product.getType()) ?
                        //Valida las reglas específicas de negocio para productos pasivos
                        validatePassiveProductProperties(product)
                        : Mono.empty())
                .then(isPersonalCustomerWithPersonalLoan(customer, product) ?
                        // Valida si es cliente personal con tarjeta de credito
                        // Un cliente personal solo puede tener un crédito personal
                        validateSinglePersonalLoan(product)
                        : Mono.empty())
                .then(isRestrictedProductForBusiness(customer, product) ?
                        // Valida si es cliente es empresarial con cuenta de ahorro o plazo fijo
                        Mono.error(new IllegalArgumentException(Constants.ERROR_BUSINESS_CANNOT_HAVE_PASSIVE_ACCOUNTS))
                        : Mono.empty())
                .then(isBusinessCurrentAccount(customer, product) ?
                        // Valida si es cliente empresarial con cuenta corriente
                        // Las cuentas corrientes empresariales deben tener al menos un titular definido
                        validateBusinessAccountHolders(product)
                        : Mono.empty())
                .then(isUniquePassiveAccountForPersonal(customer, product) ?
                        // Valida si es cliente es personal con cuenta de ahorro o corriente
                        // Un cliente personal no puede tener más de un producto pasivo del mismo tipo
                        validateUniquePassiveAccount(product)
                        : Mono.empty())
                .then(repository.save(product)); // Si cumple todas las reglas, se guarda el producto
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
                        return Mono.error(new IllegalArgumentException(Constants.ERROR_PERSONAL_ONE_CREDIT_ONLY));
                    }
                    return Mono.empty();
                });
    }

    private Mono<BankProduct> validatePassiveProductProperties(BankProduct product) {
        switch (product.getType()) {
            case AHORRO:
                if (product.getMaintenanceFee() != null && product.getMaintenanceFee() > 0) {
                    return Mono.error(new IllegalArgumentException(Constants.ERROR_SAVINGS_NO_MAINTENANCE_FEE));
                }
                if (product.getMonthlyLimit() == null || product.getMonthlyLimit() <= 0) {
                    return Mono.error(new IllegalArgumentException(Constants.ERROR_SAVINGS_REQUIRE_MONTHLY_LIMIT));
                }
                break;

            case CORRIENTE:
                if (product.getMaintenanceFee() == null || product.getMaintenanceFee() <= 0) {
                    return Mono.error(new IllegalArgumentException(Constants.ERROR_CURRENT_ACCOUNT_REQUIRES_FEE));
                }
                if (product.getMonthlyLimit() != null && product.getMonthlyLimit() > 0) {
                    return Mono.error(new IllegalArgumentException(Constants.ERROR_CURRENT_ACCOUNT_NO_MONTHLY_LIMIT));
                }
                break;

            case PLAZO_FIJO:
                if (product.getMaintenanceFee() != null && product.getMaintenanceFee() > 0) {
                    return Mono.error(new IllegalArgumentException(Constants.ERROR_FIXED_TERM_NO_MAINTENANCE_FEE));
                }

                if (product.getMonthlyLimit() != null && product.getMonthlyLimit() != 1) {
                    return Mono.error(new IllegalArgumentException(Constants.ERROR_FIXED_TERM_REQUIRE_MONTHLY_LIMIT));
                }
                if (product.getAllowedTransactionDay() == null
                        || product.getAllowedTransactionDay() < 1
                        || product.getAllowedTransactionDay() > 31) {
                    return Mono.error(new IllegalArgumentException(Constants.ERROR_FIXED_TERM_REQUIRE_TRANSACTION_DAY));
                }
                break;

            default:
                // No aplica a productos no pasivos
                break;
        }

        return Mono.empty();
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
            return Mono.error(new IllegalArgumentException(Constants.ERROR_BUSINESS_CURRENT_ACCOUNT_REQUIRES_HOLDER));
        }
        return Mono.empty();
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
                        return Mono.error(
                                new IllegalArgumentException(
                                        String.format(
                                                Constants.ERROR_PERSONAL_UNIQUE_PASSIVE_ACCOUNT,
                                                product.getType().name()
                                        )
                                )
                        );
                    }
                    return Mono.empty();
                });
    }

    private boolean isPassiveProduct(ProductType type) {
        return type == ProductType.AHORRO ||
                type == ProductType.CORRIENTE ||
                type == ProductType.PLAZO_FIJO;
    }
}
