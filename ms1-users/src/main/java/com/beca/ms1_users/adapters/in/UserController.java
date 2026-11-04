
package com.beca.ms1_users.adapters.in;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import com.beca.ms1_users.application.port.in.UserUseCase;
import com.beca.ms1_users.application.port.in.RegisterUserCommand;
import com.beca.ms1_users.application.port.in.UpdateUserCommand;
import com.beca.ms1_users.domain.User;
import com.beca.ms1_users.dto.UserRequestDTO;
import com.beca.ms1_users.dto.UserResponseDTO;
import com.beca.ms1_users.dto.UserUpdateRequestDTO;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserUseCase userUseCase;

    public UserController(UserUseCase userUseCase) {
        this.userUseCase = userUseCase;
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> registerUser(
            @Valid @RequestBody UserRequestDTO userRequestDTO) {

        RegisterUserCommand command = new RegisterUserCommand(
                userRequestDTO.getNome(),
                userRequestDTO.getEmail(),
                userRequestDTO.getSenha()
        );

        User user = userUseCase.registerUser(command);

        return new ResponseEntity<>(toResponseDTO(user), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> findAllUsers() {

        List<UserResponseDTO> users = userUseCase.findAllUsers()
                .stream()
                .map(this::toResponseDTO)
                .toList();

        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> findUserById(
            @PathVariable UUID id) {

        User user = userUseCase.findUserById(id);

        return new ResponseEntity<>(toResponseDTO(user), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserUpdateRequestDTO userUpdateRequestDTO) {

        UpdateUserCommand command = new UpdateUserCommand(
                userUpdateRequestDTO.getNome(),
                userUpdateRequestDTO.getEmail()
        );

        User user = userUseCase.updateUser(id, command);

        return new ResponseEntity<>(toResponseDTO(user), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {

        userUseCase.deleteUser(id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private UserResponseDTO toResponseDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getNome(),
                user.getEmail()
        );
    }
}