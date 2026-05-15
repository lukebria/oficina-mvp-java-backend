package br.com.oficina.mvp.domains;

import br.com.oficina.mvp.domains.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "customers")
public class Customer extends BaseEntity {
    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String document;

    private String email;
    private String phone;

    protected Customer() {}

    public Customer(String name, String document, String email, String phone) {
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
