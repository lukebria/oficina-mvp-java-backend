package br.com.oficina.mvp.serviceorder.application;

import br.com.oficina.mvp.catalog.application.port.out.CatalogRepositoryPort;
import br.com.oficina.mvp.customer.application.port.out.CustomerRepositoryPort;
import br.com.oficina.mvp.customer.domain.Customer;
import br.com.oficina.mvp.shared.domain.ServiceOrderStatus;
import br.com.oficina.mvp.part.application.port.out.PartRepositoryPort;
import br.com.oficina.mvp.part.domain.Part;
import br.com.oficina.mvp.serviceorder.application.port.in.CreateServiceOrderCommand;
import br.com.oficina.mvp.serviceorder.application.port.in.PublicServiceOrderUseCase;
import br.com.oficina.mvp.serviceorder.application.port.in.ServiceOrderUseCase;
import br.com.oficina.mvp.serviceorder.application.port.out.ServiceOrderRepositoryPort;
import br.com.oficina.mvp.serviceorder.domain.ServiceOrder;
import br.com.oficina.mvp.serviceorder.domain.WorkOrderPart;
import br.com.oficina.mvp.serviceorder.domain.WorkOrderService;
import br.com.oficina.mvp.shared.exception.BusinessException;
import br.com.oficina.mvp.shared.validation.DocumentValidator;
import br.com.oficina.mvp.shared.validation.PlateValidator;
import br.com.oficina.mvp.vehicle.application.port.out.VehicleRepositoryPort;
import br.com.oficina.mvp.vehicle.domain.Vehicle;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ServiceOrderService implements ServiceOrderUseCase, PublicServiceOrderUseCase {
    private static final int MAX_CODE_GENERATION_ATTEMPTS = 5;

    private final ServiceOrderRepositoryPort serviceOrders;
    private final CustomerRepositoryPort customers;
    private final VehicleRepositoryPort vehicles;
    private final CatalogRepositoryPort catalog;
    private final PartRepositoryPort parts;

    public ServiceOrderService(
            ServiceOrderRepositoryPort serviceOrders,
            CustomerRepositoryPort customers,
            VehicleRepositoryPort vehicles,
            CatalogRepositoryPort catalog,
            PartRepositoryPort parts
    ) {
        this.serviceOrders = serviceOrders;
        this.customers = customers;
        this.vehicles = vehicles;
        this.catalog = catalog;
        this.parts = parts;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceOrder> list() {
        return serviceOrders.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceOrder findById(Long id) {
        return findEntity(id);
    }

    @Override
    @Transactional
    public ServiceOrder create(CreateServiceOrderCommand command) {
        var document = DocumentValidator.requireValid(command.customerDocument());
        var plate = PlateValidator.requireValid(command.vehicle().plate());

        var customer = resolveCustomer(command.customer(), document);
        var vehicle = resolveVehicle(command.vehicle(), customer, plate);

        var order = new ServiceOrder(generateUniqueOrderCode(), customer, vehicle, command.customerNotes());
        addRequestedServices(order, command.services());
        addRequestedParts(order, command.parts());

        order.markBudgetWaitingApproval();
        return serviceOrders.save(order);
    }

    private Customer resolveCustomer(CreateServiceOrderCommand.CustomerData data, String document) {
        var customer = customers.findByDocument(document)
                .orElseGet(() -> customers.save(new Customer(data.name(), document, data.email(), data.phone())));
        customer.update(data.name(), document, data.email(), data.phone());
        return customer;
    }

    private Vehicle resolveVehicle(CreateServiceOrderCommand.VehicleData data, Customer customer, String plate) {
        var vehicle = vehicles.findByPlate(plate)
                .orElseGet(() -> vehicles.save(new Vehicle(customer, plate, data.brand(), data.model(), data.manufacturingYear())));
        vehicle.update(customer, plate, data.brand(), data.model(), data.manufacturingYear());
        return vehicle;
    }

    private void addRequestedServices(ServiceOrder order, List<CreateServiceOrderCommand.ServiceItemData> requestedServices) {
        for (var requestedService : requestedServices) {
            var serviceItem = catalog.findById(requestedService.serviceItemId())
                    .filter(item -> Boolean.TRUE.equals(item.getActive()))
                    .orElseThrow(() -> new BusinessException("Um ou mais serviços informados não existem ou estão inativos.", HttpStatus.BAD_REQUEST));
            order.addService(new WorkOrderService(serviceItem, requestedService.quantity()));
        }
    }

    private void addRequestedParts(ServiceOrder order, List<CreateServiceOrderCommand.PartItemData> requestedParts) {
        if (requestedParts == null) {
            return;
        }
        for (var requestedPart : requestedParts) {
            var part = parts.findById(requestedPart.partId())
                    .filter(item -> Boolean.TRUE.equals(item.getActive()))
                    .orElseThrow(() -> new BusinessException("Uma ou mais peças informadas não existem ou estão inativas.", HttpStatus.BAD_REQUEST));
            order.addPart(new WorkOrderPart(part, requestedPart.quantity()));
        }
    }

    @Override
    @Transactional
    public ServiceOrder approve(Long id, String comment) {
        var order = findEntity(id);
        validateStock(order);
        decrementStock(order);
        order.approve(comment);
        return order;
    }

    @Override
    @Transactional
    public ServiceOrder updateStatus(Long id, ServiceOrderStatus status, String comment) {
        var order = findEntity(id);
        order.changeStatus(status, comment);
        return order;
    }

    @Override
    @Transactional
    public ServiceOrder updateDiagnosis(Long id, String diagnosis) {
        var order = findEntity(id);
        order.updateDiagnosis(diagnosis);
        return order;
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceOrder findByCode(String code, String document) {
        var normalizedDocument = DocumentValidator.normalize(document);
        var order = serviceOrders.findByCode(code)
                .orElseThrow(() -> new BusinessException("Ordem de serviço não encontrada para os dados informados.", HttpStatus.NOT_FOUND));
        if (!order.getCustomer().getDocument().equals(normalizedDocument)) {
            throw new BusinessException("Ordem de serviço não encontrada para os dados informados.", HttpStatus.NOT_FOUND);
        }
        return order;
    }

    @Override
    @Transactional
    public ServiceOrder approveByCustomer(String code, String document, String comment) {
        var normalizedDocument = DocumentValidator.normalize(document);
        var order = serviceOrders.findByCode(code)
                .orElseThrow(() -> new BusinessException("Ordem de serviço não encontrada.", HttpStatus.NOT_FOUND));

        if (!order.getCustomer().getDocument().equals(normalizedDocument)) {
            throw new BusinessException("Documento não corresponde ao cliente da OS.", HttpStatus.FORBIDDEN);
        }

        if (order.getStatus() != ServiceOrderStatus.AGUARDANDO_APROVACAO) {
            throw new BusinessException("Esta OS não está aguardando aprovação do cliente.", HttpStatus.BAD_REQUEST);
        }

        validateStock(order);
        decrementStock(order);
        order.approve(comment == null ? "Orçamento aprovado pelo cliente. OS enviada para execução." : comment);
        return order;
    }

    private ServiceOrder findEntity(Long id) {
        return serviceOrders.findById(id)
                .orElseThrow(() -> new BusinessException("Ordem de serviço não encontrada.", HttpStatus.NOT_FOUND));
    }

    private void validateStock(ServiceOrder order) {
        for (var orderPart : order.getParts()) {
            Part part = orderPart.getPart();
            if (part.getStockQuantity() < orderPart.getQuantity()) {
                var details = new HashMap<String, Object>();
                details.put("partId", part.getId());
                details.put("available", part.getStockQuantity());
                details.put("requested", orderPart.getQuantity());
                throw new BusinessException("Estoque insuficiente para a peça " + part.getName() + ".", HttpStatus.UNPROCESSABLE_CONTENT, details);
            }
        }
    }

    private void decrementStock(ServiceOrder order) {
        for (var orderPart : order.getParts()) {
            orderPart.getPart().decrementStock(orderPart.getQuantity());
        }
    }

    // Checa unicidade antes do insert em vez de tentar salvar e reagir a um conflito: no Postgres, uma violação de
    // constraint aborta a transação inteira, então um save() seguinte na mesma transação falharia de qualquer forma.
    private String generateUniqueOrderCode() {
        for (int attempt = 1; attempt <= MAX_CODE_GENERATION_ATTEMPTS; attempt++) {
            var code = generateOrderCode();
            if (!serviceOrders.existsByCode(code)) {
                return code;
            }
        }
        throw new BusinessException("Não foi possível gerar um código único para a OS. Tente novamente.", HttpStatus.CONFLICT);
    }

    private String generateOrderCode() {
        var date = DateTimeFormatter.BASIC_ISO_DATE.format(OffsetDateTime.now());
        var random = String.format("%05d", ThreadLocalRandom.current().nextInt(0, 100000));
        return "OS-" + date + "-" + random;
    }
}
