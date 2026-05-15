package br.com.oficina.mvp.infra;

import br.com.oficina.mvp.domains.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByDocument(String document);
}
