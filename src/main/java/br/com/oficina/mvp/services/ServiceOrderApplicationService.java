package br.com.oficina.mvp.services;

import br.com.oficina.mvp.infra.ServiceCatalogItemRepository;
import br.com.oficina.mvp.domains.Customer;
import br.com.oficina.mvp.infra.CustomerRepository;
import br.com.oficina.mvp.domains.Part;
import br.com.oficina.mvp.infra.PartRepository;
import br.com.oficina.mvp.dtos.CreateServiceOrderRequestDto;
import br.com.oficina.mvp.dtos.PublicServiceOrderResponseDto;
import br.com.oficina.mvp.dtos.ServiceOrderResponseDto;
import br.com.oficina.mvp.domains.ServiceOrder;
import br.com.oficina.mvp.dtos.enums.ServiceOrderStatus;
import br.com.oficina.mvp.domains.WorkOrderPart;
import br.com.oficina.mvp.domains.WorkOrderService;
import br.com.oficina.mvp.infra.ServiceOrderRepository;
import br.com.oficina.mvp.shared.exception.BusinessException;
import br.com.oficina.mvp.shared.validation.DocumentValidator;
import br.com.oficina.mvp.shared.validation.PlateValidator;
import br.com.oficina.mvp.domains.Vehicle;
import br.com.oficina.mvp.infra.VehicleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class ServiceOrderApplicationService {
    private final ServiceOrderRepository serviceOrders;
    private final CustomerRepository customers;
    private final VehicleRepository vehicles;
    private final ServiceCatalogItemRepository catalog;
    private final PartRepository parts;

    public ServiceOrderApplicationService(
            ServiceOrderRepository serviceOrders,
            CustomerRepository customers,
            VehicleRepository vehicles,
            ServiceCatalogItemRepository catalog,
            PartRepository parts
    ) {
        this.serviceOrders = serviceOrders;
        this.customers = customers;
        this.vehicles = vehicles;
        this.catalog = catalog;
        this.parts = parts;
    }

    @Transactional(readOnly = true)
    public List<ServiceOrderResponseDto> list() {
        return serviceOrders.findAll()
                .stream()
                .map(ServiceOrderResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ServiceOrderResponseDto findById(Long id) {
        return ServiceOrderResponseDto.from(findEntity(id));
    }

    @Transactional
    public ServiceOrderResponseDto create(CreateServiceOrderRequestDto request) {
        var document = DocumentValidator.requireValid(request.customerDocument());
        var plate = PlateValidator.requireValid(request.vehicle().plate());

        var customer = resolveCustomer(request.customer(), document);
        var vehicle = resolveVehicle(request.vehicle(), customer, plate);

        var order = new ServiceOrder(generateOrderCode(), customer, vehicle, request.customerNotes());
        addRequestedServices(order, request.services());
        addRequestedParts(order, request.parts());

        order.markBudgetWaitingApproval();
        return ServiceOrderResponseDto.from(serviceOrders.save(order));
    }

    private Customer resolveCustomer(CreateServiceOrderRequestDto.CustomerData data, String document) {
        var customer = customers.findByDocument(document)
                .orElseGet(() -> customers.save(new Customer(data.name(), document, data.email(), data.phone())));
        customer.update(data.name(), document, data.email(), data.phone());
        return customer;
    }

    private Vehicle resolveVehicle(CreateServiceOrderRequestDto.VehicleData data, Customer customer, String plate) {
        var vehicle = vehicles.findByPlate(plate)
                .orElseGet(() -> vehicles.save(new Vehicle(customer, plate, data.brand(), data.model(), data.manufacturingYear())));
        vehicle.update(customer, plate, data.brand(), data.model(), data.manufacturingYear());
        return vehicle;
    }

    private void addRequestedServices(ServiceOrder order, List<CreateServiceOrderRequestDto.ServiceItemData> requestedServices) {
        for (var requestedService : requestedServices) {
            var serviceItem = catalog.findById(requestedService.serviceItemId())
                    .filter(item -> Boolean.TRUE.equals(item.getActive()))
                    .orElseThrow(() -> new BusinessException("Um ou mais serviços informados não existem ou estão inativos.", HttpStatus.BAD_REQUEST));
            order.addService(new WorkOrderService(serviceItem, requestedService.quantity()));
        }
    }

    private void addRequestedParts(ServiceOrder order, List<CreateServiceOrderRequestDto.PartItemData> requestedParts) {
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

    @Transactional
    public ServiceOrderResponseDto approve(Long id, String comment) {
        var order = findEntity(id);
        validateStock(order);
        decrementStock(order);
        order.approve(comment);
        return ServiceOrderResponseDto.from(order);
    }

    @Transactional
    public ServiceOrderResponseDto updateStatus(Long id, ServiceOrderStatus status, String comment) {
        var order = findEntity(id);
        order.changeStatus(status, comment);
        return ServiceOrderResponseDto.from(order);
    }

    @Transactional(readOnly = true)
    public PublicServiceOrderResponseDto findPublicByCode(String code, String document) {
        var normalizedDocument = DocumentValidator.normalize(document);
        var order = serviceOrders.findByCode(code)
                .orElseThrow(() -> new BusinessException("Ordem de serviço não encontrada para os dados informados.", HttpStatus.NOT_FOUND));
        if (!order.getCustomer().getDocument().equals(normalizedDocument)) {
            throw new BusinessException("Ordem de serviço não encontrada para os dados informados.", HttpStatus.NOT_FOUND);
        }
        return PublicServiceOrderResponseDto.from(order);
    }

    @Transactional
    public PublicServiceOrderResponseDto approveByCustomer(String code, String document, String comment) {
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
        return PublicServiceOrderResponseDto.from(order);
    }

    public ServiceOrder findEntity(Long id) {
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

    private String generateOrderCode() {
        var date = DateTimeFormatter.BASIC_ISO_DATE.format(OffsetDateTime.now());
        var random = String.format("%05d", ThreadLocalRandom.current().nextInt(0, 100000));
        return "OS-" + date + "-" + random;
    }
}
