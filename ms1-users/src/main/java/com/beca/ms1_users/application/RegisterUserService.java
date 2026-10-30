package com.beca.ms1_users.application;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.beca.ms1_users.application.port.in.RegisterUserUseCase;
import com.beca.ms1_users.application.port.out.UserRepositoryPort;
import com.beca.ms1_users.domain.User;
import com.beca.ms1_users.dto.UserRequestDTO;
import com.beca.ms1_users.dto.UserResponseDTO;
import com.beca.ms1_users.domain.UserNotFoundException;
import com.beca.ms1_users.dto.UserUpdateRequestDTO;

@Service
public class RegisterUserService implements RegisterUserUseCase {

    private final UserRepositoryPort userRepositoryPort;

    public RegisterUserService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public UserResponseDTO registerUser(UserRequestDTO userRequestDTO) {
        String email = userRequestDTO.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (userRepositoryPort.existsByEmail(email)) {
            throw new IllegalArgumentException("Email ja cadastrado");
        }

        User user = new User(
                userRequestDTO.getNome(),
                email,
                userRequestDTO.getSenha()
        );

        User savedUser = userRepositoryPort.save(user);

        return new UserResponseDTO(
                savedUser.getId(),
                savedUser.getNome(),
                savedUser.getEmail()
        );
    }

    @Override
    public List<UserResponseDTO> findAllUsers() {
        List<User> users = userRepositoryPort.findAll();
        return users.stream()
                .map(user -> new UserResponseDTO(
                        user.getId(),
                        user.getNome(),
                        user.getEmail()
                ))
                .toList();
    }

    @Override
    public UserResponseDTO findUserById(UUID id) {
        User user = userRepositoryPort.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario nao encontrado"));

        return new UserResponseDTO(
                user.getId(),
                user.getNome(),
                user.getEmail()
        );
    }

    @Override
    public UserResponseDTO updateUser(UUID id, UserUpdateRequestDTO userUpdateRequestDTO) {

    User user = userRepositoryPort.findById(id)
            .orElseThrow(() -> new UserNotFoundException("Usuario nao encontrado"));

    String email = userUpdateRequestDTO.getEmail()
            .trim()
            .toLowerCase(Locale.ROOT);

    if (!user.getEmail().equalsIgnoreCase(email)
            && userRepositoryPort.existsByEmail(email)) {
        throw new IllegalArgumentException("Email ja cadastrado");
    }

    user.atualizar(
            userUpdateRequestDTO.getNome(),
            email
    );

    User updatedUser = userRepositoryPort.save(user);

    return new UserResponseDTO(
            updatedUser.getId(),
            updatedUser.getNome(),
            updatedUser.getEmail()
    );
    }

    @Override
    public void deleteUser(UUID id) {
        User user = userRepositoryPort.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario nao encontrado"));

        userRepositoryPort.delete(user);
    }
}