package br.com.oficina.mvp.customer.domain;

import br.com.oficina.mvp.shared.domain.BaseDomain;

import java.time.OffsetDateTime;

public class Customer extends BaseDomain {
    private String name;
    private String document;
    private String email;
    private String phone;
    private CustomerStatus status;

    public Customer(String name, String document, String email, String phone) {
        this(name, document, email, phone, CustomerStatus.ACTIVE);
    }

    public Customer(String name, String document, String email, String phone, CustomerStatus status) {
        this.name = name;
        this.document = document;
        this.email = email;
        this.phone = phone;
        this.status = status;
    }

    public Customer(Long id, OffsetDateTime createdAt, OffsetDateTime updatedAt,
                     String name, String document, String email, String phone, CustomerStatus status) {
        super(id, createdAt, updatedAt);
        this.name = name;
        this.document = document;
        this.email = email;
        this.phone = phone;
        this.status = status;
    }

    public void update(String name, String document, String email, String phone, CustomerStatus status) {
        if (name != null) this.name = name;
        if (document != null) this.document = document;
        this.email = email;
        this.phone = phone;
        if (status != null) this.status = status;
    }

    public String getName() { return name; }
    public String getDocument() { return document; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public CustomerStatus getStatus() { return status; }
}
