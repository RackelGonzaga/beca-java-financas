package com.beca.ms1_users.adapters.out.repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import com.beca.ms1_users.adapters.out.UserEntity;
import com.beca.ms1_users.adapters.out.UserRepository;
import com.beca.ms1_users.application.port.out.UserRepositoryPort;
import com.beca.ms1_users.domain.EmailAlreadyExistsException;
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

        try {
            UserEntity savedEntity = userRepository.saveAndFlush(entity);

            return savedEntity.toDomain();

        } catch (DataIntegrityViolationException exception) {

            if (isEmailUniqueViolation(exception)) {
                throw new EmailAlreadyExistsException(
                        "Email ja cadastrado"
                );
            }

            throw exception;
        }
    }

    private boolean isEmailUniqueViolation(
            DataIntegrityViolationException exception) {

        Throwable cause = exception;

        while (cause != null) {

            if (cause instanceof ConstraintViolationException violation) {

                String constraintName = violation.getConstraintName();

                if (constraintName != null
                        && constraintName.toLowerCase(Locale.ROOT).contains("email")) {
                    return true;
                }
            }

            if (cause instanceof SQLException sqlException) {

                if ("23505".equals(sqlException.getSQLState())) {

                    String message = sqlException.getMessage();

                    if (message != null
                            && message.toLowerCase(Locale.ROOT).contains("email")) {
                        return true;
                    }
                }
            }

            cause = cause.getCause();
        }

        return false;
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

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
            .map(entity -> entity.toDomain());
    }
}