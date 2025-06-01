import com.nttdata.product.model.BankProduct;
import com.nttdata.product.model.Dto.CustomerDTO;
import com.nttdata.product.model.Dto.CustomerResponse;
import com.nttdata.product.model.Type.CustomerType;
import com.nttdata.product.model.Type.ProductType;
import com.nttdata.product.repository.BankProductRepository;
import com.nttdata.product.service.impl.BankProductServiceImpl;
import com.nttdata.product.utils.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BankProductServiceImplTest {

    @Mock
    private BankProductRepository repository;

    @Mock
    private WebClient webClient;

    @InjectMocks
    private BankProductServiceImpl service;

    @BeforeEach
    void setup() {
        // Simula la llamada a Utils.getCustomerService().get()
        Utils.setCustomerService(() -> webClient);
    }

    @Test
    void testGetAll() {
        BankProduct product = new BankProduct();
        Mockito.when(repository.findAll()).thenReturn(Flux.just(product));

        StepVerifier.create(service.getAll())
                .expectNext(product)
                .verifyComplete();
    }

    @Test
    void testGetById_found() {
        BankProduct product = new BankProduct();
        Mockito.when(repository.findById("123")).thenReturn(Mono.just(product));

        StepVerifier.create(service.getById("123"))
                .expectNext(product)
                .verifyComplete();
    }

    @Test
    void testGetById_notFound() {
        Mockito.when(repository.findById("999")).thenReturn(Mono.empty());

        StepVerifier.create(service.getById("999"))
                .verifyComplete();
    }

    @Test
    void testCreate_successful() {
        // Producto a crear
        BankProduct product = new BankProduct();
        product.setCustomerId("123");
        product.setType(ProductType.CREDITO_PERSONAL);

        // Cliente simulado
        CustomerDTO customer = new CustomerDTO();
        customer.setType(CustomerType.PERSONAL);

        CustomerResponse response = new CustomerResponse();
        response.setCustomers(List.of(customer));

        // Mock completo de WebClient
        WebClient.RequestHeadersUriSpec uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        WebClient client = mock(WebClient.class);

        when(client.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString(), (Object[]) any())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(CustomerResponse.class)).thenReturn(Mono.just(response));

        // Simular cliente HTTP en Utils
        Utils.setCustomerService(() -> client);

        // Mock de repositorio
        when(repository.findByCustomerId(product.getCustomerId())).thenReturn(Flux.empty());
        when(repository.save(product)).thenReturn(Mono.just(product));

        // Act + Assert
        StepVerifier.create(service.create(product))
                .expectNext(product)
                .verifyComplete();
    }

    @Test
    void testUpdate_successful() {
        BankProduct existing = new BankProduct();
        existing.setId("abc");

        BankProduct updated = new BankProduct();
        updated.setType(ProductType.AHORRO);
        updated.setCustomerId("cust1");

        Mockito.when(repository.findById("abc")).thenReturn(Mono.just(existing));
        Mockito.when(repository.save(Mockito.any())).thenReturn(Mono.just(updated));

        StepVerifier.create(service.update("abc", updated))
                .expectNext(updated)
                .verifyComplete();
    }

    @Test
    void testDelete_successful() {
        Mockito.when(repository.deleteById("id")).thenReturn(Mono.empty());

        StepVerifier.create(service.delete("id"))
                .verifyComplete();
    }

}