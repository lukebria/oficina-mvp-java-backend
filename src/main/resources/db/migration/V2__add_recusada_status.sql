ALTER TABLE service_orders DROP CONSTRAINT service_orders_status_chk;
ALTER TABLE service_orders ADD CONSTRAINT service_orders_status_chk
    CHECK (status IN ('RECEBIDA', 'EM_DIAGNOSTICO', 'AGUARDANDO_APROVACAO', 'EM_EXECUCAO', 'FINALIZADA', 'ENTREGUE', 'RECUSADA'));

ALTER TABLE service_order_status_history DROP CONSTRAINT service_order_status_history_status_chk;
ALTER TABLE service_order_status_history ADD CONSTRAINT service_order_status_history_status_chk
    CHECK (status IN ('RECEBIDA', 'EM_DIAGNOSTICO', 'AGUARDANDO_APROVACAO', 'EM_EXECUCAO', 'FINALIZADA', 'ENTREGUE', 'RECUSADA'));
