package br.com.oficina.mvp.shared.api;

import br.com.oficina.mvp.auth.application.port.out.UserRepositoryPort;
import br.com.oficina.mvp.auth.domain.User;
import br.com.oficina.mvp.customer.application.port.out.CustomerRepositoryPort;
import br.com.oficina.mvp.customer.domain.Customer;
import br.com.oficina.mvp.serviceorder.application.port.out.ServiceOrderRepositoryPort;
import br.com.oficina.mvp.serviceorder.domain.ServiceOrder;
import br.com.oficina.mvp.shared.domain.Role;
import br.com.oficina.mvp.shared.security.JwtService;
import br.com.oficina.mvp.vehicle.application.port.out.VehicleRepositoryPort;
import br.com.oficina.mvp.vehicle.domain.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de ponta a ponta (Spring Security real, sem mocks) para a matriz de autorização granular por role definida
 * em {@code SecurityConfig}. Cobre as "formas" de regra que existem na matriz — todos os três papéis, só ADMIN,
 * ADMIN+MECHANIC e rota pública — em vez de repetir a mesma checagem para cada uma das ~30 combinações rota/méthodo.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthorizationIntegrationTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    UserRepositoryPort users;
    @Autowired
    CustomerRepositoryPort customers;
    @Autowired
    VehicleRepositoryPort vehicles;
    @Autowired
    ServiceOrderRepositoryPort serviceOrders;
    @Autowired
    JwtService jwtService;
    @Autowired
    PasswordEncoder passwordEncoder;

    private String adminToken;
    private String mechanicToken;
    private String attendantToken;

    @BeforeEach
    void setUp() {
        adminToken = tokenFor(Role.ADMIN);
        mechanicToken = tokenFor(Role.MECHANIC);
        attendantToken = tokenFor(Role.ATTENDANT);
    }

    private String tokenFor(Role role) {
        var user = users.save(new User(role.name(), role.name().toLowerCase() + "-" + System.nanoTime() + "@teste.com",
                passwordEncoder.encode("Senha@123"), role));
        return jwtService.generate(user);
    }

    @Test
    void shouldAllowAllThreeRolesToListCustomers() throws Exception {
        mvc.perform(get("/api/customers").header("Authorization", "Bearer " + adminToken)).andExpect(status().isOk());
        mvc.perform(get("/api/customers").header("Authorization", "Bearer " + mechanicToken)).andExpect(status().isOk());
        mvc.perform(get("/api/customers").header("Authorization", "Bearer " + attendantToken)).andExpect(status().isOk());
    }

    @Test
    void shouldRestrictCustomerDeletionToAdmin() throws Exception {
        var customer = customers.save(new Customer("Del Teste", "11144477735", "del@teste.com", "11999999999"));

        mvc.perform(delete("/api/customers/" + customer.getId()).header("Authorization", "Bearer " + mechanicToken))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/customers/" + customer.getId()).header("Authorization", "Bearer " + attendantToken))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/customers/" + customer.getId()).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldRestrictCatalogCreationToAdmin() throws Exception {
        var payload = """
                {"name":"Servico Authz Teste","description":"desc","basePrice":10.00,"estimatedMinutes":10,"active":true}
                """;

        mvc.perform(post("/api/services").header("Authorization", "Bearer " + mechanicToken)
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/services").header("Authorization", "Bearer " + attendantToken)
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/services").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldAllowAdminAndMechanicButNotAttendantToApproveServiceOrder() throws Exception {
        var customer = customers.save(new Customer("Aprov Teste", "93541134780", "aprov@teste.com", "11988888888"));
        var vehicle = vehicles.save(new Vehicle(customer, "APV1234", "Ford", "Ka", 2021));
        var order = new ServiceOrder("OS-AUTHZ-TEST", customer, vehicle, null);
        order.markBudgetWaitingApproval();
        var saved = serviceOrders.save(order);

        mvc.perform(patch("/api/service-orders/" + saved.getId() + "/approve").header("Authorization", "Bearer " + attendantToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/api/service-orders/" + saved.getId() + "/approve").header("Authorization", "Bearer " + mechanicToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRestrictReportsToAdmin() throws Exception {
        mvc.perform(get("/api/reports/average-execution-time").header("Authorization", "Bearer " + mechanicToken))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/reports/average-execution-time").header("Authorization", "Bearer " + attendantToken))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/reports/average-execution-time").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void shouldKeepHealthEndpointsPublic() throws Exception {
        mvc.perform(get("/api/health")).andExpect(status().isOk());
    }
}
