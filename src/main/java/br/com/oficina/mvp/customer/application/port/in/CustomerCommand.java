package br.com.oficina.mvp.customer.application.port.in;

public record CustomerCommand(String name, String document, String email, String phone) {}
