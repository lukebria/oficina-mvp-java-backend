package br.com.oficina.mvp.customer.domain;

import br.com.oficina.mvp.shared.domain.BaseDomain;

import java.time.OffsetDateTime;

public class Customer extends BaseDomain {
    private String name;
    private String document;
    private String email;
    private String phone;

    public Customer(String name, String document, String email, String phone) {
        this.name = name;
        this.document = document;
        this.email = email;
        this.phone = phone;
    }

    public Customer(Long id, OffsetDateTime createdAt, OffsetDateTime updatedAt,
                     String name, String document, String email, String phone) {
        super(id, createdAt, updatedAt);
        this.name = name;
        this.document = document;
        this.email = email;
        this.phone = phone;
    }

    public void update(String name, String document, String email, String phone) {
        if (name != null) this.name = name;
        if (document != null) this.document = document;
        this.email = email;
        this.phone = phone;
    }

    public String getName() { return name; }
    public String getDocument() { return document; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
}
