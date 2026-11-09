package com.beca.ms1_users.adapters.in;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.beca.ms1_users.application.port.in.ImportUsersCommand;
import com.beca.ms1_users.application.port.in.ImportUsersResult;
import com.beca.ms1_users.application.port.in.RegisterUserCommand;
import com.beca.ms1_users.application.port.in.UpdateUserCommand;
import com.beca.ms1_users.application.port.in.UserUseCase;
import com.beca.ms1_users.domain.User;
import com.beca.ms1_users.dto.UserRequestDTO;
import com.beca.ms1_users.dto.UserResponseDTO;
import com.beca.ms1_users.dto.UserUpdateRequestDTO;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserUseCase userUseCase;
    private final UserCsvParser userCsvParser;

    public UserController(
            UserUseCase userUseCase,
            UserCsvParser userCsvParser) {

        this.userUseCase = userUseCase;
        this.userCsvParser = userCsvParser;
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> registerUser(
            @Valid @RequestBody UserRequestDTO request) {

        RegisterUserCommand command = new RegisterUserCommand(
                request.getNome(),
                request.getEmail(),
                request.getSenha()
        );

        User user = userUseCase.registerUser(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toResponseDTO(user));
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> findAllUsers() {

        List<UserResponseDTO> users = userUseCase.findAllUsers()
                .stream()
                .map(this::toResponseDTO)
                .toList();

        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> findUserById(
            @PathVariable UUID id) {

        User user = userUseCase.findUserById(id);

        return ResponseEntity.ok(toResponseDTO(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserUpdateRequestDTO request) {

        UpdateUserCommand command = new UpdateUserCommand(
                request.getNome(),
                request.getEmail()
        );

        User user = userUseCase.updateUser(id, command);

        return ResponseEntity.ok(toResponseDTO(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable UUID id) {

        userUseCase.deleteUser(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping(
            value = "/import",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ImportUsersResult> importUsers(
            @RequestPart("file") MultipartFile file) {

        if (file.isEmpty()) {
            throw new IllegalArgumentException(
                    "O arquivo CSV está vazio"
            );
        }

        String filename = file.getOriginalFilename();

        if (filename == null
                || !filename.toLowerCase(Locale.ROOT).endsWith(".csv")) {

            throw new IllegalArgumentException(
                    "O arquivo precisa estar no formato CSV"
            );
        }

        try {
            var usuarios = userCsvParser.parse(file.getBytes());

            ImportUsersCommand command = new ImportUsersCommand(
                    usuarios
            );

            ImportUsersResult result =
                    userUseCase.importUsers(command);

            return ResponseEntity.ok(result);

        } catch (IOException exception) {
            throw new IllegalArgumentException(
                    "Não foi possível ler o arquivo CSV",
                    exception
            );
        }
    }

    private UserResponseDTO toResponseDTO(User user) {

        return new UserResponseDTO(
                user.getId(),
                user.getNome(),
                user.getEmail()
        );
    }
}