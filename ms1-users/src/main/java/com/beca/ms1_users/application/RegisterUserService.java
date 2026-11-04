
package com.beca.ms1_users.application;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.beca.ms1_users.application.port.in.UserUseCase;
import com.beca.ms1_users.application.port.in.RegisterUserCommand;
import com.beca.ms1_users.application.port.in.UpdateUserCommand;
import com.beca.ms1_users.application.port.out.UserRepositoryPort;
import com.beca.ms1_users.domain.EmailAlreadyExistsException;
import com.beca.ms1_users.domain.User;
import com.beca.ms1_users.domain.UserNotFoundException;

@Service
public class RegisterUserService implements UserUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;

    public RegisterUserService(
            UserRepositoryPort userRepositoryPort,
            PasswordEncoder passwordEncoder) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User registerUser(RegisterUserCommand command) {
        String email = command.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (userRepositoryPort.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email ja cadastrado");
        }

        String senhaCriptografada = passwordEncoder.encode(
                command.senha()
        );

        User user = new User(
                command.nome(),
                email,
                senhaCriptografada
        );

        return userRepositoryPort.save(user);
    }

    @Override
    public List<User> findAllUsers() {
        return userRepositoryPort.findAll();
    }

    @Override
    public User findUserById(UUID id) {
        return userRepositoryPort.findById(id)
                .orElseThrow(() -> new UserNotFoundException(
                        "Usuario nao encontrado"
                ));
    }

    @Override
    public User updateUser(UUID id, UpdateUserCommand command) {
        User user = userRepositoryPort.findById(id)
                .orElseThrow(() -> new UserNotFoundException(
                        "Usuario nao encontrado"
                ));

        String email = command.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (!user.getEmail().equalsIgnoreCase(email)
                && userRepositoryPort.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email ja cadastrado");
        }

        user.atualizar(command.nome(), email);

        return userRepositoryPort.save(user);
    }

    @Override
    public void deleteUser(UUID id) {
        User user = userRepositoryPort.findById(id)
                .orElseThrow(() -> new UserNotFoundException(
                        "Usuario nao encontrado"
                ));

        userRepositoryPort.delete(user);
    }
}