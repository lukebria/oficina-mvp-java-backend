package br.com.oficina.mvp.customer.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface CustomerJpaRepository extends JpaRepository<CustomerJpaEntity, Long> {
    Optional<CustomerJpaEntity> findByDocument(String document);
}
