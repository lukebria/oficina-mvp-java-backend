package br.com.oficina.mvp.serviceorder.adapter.out.persistence;

import br.com.oficina.mvp.catalog.adapter.out.persistence.ServiceCatalogItemJpaEntity;
import br.com.oficina.mvp.catalog.adapter.out.persistence.ServiceCatalogItemMapper;
import br.com.oficina.mvp.customer.adapter.out.persistence.CustomerJpaEntity;
import br.com.oficina.mvp.customer.adapter.out.persistence.CustomerMapper;
import br.com.oficina.mvp.part.adapter.out.persistence.PartJpaEntity;
import br.com.oficina.mvp.part.adapter.out.persistence.PartMapper;
import br.com.oficina.mvp.serviceorder.domain.ServiceOrder;
import br.com.oficina.mvp.serviceorder.domain.ServiceOrderStatusHistory;
import br.com.oficina.mvp.serviceorder.domain.WorkOrderPart;
import br.com.oficina.mvp.serviceorder.domain.WorkOrderService;
import br.com.oficina.mvp.vehicle.adapter.out.persistence.VehicleJpaEntity;
import br.com.oficina.mvp.vehicle.adapter.out.persistence.VehicleMapper;
import jakarta.persistence.EntityManager;

final class ServiceOrderMapper {
    private ServiceOrderMapper() {}

    static ServiceOrder toDomain(ServiceOrderJpaEntity entity) {
        var order = new ServiceOrder(
                entity.getId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getCode(),
                CustomerMapper.toDomain(entity.getCustomer()),
                VehicleMapper.toDomain(entity.getVehicle()),
                entity.getStatus(),
                entity.getCustomerNotes(),
                entity.getDiagnosis(),
                entity.getTotalServices(),
                entity.getTotalParts(),
                entity.getTotalAmount(),
                entity.getApprovedAt(),
                entity.getStartedAt(),
                entity.getFinalizedAt(),
                entity.getDeliveredAt()
        );

        entity.getServices().forEach(item -> order.getServices().add(new WorkOrderService(
                item.getId(),
                ServiceCatalogItemMapper.toDomain(item.getServiceItem()),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getTotalPrice()
        )));

        entity.getParts().forEach(item -> order.getParts().add(new WorkOrderPart(
                item.getId(),
                PartMapper.toDomain(item.getPart()),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getTotalPrice()
        )));

        entity.getHistory().forEach(item -> order.getHistory().add(new ServiceOrderStatusHistory(
                item.getId(),
                order,
                item.getStatus(),
                item.getComment(),
                item.getChangedAt()
        )));

        return order;
    }

    static ServiceOrderJpaEntity toNewEntity(ServiceOrder domain, EntityManager entityManager) {
        var customerRef = entityManager.getReference(CustomerJpaEntity.class, domain.getCustomer().getId());
        var vehicleRef = entityManager.getReference(VehicleJpaEntity.class, domain.getVehicle().getId());

        var entity = new ServiceOrderJpaEntity(domain.getCode(), customerRef, vehicleRef, domain.getCustomerNotes());
        applyScalars(domain, entity);

        for (WorkOrderService item : domain.getServices()) {
            var serviceItemRef = entityManager.getReference(ServiceCatalogItemJpaEntity.class, item.getServiceItem().getId());
            entity.getServices().add(new WorkOrderServiceJpaEntity(entity, serviceItemRef, item.getQuantity(), item.getUnitPrice(), item.getTotalPrice()));
        }

        for (WorkOrderPart item : domain.getParts()) {
            var partRef = entityManager.getReference(PartJpaEntity.class, item.getPart().getId());
            entity.getParts().add(new WorkOrderPartJpaEntity(entity, partRef, item.getQuantity(), item.getUnitPrice(), item.getTotalPrice()));
        }

        for (ServiceOrderStatusHistory item : domain.getHistory()) {
            entity.getHistory().add(new ServiceOrderStatusHistoryJpaEntity(entity, item.getStatus(), item.getComment(), item.getChangedAt()));
        }

        return entity;
    }

    // services/parts nunca mudam após a criação da OS (addService/addPart só são chamados em ServiceOrderService.create),
    // então update só precisa sincronizar campos escalares e anexar entradas novas de histórico (que só cresce, nunca remove).
    static void applyToEntity(ServiceOrder domain, ServiceOrderJpaEntity entity) {
        applyScalars(domain, entity);

        var newHistoryEntries = domain.getHistory().subList(entity.getHistory().size(), domain.getHistory().size());
        for (ServiceOrderStatusHistory item : newHistoryEntries) {
            entity.getHistory().add(new ServiceOrderStatusHistoryJpaEntity(entity, item.getStatus(), item.getComment(), item.getChangedAt()));
        }
    }

    private static void applyScalars(ServiceOrder domain, ServiceOrderJpaEntity entity) {
        entity.setStatus(domain.getStatus());
        entity.setDiagnosis(domain.getDiagnosis());
        entity.setTotalServices(domain.getTotalServices());
        entity.setTotalParts(domain.getTotalParts());
        entity.setTotalAmount(domain.getTotalAmount());
        entity.setApprovedAt(domain.getApprovedAt());
        entity.setStartedAt(domain.getStartedAt());
        entity.setFinalizedAt(domain.getFinalizedAt());
        entity.setDeliveredAt(domain.getDeliveredAt());
    }
}
