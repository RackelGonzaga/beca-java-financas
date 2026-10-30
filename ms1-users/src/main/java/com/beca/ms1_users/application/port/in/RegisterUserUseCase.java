package com.beca.ms1_users.application.port.in;

import com.beca.ms1_users.dto.UserUpdateRequestDTO;
import com.beca.ms1_users.dto.UserRequestDTO;
import com.beca.ms1_users.dto.UserResponseDTO;
import java.util.List;
import java.util.UUID;

public interface RegisterUserUseCase {
    UserResponseDTO registerUser(UserRequestDTO userRequestDTO);

    List<UserResponseDTO> findAllUsers();

    UserResponseDTO findUserById(UUID id);

    UserResponseDTO updateUser(UUID id, UserUpdateRequestDTO userUpdateRequestDTO);

    void deleteUser(UUID id);
}