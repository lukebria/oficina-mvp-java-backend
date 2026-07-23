package br.com.oficina.mvp.auth.adapter.out.persistence;

import br.com.oficina.mvp.auth.domain.User;

final class UserMapper {
    private UserMapper() {}

    static User toDomain(UserJpaEntity entity) {
        return new User(
                entity.getId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getName(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getRole()
        );
    }

    static UserJpaEntity toNewEntity(User domain) {
        return new UserJpaEntity(domain.getName(), domain.getEmail(), domain.getPasswordHash(), domain.getRole());
    }
}
