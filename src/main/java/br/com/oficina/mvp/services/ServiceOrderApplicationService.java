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
import java.util.List;
import java.util.Map;
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
        var document = DocumentValidator.normalize(request.customerDocument());
        if (!DocumentValidator.isValidCpfOrCnpj(document)) {
            throw new BusinessException("CPF/CNPJ inválido.", HttpStatus.BAD_REQUEST);
        }

        var plate = PlateValidator.normalize(request.vehicle().plate());
        if (!PlateValidator.isValidBrazilianPlate(plate)) {
            throw new BusinessException("Placa de veículo inválida.", HttpStatus.BAD_REQUEST);
        }

        var customer = customers.findByDocument(document)
                .orElseGet(() -> customers.save(new Customer(
                        request.customer().name(), document, request.customer().email(), request.customer().phone()
                )));
        customer.update(request.customer().name(), document, request.customer().email(), request.customer().phone());

        var vehicle = vehicles.findByPlate(plate)
                .orElseGet(() -> vehicles.save(new Vehicle(
                        customer, plate, request.vehicle().brand(), request.vehicle().model(), request.vehicle().year()
                )));
        vehicle.update(customer, plate, request.vehicle().brand(), request.vehicle().model(), request.vehicle().year());

        var order = new ServiceOrder(generateOrderCode(), customer, vehicle, request.customerNotes());

        for (var requestedService : request.services()) {
            var serviceItem = catalog.findById(requestedService.serviceItemId())
                    .filter(item -> Boolean.TRUE.equals(item.getActive()))
                    .orElseThrow(() -> new BusinessException("Um ou mais serviços informados não existem ou estão inativos.", HttpStatus.BAD_REQUEST));
            order.addService(new WorkOrderService(serviceItem, requestedService.quantity()));
        }

        if (request.parts() != null) {
            for (var requestedPart : request.parts()) {
                var part = parts.findById(requestedPart.partId())
                        .filter(item -> Boolean.TRUE.equals(item.getActive()))
                        .orElseThrow(() -> new BusinessException("Uma ou mais peças informadas não existem ou estão inativas.", HttpStatus.BAD_REQUEST));
                order.addPart(new WorkOrderPart(part, requestedPart.quantity()));
            }
        }

        order.markBudgetWaitingApproval();
        return ServiceOrderResponseDto.from(serviceOrders.save(order));
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
                throw new BusinessException("Estoque insuficiente para a peça " + part.getName() + ".", HttpStatus.UNPROCESSABLE_ENTITY,
                        Map.of("partId", part.getId(), "available", part.getStockQuantity(), "requested", orderPart.getQuantity()));
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
