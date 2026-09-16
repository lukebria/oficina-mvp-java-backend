package br.com.oficina.mvp.customer.adapter.in.web;

import br.com.oficina.mvp.customer.application.port.out.CustomerRepositoryPort;
import br.com.oficina.mvp.customer.domain.Customer;
import br.com.oficina.mvp.customer.domain.CustomerStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Endpoint de serviço-a-serviço consumido pela Function Serverless externa (autenticação via CPF) — ver
 * docs/architecture.md, seção 5. Autenticado por API key própria, não por JWT.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InternalCustomerControllerIntegrationTest {
    private static final String API_KEY_HEADER = "X-Internal-Api-Key";
    private static final String VALID_API_KEY = "teste-internal-api-key";

    @Autowired
    MockMvc mvc;
    @Autowired
    CustomerRepositoryPort customers;

    @Test
    void shouldRejectRequestsWithoutApiKey() throws Exception {
        mvc.perform(get("/api/internal/customers/52998224725"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectRequestsWithWrongApiKey() throws Exception {
        mvc.perform(get("/api/internal/customers/52998224725").header(API_KEY_HEADER, "chave-errada"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnFoundWhenCustomerExists() throws Exception {
        var nanos = String.valueOf(System.nanoTime());
        var document = nanos.substring(nanos.length() - 11);
        customers.save(new Customer("Cliente Interno", document, "interno-" + nanos + "@teste.com", "11966666666"));

        mvc.perform(get("/api/internal/customers/" + document).header(API_KEY_HEADER, VALID_API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.found").value(true))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldReturnInactiveStatusWhenCustomerIsInactive() throws Exception {
        var nanos = String.valueOf(System.nanoTime());
        var document = nanos.substring(nanos.length() - 11);
        customers.save(new Customer("Cliente Inativo", document, "inativo-" + nanos + "@teste.com", "11944444444", CustomerStatus.INACTIVE));

        mvc.perform(get("/api/internal/customers/" + document).header(API_KEY_HEADER, VALID_API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.found").value(true))
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    void shouldReturnNotFoundWhenCustomerDoesNotExist() throws Exception {
        mvc.perform(get("/api/internal/customers/00000000000").header(API_KEY_HEADER, VALID_API_KEY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.found").value(false))
                .andExpect(jsonPath("$.status").value("NOT_FOUND"));
    }
}
