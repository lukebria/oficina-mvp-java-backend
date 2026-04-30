package br.com.oficina.mvp.serviceorders.domain;

import br.com.oficina.mvp.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

public final class ServiceOrderStatusPolicy {
    private static final Map<ServiceOrderStatus, List<ServiceOrderStatus>> ALLOWED = Map.of(
            ServiceOrderStatus.RECEBIDA, List.of(ServiceOrderStatus.EM_DIAGNOSTICO, ServiceOrderStatus.AGUARDANDO_APROVACAO),
            ServiceOrderStatus.EM_DIAGNOSTICO, List.of(ServiceOrderStatus.AGUARDANDO_APROVACAO),
            ServiceOrderStatus.AGUARDANDO_APROVACAO, List.of(ServiceOrderStatus.EM_EXECUCAO),
            ServiceOrderStatus.EM_EXECUCAO, List.of(ServiceOrderStatus.FINALIZADA),
            ServiceOrderStatus.FINALIZADA, List.of(ServiceOrderStatus.ENTREGUE),
            ServiceOrderStatus.ENTREGUE, List.of()
    );

    private ServiceOrderStatusPolicy() {}

    public static void assertTransition(ServiceOrderStatus current, ServiceOrderStatus next) {
        var allowed = allowedTransitions(current);
        if (!allowed.contains(next)) {
            throw new BusinessException("Transição de status inválida: " + current + " -> " + next + ".", HttpStatus.UNPROCESSABLE_ENTITY,
                    Map.of("current", current.name(), "next", next.name(), "allowed", allowed));
        }
    }

    public static List<ServiceOrderStatus> allowedTransitions(ServiceOrderStatus current) {
        return ALLOWED.getOrDefault(current, List.of());
    }
}
