package com.beca.ms1_users.application;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.beca.ms1_users.application.port.in.ImportUsersCommand;
import com.beca.ms1_users.application.port.in.ImportUsersResult;
import com.beca.ms1_users.application.port.in.RegisterUserCommand;
import com.beca.ms1_users.application.port.in.UpdateUserCommand;
import com.beca.ms1_users.application.port.in.UserUseCase;
import com.beca.ms1_users.application.port.out.UserRepositoryPort;
import com.beca.ms1_users.domain.EmailAlreadyExistsException;
import com.beca.ms1_users.domain.User;
import com.beca.ms1_users.domain.UserNotFoundException;

import jakarta.validation.Validator;

@Service
public class UserService implements UserUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Validator validator;

    public UserService(
            UserRepositoryPort userRepository,
            PasswordEncoder passwordEncoder,
            Validator validator) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.validator = validator;
    }

    @Override
    public User registerUser(RegisterUserCommand command) {

        String email = normalizeEmail(command.email());

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(
                    "Email ja cadastrado"
            );
        }

        String senhaCriptografada = passwordEncoder.encode(command.senha());

        User user = new User(
                command.nome().trim(),
                email,
                senhaCriptografada
        );

        return userRepository.save(user);
    }

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User findUserById(UUID id) {

        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(
                        "Usuario nao encontrado"
                ));
    }

    @Override
    public User updateUser(UUID id, UpdateUserCommand command) {

        User user = findUserById(id);

        String email = normalizeEmail(command.email());

        boolean emailAlterado = !user.getEmail().equals(email);

        if (emailAlterado && userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(
                    "Email ja cadastrado"
            );
        }

        user.atualizar(
                command.nome().trim(),
                email
        );

        return userRepository.save(user);
    }

    @Override
    public void deleteUser(UUID id) {

        User user = findUserById(id);

        userRepository.delete(user);
    }

    @Override
    public ImportUsersResult importUsers(ImportUsersCommand command) {

        List<ImportUsersResult.ImportError> erros = new ArrayList<>();
        Set<String> emailsNoArquivo = new HashSet<>();

        int totalLinhas = 0;
        int importados = 0;

        byte[] content = command.content();

        if (content == null || content.length == 0) {
            throw new IllegalArgumentException(
                    "O arquivo CSV está vazio"
            );
        }

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .get();

        try (
                Reader reader = new InputStreamReader(
                        new ByteArrayInputStream(content),
                        StandardCharsets.UTF_8
                );
                CSVParser parser = format.parse(reader)
        ) {

            Set<String> headers = parser.getHeaderMap().keySet();

            if (!headers.containsAll(
                    Set.of("nome", "email", "senha")
            )) {
                throw new IllegalArgumentException(
                        "O CSV precisa conter as colunas: nome, email e senha"
                );
            }

            for (CSVRecord record : parser) {

                totalLinhas++;

                int numeroLinha = (int) record.getRecordNumber() + 1;

                if (!record.isConsistent()) {

                    erros.add(new ImportUsersResult.ImportError(
                            numeroLinha,
                            "Quantidade de colunas inválida"
                    ));

                    continue;
                }

                String nome = record.get("nome");
                String email = normalizeEmail(record.get("email"));
                String senha = record.get("senha");

                RegisterUserCommand registerCommand =
                        new RegisterUserCommand(nome, email, senha);

                var violations = validator.validate(registerCommand);

                if (!violations.isEmpty()) {

                    String motivo = violations.stream()
                            .map(violation -> violation.getMessage())
                            .sorted()
                            .reduce((a, b) -> a + "; " + b)
                            .orElse("Dados inválidos");

                    erros.add(new ImportUsersResult.ImportError(
                            numeroLinha,
                            motivo
                    ));

                    continue;
                }

                if (!emailsNoArquivo.add(email)) {

                    erros.add(new ImportUsersResult.ImportError(
                            numeroLinha,
                            "Email duplicado no arquivo"
                    ));

                    continue;
                }

                try {

                    registerUser(registerCommand);
                    importados++;

                } catch (EmailAlreadyExistsException exception) {

                    erros.add(new ImportUsersResult.ImportError(
                            numeroLinha,
                            "Email já cadastrado no sistema"
                    ));
                }
            }

        } catch (IOException exception) {

            throw new IllegalArgumentException(
                    "Não foi possível processar o arquivo CSV",
                    exception
            );
        }

        return new ImportUsersResult(
                totalLinhas,
                importados,
                erros.size(),
                erros
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}