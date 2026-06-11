package br.com.oficina.mvp.serviceorders.domain;

import br.com.oficina.mvp.domains.ServiceOrderStatusPolicy;
import br.com.oficina.mvp.dtos.enums.ServiceOrderStatus;
import br.com.oficina.mvp.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceOrderStatusPolicyTest {

    @Test
    void shouldAllowExpectedTransitions() {
        ServiceOrderStatusPolicy.assertTransition(ServiceOrderStatus.RECEBIDA, ServiceOrderStatus.EM_DIAGNOSTICO);
        ServiceOrderStatusPolicy.assertTransition(ServiceOrderStatus.RECEBIDA, ServiceOrderStatus.AGUARDANDO_APROVACAO);
        ServiceOrderStatusPolicy.assertTransition(ServiceOrderStatus.EM_DIAGNOSTICO, ServiceOrderStatus.AGUARDANDO_APROVACAO);
        ServiceOrderStatusPolicy.assertTransition(ServiceOrderStatus.AGUARDANDO_APROVACAO, ServiceOrderStatus.EM_EXECUCAO);
        ServiceOrderStatusPolicy.assertTransition(ServiceOrderStatus.EM_EXECUCAO, ServiceOrderStatus.FINALIZADA);
        ServiceOrderStatusPolicy.assertTransition(ServiceOrderStatus.FINALIZADA, ServiceOrderStatus.ENTREGUE);
    }

    @Test
    void shouldRejectInvalidTransition() {
        assertThatThrownBy(() -> ServiceOrderStatusPolicy.assertTransition(ServiceOrderStatus.RECEBIDA, ServiceOrderStatus.ENTREGUE))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Transição de status inválida")
                .satisfies(ex -> {
                    var business = (BusinessException) ex;
                    assertThat(business.getDetails()).containsKeys("current", "next", "allowed");
                });
    }

    @Test
    void shouldExposeAllowedTransitionsForAllStatuses() {
        assertThat(ServiceOrderStatusPolicy.allowedTransitions(ServiceOrderStatus.RECEBIDA))
                .containsExactly(ServiceOrderStatus.EM_DIAGNOSTICO, ServiceOrderStatus.AGUARDANDO_APROVACAO);
        assertThat(ServiceOrderStatusPolicy.allowedTransitions(ServiceOrderStatus.EM_DIAGNOSTICO))
                .containsExactly(ServiceOrderStatus.AGUARDANDO_APROVACAO);
        assertThat(ServiceOrderStatusPolicy.allowedTransitions(ServiceOrderStatus.AGUARDANDO_APROVACAO))
                .containsExactly(ServiceOrderStatus.EM_EXECUCAO);
        assertThat(ServiceOrderStatusPolicy.allowedTransitions(ServiceOrderStatus.EM_EXECUCAO))
                .containsExactly(ServiceOrderStatus.FINALIZADA);
        assertThat(ServiceOrderStatusPolicy.allowedTransitions(ServiceOrderStatus.FINALIZADA))
                .containsExactly(ServiceOrderStatus.ENTREGUE);
        assertThat(ServiceOrderStatusPolicy.allowedTransitions(ServiceOrderStatus.ENTREGUE)).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ServiceOrderStatus.class, names = {"ENTREGUE", "FINALIZADA", "EM_EXECUCAO"})
    void shouldRejectBackwardTransitions(ServiceOrderStatus current) {
        assertThatThrownBy(() -> ServiceOrderStatusPolicy.assertTransition(current, ServiceOrderStatus.RECEBIDA))
                .isInstanceOf(BusinessException.class);
    }
}
