package br.com.oficina.mvp.serviceorders.domain;

import br.com.oficina.mvp.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceOrderStatusPolicyTest {
    @Test
    void shouldAllowExpectedTransitions() {
        ServiceOrderStatusPolicy.assertTransition(ServiceOrderStatus.RECEBIDA, ServiceOrderStatus.EM_DIAGNOSTICO);
        ServiceOrderStatusPolicy.assertTransition(ServiceOrderStatus.EM_DIAGNOSTICO, ServiceOrderStatus.AGUARDANDO_APROVACAO);
        ServiceOrderStatusPolicy.assertTransition(ServiceOrderStatus.AGUARDANDO_APROVACAO, ServiceOrderStatus.EM_EXECUCAO);
        ServiceOrderStatusPolicy.assertTransition(ServiceOrderStatus.EM_EXECUCAO, ServiceOrderStatus.FINALIZADA);
        ServiceOrderStatusPolicy.assertTransition(ServiceOrderStatus.FINALIZADA, ServiceOrderStatus.ENTREGUE);
    }

    @Test
    void shouldRejectInvalidTransition() {
        assertThatThrownBy(() -> ServiceOrderStatusPolicy.assertTransition(ServiceOrderStatus.RECEBIDA, ServiceOrderStatus.ENTREGUE))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Transição de status inválida");
    }

    @Test
    void shouldExposeAllowedTransitions() {
        assertThat(ServiceOrderStatusPolicy.allowedTransitions(ServiceOrderStatus.ENTREGUE)).isEmpty();
        assertThat(ServiceOrderStatusPolicy.allowedTransitions(ServiceOrderStatus.RECEBIDA))
                .containsExactly(ServiceOrderStatus.EM_DIAGNOSTICO, ServiceOrderStatus.AGUARDANDO_APROVACAO);
    }
}
