package br.com.oficina.mvp.serviceorder.application.port.out;

import br.com.oficina.mvp.serviceorder.domain.ServiceOrder;

import java.util.List;
import java.util.Optional;

public interface ServiceOrderRepositoryPort {
    List<ServiceOrder> findAll();

    // Ordenada por prioridade de status (EM_EXECUCAO > AGUARDANDO_APROVACAO > EM_DIAGNOSTICO > RECEBIDA, mais
    // antigas primeiro), excluindo OS em estado terminal (FINALIZADA, ENTREGUE, RECUSADA).
    List<ServiceOrder> findActiveOrderedByStatusPriority();

    Optional<ServiceOrder> findById(Long id);

    Optional<ServiceOrder> findByCode(String code);

    boolean existsByCode(String code);

    ServiceOrder save(ServiceOrder order);
}
