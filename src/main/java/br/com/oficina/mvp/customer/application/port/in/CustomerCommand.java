package br.com.oficina.mvp.customer.application.port.in;

import br.com.oficina.mvp.customer.domain.CustomerStatus;

public record CustomerCommand(String name, String document, String email, String phone, CustomerStatus status) {}
