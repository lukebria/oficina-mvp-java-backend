package br.com.oficina.mvp.auth.application.port.out;

import br.com.oficina.mvp.auth.domain.User;

import java.util.Optional;

public interface UserRepositoryPort {
    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    User save(User user);
}
