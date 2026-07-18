package br.com.oficina.mvp.shared.api;

import br.com.oficina.mvp.auth.application.port.out.UserRepositoryPort;
import br.com.oficina.mvp.auth.domain.User;
import br.com.oficina.mvp.catalog.application.port.out.CatalogRepositoryPort;
import br.com.oficina.mvp.catalog.domain.ServiceCatalogItem;
import br.com.oficina.mvp.customer.application.port.out.CustomerRepositoryPort;
import br.com.oficina.mvp.customer.domain.Customer;
import br.com.oficina.mvp.part.application.port.out.PartRepositoryPort;
import br.com.oficina.mvp.part.domain.Part;
import br.com.oficina.mvp.serviceorder.application.port.out.ServiceOrderRepositoryPort;
import br.com.oficina.mvp.serviceorder.domain.ServiceOrder;
import br.com.oficina.mvp.serviceorder.domain.WorkOrderPart;
import br.com.oficina.mvp.serviceorder.domain.WorkOrderService;
import br.com.oficina.mvp.shared.domain.Role;
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

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de ponta a ponta com Spring/H2 reais (sem mocks). Ao contrário dos testes unitários de application service,
 * que mockam as portas de saída, estes exercitam o Hibernate de verdade com {@code open-in-view: false}, então
 * pegam regressões de LazyInitializationException quando um DTO passa a acessar uma associação LAZY fora da
 * transação — exatamente a classe de bug encontrada em VehicleResponseDto/ServiceOrderResponseDto.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LazyAssociationSerializationIntegrationTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    UserRepositoryPort users;
    @Autowired
    CustomerRepositoryPort customers;
    @Autowired
    VehicleRepositoryPort vehicles;
    @Autowired
    CatalogRepositoryPort catalog;
    @Autowired
    PartRepositoryPort parts;
    @Autowired
    ServiceOrderRepositoryPort serviceOrders;
    @Autowired
    JwtService jwtService;
    @Autowired
    PasswordEncoder passwordEncoder;

    private String token;

    @BeforeEach
    void setUp() {
        var admin = users.save(new User("Admin", "admin-" + System.nanoTime() + "@teste.com",
                passwordEncoder.encode("Admin@123"), Role.ADMIN));
        token = jwtService.generate(admin);
    }

    @Test
    void shouldSerializeVehicleListAndDetailWithoutLazyInitializationException() throws Exception {
        var customer = customers.save(new Customer("João Silva", "52998224725", "joao@teste.com", "11999999999"));
        var vehicle = vehicles.save(new Vehicle(customer, "ABC1234", "Fiat", "Uno", 2020));

        // A lista mistura dados de tod méthodo de teste desta classe (mesmo H2, ordem de execução não garantida);
        // o ponto aqui é só confirmar que a serialização não estoura LazyInitializationException.
        mvc.perform(get("/api/vehicles").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mvc.perform(get("/api/vehicles/" + vehicle.getId()).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName").value("João Silva"));
    }

    @Test
    void shouldSerializeServiceOrderListAndDetailWithoutLazyInitializationException() throws Exception {
        var customer = customers.save(new Customer("Maria Souza", "11144477735", "maria@teste.com", "11988888888"));
        var vehicle = vehicles.save(new Vehicle(customer, "XYZ9A87", "Toyota", "Corolla", 2022));
        var catalogItem = catalog.save(new ServiceCatalogItem("Troca de óleo", "Completa", new BigDecimal("180.00"), 60, true));
        var part = parts.save(new Part("Filtro de óleo", "FLT-LAZY-001", new BigDecimal("45.90"), 10, 2, true));

        var order = new ServiceOrder("OS-LAZY-TEST", customer, vehicle, "Barulho no motor");
        order.addService(new WorkOrderService(catalogItem, 1));
        order.addPart(new WorkOrderPart(part, 1));
        order.markBudgetWaitingApproval();
        var saved = serviceOrders.save(order);

        // Mesma ressalva: a lista mistura dados de outros métodos de teste; só a chamada por id abaixo é
        // determinística o suficiente para asserções de conteúdo.
        mvc.perform(get("/api/service-orders").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mvc.perform(get("/api/service-orders/" + saved.getId()).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customer.name").value("Maria Souza"))
                .andExpect(jsonPath("$.vehicle.plate").value("XYZ9A87"))
                .andExpect(jsonPath("$.services[0].name").value("Troca de óleo"))
                .andExpect(jsonPath("$.parts[0].name").value("Filtro de óleo"))
                .andExpect(jsonPath("$.history").isArray());

        mvc.perform(get("/api/public/service-orders/OS-LAZY-TEST?document=11144477735"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName").value("Maria Souza"))
                .andExpect(jsonPath("$.services[0].name").value("Troca de óleo"));
    }
}
