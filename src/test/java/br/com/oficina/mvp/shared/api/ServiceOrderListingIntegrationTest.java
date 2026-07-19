package br.com.oficina.mvp.shared.api;

import br.com.oficina.mvp.auth.application.port.out.UserRepositoryPort;
import br.com.oficina.mvp.auth.domain.User;
import br.com.oficina.mvp.customer.application.port.out.CustomerRepositoryPort;
import br.com.oficina.mvp.customer.domain.Customer;
import br.com.oficina.mvp.serviceorder.application.port.out.ServiceOrderRepositoryPort;
import br.com.oficina.mvp.serviceorder.domain.ServiceOrder;
import br.com.oficina.mvp.shared.domain.Role;
import br.com.oficina.mvp.shared.domain.ServiceOrderStatus;
import br.com.oficina.mvp.shared.security.JwtService;
import br.com.oficina.mvp.vehicle.application.port.out.VehicleRepositoryPort;
import br.com.oficina.mvp.vehicle.domain.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de ponta a ponta (Spring + H2 reais, sem mocks) para a listagem de OS: por padrão, ordenação por prioridade
 * de status (EM_EXECUCAO > AGUARDANDO_APROVACAO > EM_DIAGNOSTICO > RECEBIDA, mais antigas primeiro) e exclusão de OS
 * em estado terminal (FINALIZADA, ENTREGUE, RECUSADA) da listagem; com {@code all=true}, listagem completa sem
 * filtro nem ordenação especial.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ServiceOrderListingIntegrationTest {

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

    private String token;
    private Customer customer;
    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        var admin = users.save(new User("Admin", "admin-" + System.nanoTime() + "@teste.com",
                passwordEncoder.encode("Admin@123"), Role.ADMIN));
        token = jwtService.generate(admin);

        var nanos = String.valueOf(System.nanoTime());
        var uniqueDocument = nanos.substring(nanos.length() - 11);
        var uniquePlate = "L" + nanos.substring(nanos.length() - 6);
        customer = customers.save(new Customer("Cliente Listagem", uniqueDocument, "cliente@teste.com", "11988888888"));
        vehicle = vehicles.save(new Vehicle(customer, uniquePlate, "Fiat", "Uno", 2021));
    }

    @Test
    void shouldOrderByStatusPriorityAndExcludeTerminalStatuses() throws Exception {
        var recebidaOld = saveOrderWithStatus("OS-LST-RECEBIDA-OLD", ServiceOrderStatus.RECEBIDA);
        sleepAFewMillis();
        var recebidaNew = saveOrderWithStatus("OS-LST-RECEBIDA-NEW", ServiceOrderStatus.RECEBIDA);
        saveOrderWithStatus("OS-LST-DIAGNOSTICO", ServiceOrderStatus.EM_DIAGNOSTICO);
        saveOrderWithStatus("OS-LST-AGUARDANDO", ServiceOrderStatus.AGUARDANDO_APROVACAO);
        saveOrderWithStatus("OS-LST-EXECUCAO", ServiceOrderStatus.EM_EXECUCAO);
        saveOrderWithStatus("OS-LST-FINALIZADA", ServiceOrderStatus.FINALIZADA);
        saveOrderWithStatus("OS-LST-ENTREGUE", ServiceOrderStatus.ENTREGUE);
        saveOrderWithStatus("OS-LST-RECUSADA", ServiceOrderStatus.RECUSADA);

        var responseBody = mvc.perform(get("/api/service-orders").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.code == 'OS-LST-FINALIZADA')]").isEmpty())
                .andExpect(jsonPath("$[?(@.code == 'OS-LST-ENTREGUE')]").isEmpty())
                .andExpect(jsonPath("$[?(@.code == 'OS-LST-RECUSADA')]").isEmpty())
                .andReturn().getResponse().getContentAsString();

        var codes = com.jayway.jsonpath.JsonPath.<java.util.List<String>>read(responseBody, "$[*].code");
        var relevantCodes = codes.stream()
                .filter(code -> code.startsWith("OS-LST-"))
                .toList();

        org.assertj.core.api.Assertions.assertThat(relevantCodes).containsExactly(
                "OS-LST-EXECUCAO",
                "OS-LST-AGUARDANDO",
                "OS-LST-DIAGNOSTICO",
                "OS-LST-RECEBIDA-OLD",
                "OS-LST-RECEBIDA-NEW"
        );
    }

    @Test
    void shouldListAllOrdersWithoutFilterOrSpecialOrderingWhenAllIsTrue() throws Exception {
        saveOrderWithStatus("OS-LST-ALL-RECEBIDA", ServiceOrderStatus.RECEBIDA);
        saveOrderWithStatus("OS-LST-ALL-FINALIZADA", ServiceOrderStatus.FINALIZADA);
        saveOrderWithStatus("OS-LST-ALL-ENTREGUE", ServiceOrderStatus.ENTREGUE);
        saveOrderWithStatus("OS-LST-ALL-RECUSADA", ServiceOrderStatus.RECUSADA);

        var responseBody = mvc.perform(get("/api/service-orders").param("all", "true").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        var codes = com.jayway.jsonpath.JsonPath.<java.util.List<String>>read(responseBody, "$[*].code");
        var relevantCodes = codes.stream()
                .filter(code -> code.startsWith("OS-LST-ALL-"))
                .toList();

        org.assertj.core.api.Assertions.assertThat(relevantCodes).containsExactlyInAnyOrder(
                "OS-LST-ALL-RECEBIDA",
                "OS-LST-ALL-FINALIZADA",
                "OS-LST-ALL-ENTREGUE",
                "OS-LST-ALL-RECUSADA"
        );
    }

    private ServiceOrder saveOrderWithStatus(String code, ServiceOrderStatus status) {
        var order = new ServiceOrder(code, customer, vehicle, null);
        if (status != ServiceOrderStatus.RECEBIDA) {
            applyTransitionsUntil(order, status);
        }
        return serviceOrders.save(order);
    }

    private void applyTransitionsUntil(ServiceOrder order, ServiceOrderStatus target) {
        switch (target) {
            case EM_DIAGNOSTICO -> order.changeStatus(ServiceOrderStatus.EM_DIAGNOSTICO, null);
            case AGUARDANDO_APROVACAO -> order.markBudgetWaitingApproval();
            case EM_EXECUCAO -> {
                order.markBudgetWaitingApproval();
                order.decideApproval(true, null);
            }
            case FINALIZADA -> {
                order.markBudgetWaitingApproval();
                order.decideApproval(true, null);
                order.changeStatus(ServiceOrderStatus.FINALIZADA, null);
            }
            case ENTREGUE -> {
                order.markBudgetWaitingApproval();
                order.decideApproval(true, null);
                order.changeStatus(ServiceOrderStatus.FINALIZADA, null);
                order.changeStatus(ServiceOrderStatus.ENTREGUE, null);
            }
            case RECUSADA -> {
                order.markBudgetWaitingApproval();
                order.decideApproval(false, null);
            }
            default -> throw new IllegalArgumentException("Status não suportado neste helper: " + target);
        }
    }

    private void sleepAFewMillis() throws InterruptedException {
        Thread.sleep(5);
    }
}
