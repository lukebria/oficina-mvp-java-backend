package br.com.oficina.mvp.customer.application;

import br.com.oficina.mvp.customer.application.port.in.CustomerCommand;
import br.com.oficina.mvp.customer.application.port.out.CustomerRepositoryPort;
import br.com.oficina.mvp.customer.domain.Customer;
import br.com.oficina.mvp.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    CustomerRepositoryPort customers;

    @InjectMocks
    CustomerService service;

    @Test
    void shouldListCustomers() {
        when(customers.findAll()).thenReturn(List.of(
                new Customer("João", "52998224725", "joao@email.com", "11999999999")
        ));

        assertThat(service.list()).hasSize(1);
    }

    @Test
    void shouldCreateCustomer() {
        var command = new CustomerCommand("João", "529.982.247-25", "joao@email.com", "11999999999");
        when(customers.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.create(command);

        assertThat(result.getName()).isEqualTo("João");
        assertThat(result.getDocument()).isEqualTo("52998224725");
    }

    @Test
    void shouldRejectInvalidDocument() {
        var command = new CustomerCommand("João", "111.111.111-11", null, null);

        assertThatThrownBy(() -> service.create(command))
                .isInstanceOfSatisfying(BusinessException.class, ex ->
                        assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void shouldFindById() {
        var customer = new Customer("João", "52998224725", "joao@email.com", null);
        when(customers.findById(1L)).thenReturn(Optional.of(customer));

        assertThat(service.findById(1L).getName()).isEqualTo("João");
    }

    @Test
    void shouldThrowWhenNotFound() {
        when(customers.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cliente não encontrado");
    }

    @Test
    void shouldUpdateCustomer() {
        var customer = new Customer("João", "52998224725", "joao@email.com", null);
        when(customers.findById(1L)).thenReturn(Optional.of(customer));

        var command = new CustomerCommand("João Atualizado", "529.982.247-25", "novo@email.com", "11888888888");
        var result = service.update(1L, command);

        assertThat(result.getName()).isEqualTo("João Atualizado");
    }

    @Test
    void shouldDeleteCustomer() {
        var customer = new Customer("João", "52998224725", null, null);
        when(customers.findById(1L)).thenReturn(Optional.of(customer));

        service.delete(1L);

        verify(customers).delete(customer);
    }
}
