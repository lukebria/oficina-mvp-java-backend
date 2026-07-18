package br.com.oficina.mvp.customer.adapter.out.persistence;

import br.com.oficina.mvp.customer.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface CustomerJpaRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByDocument(String document);
}
