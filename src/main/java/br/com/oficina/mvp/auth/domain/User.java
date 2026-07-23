package br.com.oficina.mvp.auth.domain;

import br.com.oficina.mvp.shared.domain.BaseDomain;
import br.com.oficina.mvp.shared.domain.Role;

import java.time.OffsetDateTime;

public class User extends BaseDomain {
    private final String name;
    private final String email;
    private final String passwordHash;
    private final Role role;

    public User(String name, String email, String passwordHash, Role role) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role == null ? Role.ATTENDANT : role;
    }

    public User(Long id, OffsetDateTime createdAt, OffsetDateTime updatedAt,
                String name, String email, String passwordHash, Role role) {
        super(id, createdAt, updatedAt);
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
}
