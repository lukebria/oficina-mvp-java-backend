package br.com.oficina.mvp.part.application.port.in;

import java.math.BigDecimal;

public record PartCommand(String name, String sku, BigDecimal unitPrice, Integer stockQuantity, Integer minStock, Boolean active) {}
