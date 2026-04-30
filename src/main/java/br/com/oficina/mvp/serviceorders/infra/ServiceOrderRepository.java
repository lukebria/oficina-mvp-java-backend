package br.com.oficina.mvp.serviceorders.infra;

import br.com.oficina.mvp.serviceorders.domain.ServiceOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ServiceOrderRepository extends JpaRepository<ServiceOrder, Long> {
    Optional<ServiceOrder> findByCode(String code);
}
