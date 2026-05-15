package br.com.oficina.mvp.infra;

import br.com.oficina.mvp.domains.ServiceOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ServiceOrderRepository extends JpaRepository<ServiceOrder, Long> {
    Optional<ServiceOrder> findByCode(String code);
}
