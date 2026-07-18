package br.com.oficina.mvp.serviceorder.adapter.out.persistence;

import br.com.oficina.mvp.serviceorder.domain.ServiceOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface ServiceOrderJpaRepository extends JpaRepository<ServiceOrder, Long> {
    Optional<ServiceOrder> findByCode(String code);

    boolean existsByCode(String code);
}
