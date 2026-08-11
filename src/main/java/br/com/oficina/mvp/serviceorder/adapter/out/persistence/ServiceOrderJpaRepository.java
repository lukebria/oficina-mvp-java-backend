package br.com.oficina.mvp.serviceorder.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

interface ServiceOrderJpaRepository extends JpaRepository<ServiceOrderJpaEntity, Long> {
    Optional<ServiceOrderJpaEntity> findByCode(String code);

    boolean existsByCode(String code);

    @Query("""
            SELECT so FROM ServiceOrderJpaEntity so
            WHERE so.status NOT IN (
                br.com.oficina.mvp.shared.domain.ServiceOrderStatus.FINALIZADA,
                br.com.oficina.mvp.shared.domain.ServiceOrderStatus.ENTREGUE,
                br.com.oficina.mvp.shared.domain.ServiceOrderStatus.RECUSADA
            )
            ORDER BY CASE so.status
                WHEN br.com.oficina.mvp.shared.domain.ServiceOrderStatus.EM_EXECUCAO THEN 1
                WHEN br.com.oficina.mvp.shared.domain.ServiceOrderStatus.AGUARDANDO_APROVACAO THEN 2
                WHEN br.com.oficina.mvp.shared.domain.ServiceOrderStatus.EM_DIAGNOSTICO THEN 3
                WHEN br.com.oficina.mvp.shared.domain.ServiceOrderStatus.RECEBIDA THEN 4
                ELSE 5
            END, so.createdAt ASC
            """)
    List<ServiceOrderJpaEntity> findActiveOrderedByStatusPriority();
}
