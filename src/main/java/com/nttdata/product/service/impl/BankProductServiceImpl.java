package com.nttdata.product.service.impl;

import com.nttdata.product.model.BankProduct;
import com.nttdata.product.model.Details.CurrentAccount;
import com.nttdata.product.model.Details.FixedTermAccount;
import com.nttdata.product.model.Details.SavingsAccount;
import com.nttdata.product.model.Dto.CustomerDTO;
import com.nttdata.product.model.Dto.CustomerResponse;
import com.nttdata.product.model.Type.CustomerType;
import com.nttdata.product.model.Type.ProductStatus;
import com.nttdata.product.model.Type.ProductType;
import com.nttdata.product.model.Type.ProfileType;
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
    public Flux<BankProduct> getByCustomerId(String id) {
        return repository.findByCustomerId(id);
    }

    @Override
    public Mono<BankProduct> create(BankProduct product) {
        return Utils.getCustomerService().get().get()
                .uri("/customers/{id}", product.getCustomerId())
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response -> {
                    if (response.statusCode() == HttpStatus.NOT_FOUND) {
                        return Mono.error(new EmptyResultException(
                                Constants.ERROR_FIND_CUSTOMER));
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
                .then(isCustomerWithOverdueDebt(customer))
                .then(isPassiveProduct(product.getType()) ?
                        //Valida las reglas específicas de negocio para productos pasivos
                        validatePassiveProductProperties(customer, product)
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
                    existing.setCustomerId(product.getCustomerId());
                    existing.setType(product.getType());
                    existing.setStatus(product.getStatus());
                    existing.setName(product.getName());
                    existing.setBalance(product.getBalance());
                    existing.setDetails(product.getDetails());
                    existing.setHolders(product.getHolders());
                    existing.setSigners(product.getSigners());
                    return repository.save(existing);
                });
    }

    @Override
    public Mono<Void> delete(String id) {
        return repository.deleteById(id);
    }

    private boolean isPersonalCustomerWithPersonalLoan(CustomerDTO customer, BankProduct product) {
        return customer.getType() == CustomerType.PERSONAL &&
                product.getType() == ProductType.CREDIT;
    }

    private Mono<BankProduct> validateSinglePersonalLoan(BankProduct product) {
        return repository.findByCustomerId(product.getCustomerId())
                .filter(p -> p.getType() == ProductType.CREDIT)
                .hasElements()
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalArgumentException(
                                Constants.ERROR_PERSONAL_ONE_CREDIT_ONLY));
                    }
                    return Mono.empty();
                });
    }

    private Mono<BankProduct> validatePassiveProductProperties(CustomerDTO customer, BankProduct product) {
        switch (product.getType()) {
            case SAVINGS:
                SavingsAccount detailsSavingsAccount = (SavingsAccount) product.getDetails();
                if (detailsSavingsAccount.getMaintenanceFee() != null &&
                        detailsSavingsAccount.getMaintenanceFee() > 0) {
                    return Mono.error(new IllegalArgumentException(
                            Constants.ERROR_SAVINGS_NO_MAINTENANCE_FEE));
                }
                if (detailsSavingsAccount.getMonthlyLimit() == null ||
                        detailsSavingsAccount.getMonthlyLimit() <= 0) {
                    return Mono.error(new IllegalArgumentException(
                            Constants.ERROR_SAVINGS_REQUIRE_MONTHLY_LIMIT));
                }

                // Validación: Ahorro VIP para cliente personal con tarjeta de crédito
                if (customer.getType() == CustomerType.PERSONAL &&
                        customer.getProfile() == ProfileType.VIP) {
                    return validateHasCreditCard(customer.getId());
                }
                break;

            case CURRENT:
                CurrentAccount current = (CurrentAccount) product.getDetails();
                boolean isPymeBusiness = customer.getType() == CustomerType.BUSINESS &&
                        customer.getProfile() == ProfileType.PYME;

                // Validación 1: Si es PYME, no debe tener comisión
                if (isPymeBusiness &&
                        (current.getMaintenanceFee() == null || current.getMaintenanceFee() > 0)) {
                    return Mono.error(new IllegalArgumentException(
                            Constants.ERROR_CURRENT_ACCOUNT_BUSINESS_PYMES_NO_REQUIRES_FEE));
                }

                // Validación 2: Si NO es PYME, debe tener comisión válida
                if (!isPymeBusiness &&
                        (current.getMaintenanceFee() == null || current.getMaintenanceFee() <= 0)) {
                    return Mono.error(new IllegalArgumentException(
                            Constants.ERROR_CURRENT_ACCOUNT_REQUIRES_FEE));
                }

                // Validación 3: Ningún cliente debe tener límite mensual
                if (current.getMonthlyLimit() != null && current.getMonthlyLimit() > 0) {
                    return Mono.error(new IllegalArgumentException(
                            Constants.ERROR_CURRENT_ACCOUNT_NO_MONTHLY_LIMIT));
                }

                // Validación 4: Si es PYME, debe tener tarjeta de crédito activa
                if (isPymeBusiness) {
                    return validateHasCreditCard(customer.getId()).then(Mono.just(product));
                }

                break;

            case FIXED_TERM:
                FixedTermAccount detailsFixedTermAccount = (FixedTermAccount) product.getDetails();
                if (detailsFixedTermAccount.getMaintenanceFee() != null &&
                        detailsFixedTermAccount.getMaintenanceFee() > 0) {
                    return Mono.error(new IllegalArgumentException(
                            Constants.ERROR_FIXED_TERM_NO_MAINTENANCE_FEE));
                }

                if (detailsFixedTermAccount.getMonthlyLimit() != null &&
                        detailsFixedTermAccount.getMonthlyLimit() != 1) {
                    return Mono.error(new IllegalArgumentException(
                            Constants.ERROR_FIXED_TERM_REQUIRE_MONTHLY_LIMIT));
                }
                if (detailsFixedTermAccount.getAllowedTransactionDay() == null
                        || detailsFixedTermAccount.getAllowedTransactionDay() < 1
                        || detailsFixedTermAccount.getAllowedTransactionDay() > 31) {
                    return Mono.error(new IllegalArgumentException(
                            Constants.ERROR_FIXED_TERM_REQUIRE_TRANSACTION_DAY));
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
                (product.getType() == ProductType.SAVINGS || product.getType() == ProductType.FIXED_TERM);
    }

    private boolean isBusinessCurrentAccount(CustomerDTO customer, BankProduct product) {
        return customer.getType() == CustomerType.BUSINESS &&
                product.getType() == ProductType.CURRENT;
    }

    private Mono<BankProduct> validateBusinessAccountHolders(BankProduct product) {
        if (product.getHolders() == null || product.getHolders().isEmpty()) {
            return Mono.error(new IllegalArgumentException(Constants.ERROR_BUSINESS_CURRENT_ACCOUNT_REQUIRES_HOLDER));
        }
        return Mono.empty();
    }

    private boolean isUniquePassiveAccountForPersonal(CustomerDTO customer, BankProduct product) {
        return customer.getType() == CustomerType.PERSONAL &&
                (product.getType() == ProductType.SAVINGS || product.getType() == ProductType.CURRENT);
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
        return type == ProductType.SAVINGS ||
                type == ProductType.CURRENT ||
                type == ProductType.FIXED_TERM;
    }

    private Mono<BankProduct> validateHasCreditCard(String customerId) {
        return repository.findByCustomerId(customerId)
                .filter(product -> product.getType() == ProductType.CREDIT)
                .hasElements()
                .flatMap(hasCredit -> {
                    if (!hasCredit) {
                        return Mono.error(new IllegalArgumentException(Constants.ERROR_CREDIT_CARD_REQUIRED));
                    }
                    return Mono.empty();
                });
    }

    private Mono<Object> isCustomerWithOverdueDebt(CustomerDTO customer) {
        return repository.findByCustomerId(customer.getId())
                .filter(product -> product.getType() == ProductType.CREDIT
                        && product.getStatus() == ProductStatus.BLOCKED_OVERDUE_DEBT)
                .hasElements()
                .flatMap(hasOverdueDebt -> {
                    if (hasOverdueDebt) {
                        return Mono.error(new IllegalArgumentException(Constants.ERROR_CREDIT_CARD_OVERDUE_DEBT));
                    }
                    return Mono.empty();
                });
    }

}
