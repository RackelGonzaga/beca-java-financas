package com.beca.ms1_users.adapters.out;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import com.beca.ms1_users.adapters.out.repository.UserRepositoryAdapter;
import com.beca.ms1_users.domain.EmailAlreadyExistsException;
import com.beca.ms1_users.domain.User;

class UserRepositoryAdapterTest {

    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
    }

    @Test
    void deveConverterViolacaoDeEmailDuplicadoEmExcecaoDeEmailJaCadastrado() {

        UserRepositoryAdapter adapter =
                new UserRepositoryAdapter(userRepository);

        User user = new User(
                "Maria",
                "maria@email.com",
                "senha-criptografada"
        );

        SQLException sqlException = new SQLException(
                "ERROR: duplicate key value violates unique constraint "
                        + "\"uk_users_email\" Detail: Key (email)=(maria@email.com) already exists.",
                "23505"
        );

        ConstraintViolationException constraintException =
                new ConstraintViolationException(
                        "Email duplicado",
                        sqlException,
                        "uk_users_email"
                );

        DataIntegrityViolationException databaseException =
                new DataIntegrityViolationException(
                        "Erro de integridade",
                        constraintException
                );

        when(userRepository.saveAndFlush(any(UserEntity.class)))
                .thenThrow(databaseException);

        EmailAlreadyExistsException exception = assertThrows(
                EmailAlreadyExistsException.class,
                () -> adapter.save(user)
        );

        assertEquals("Email ja cadastrado", exception.getMessage());

        verify(userRepository).saveAndFlush(any(UserEntity.class));
    }

    @Test
    void devePropagarOutrasViolacoesDeIntegridadeDoBanco() {

        UserRepositoryAdapter adapter =
                new UserRepositoryAdapter(userRepository);

        User user = new User(
                "Maria",
                "maria@email.com",
                "senha-criptografada"
        );

        SQLException sqlException = new SQLException(
                "Violacao de outra restricao unica",
                "23505"
        );

        ConstraintViolationException constraintException =
                new ConstraintViolationException(
                        "Outra restricao violada",
                        sqlException,
                        "uk_users_nome"
                );

        DataIntegrityViolationException databaseException =
                new DataIntegrityViolationException(
                        "Erro de integridade",
                        constraintException
                );

        when(userRepository.saveAndFlush(any(UserEntity.class)))
                .thenThrow(databaseException);

        DataIntegrityViolationException exception = assertThrows(
                DataIntegrityViolationException.class,
                () -> adapter.save(user)
        );

        assertSame(databaseException, exception);

        verify(userRepository).saveAndFlush(any(UserEntity.class));
    }
}