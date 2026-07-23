package br.com.oficina.mvp.customer.adapter.out.persistence;

import br.com.oficina.mvp.shared.persistence.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "customers")
public class CustomerJpaEntity extends BaseJpaEntity {
    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String document;

    private String email;
    private String phone;

    protected CustomerJpaEntity() {}

    CustomerJpaEntity(String name, String document, String email, String phone) {
        this.name = name;
        this.document = document;
        this.email = email;
        this.phone = phone;
    }

    String getName() { return name; }
    String getDocument() { return document; }
    String getEmail() { return email; }
    String getPhone() { return phone; }

    void setName(String name) { this.name = name; }
    void setDocument(String document) { this.document = document; }
    void setEmail(String email) { this.email = email; }
    void setPhone(String phone) { this.phone = phone; }
}
