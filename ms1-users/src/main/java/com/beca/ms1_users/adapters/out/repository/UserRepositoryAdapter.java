
package com.beca.ms1_users.adapters.out.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.beca.ms1_users.adapters.out.UserEntity;
import com.beca.ms1_users.adapters.out.UserRepository;
import com.beca.ms1_users.application.port.out.UserRepositoryPort;
import com.beca.ms1_users.domain.User;

@Component
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserRepository userRepository;

    public UserRepositoryAdapter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmailIgnoreCase(email);
    }

    @Override
    public User save(User user) {
        UserEntity entity = UserEntity.fromDomain(user);
        UserEntity savedEntity = userRepository.save(entity);

        return savedEntity.toDomain();
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll()
                .stream()
                .map(entity -> entity.toDomain())
                .toList();
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id)
                .map(entity -> entity.toDomain());
    }

    @Override
    public void delete(User user) {
        userRepository.deleteById(user.getId());
    }
}