package br.com.oficina.mvp.catalog.application.port.in;

import java.math.BigDecimal;

public record CatalogCommand(String name, String description, BigDecimal basePrice, Integer estimatedMinutes, Boolean active) {}
