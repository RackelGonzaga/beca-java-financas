package com.beca.ms1_users.adapters.in;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import com.beca.ms1_users.dto.UserUpdateRequestDTO;
import org.springframework.web.bind.annotation.DeleteMapping;

import jakarta.validation.Valid;

import com.beca.ms1_users.application.port.in.RegisterUserUseCase;
import com.beca.ms1_users.dto.UserRequestDTO;
import com.beca.ms1_users.dto.UserResponseDTO;
import com.beca.ms1_users.domain.UserNotFoundException;

@RestController
@RequestMapping("/users")
public class UserController {

    private final RegisterUserUseCase registerUserUseCase;

    public UserController(RegisterUserUseCase registerUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> registerUser(@Valid @RequestBody UserRequestDTO userRequestDTO) {
        UserResponseDTO userResponseDTO = registerUserUseCase.registerUser(userRequestDTO);
        return new ResponseEntity<>(userResponseDTO, HttpStatus.CREATED);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(
        IllegalArgumentException exception) {

        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(exception.getMessage());
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> findAllUsers() {
        List<UserResponseDTO> users = registerUserUseCase.findAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> findUserById(@PathVariable UUID id) {
        UserResponseDTO user = registerUserUseCase.findUserById(id);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException(
        UserNotFoundException exception) {

        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(exception.getMessage());
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
        @PathVariable UUID id,
        @Valid @RequestBody UserUpdateRequestDTO userUpdateRequestDTO) {

        UserResponseDTO user = registerUserUseCase.updateUser(id, userUpdateRequestDTO);

        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        registerUserUseCase.deleteUser(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}