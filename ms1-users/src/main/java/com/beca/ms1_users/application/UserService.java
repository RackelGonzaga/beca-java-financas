package com.beca.ms1_users.application;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.beca.ms1_users.application.port.in.ImportUserRowCommand;
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

    private static final int MAX_LINHAS = 1000;

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

        String senhaCriptografada =
                passwordEncoder.encode(command.senha());

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

        boolean emailAlterado =
                !user.getEmail().equals(email);

        if (emailAlterado && userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(
                    "Email ja cadastrado"
            );
        }

        user.atualizar(command.nome().trim(), email);

        return userRepository.save(user);
    }

    @Override
    public void deleteUser(UUID id) {

        User user = findUserById(id);

        userRepository.delete(user);
    }

    @Override
    public ImportUsersResult importUsers(ImportUsersCommand command) {

        if (command == null || command.usuarios() == null) {
            throw new IllegalArgumentException(
                    "A lista de usuários não pode ser nula"
            );
        }

        List<ImportUserRowCommand> usuarios = command.usuarios();

        if (usuarios.size() > MAX_LINHAS) {
            throw new IllegalArgumentException(
                    "O arquivo CSV não pode conter mais de "
                            + MAX_LINHAS + " registros"
            );
        }

        List<ImportUsersResult.ImportError> erros =
                new ArrayList<>();

        Set<String> emailsNoArquivo = new HashSet<>();

        int importados = 0;

        for (int i = 0; i < usuarios.size(); i++) {

            ImportUserRowCommand usuario = usuarios.get(i);

            int numeroLinha = usuario == null
                    ? i + 2
                    : usuario.numeroLinha();

            try {

                if (usuario == null) {
                    throw new IllegalArgumentException(
                            "Registro inválido"
                    );
                }

                RegisterUserCommand registerCommand =
                        new RegisterUserCommand(
                                usuario.nome(),
                                usuario.email(),
                                usuario.senha()
                        );

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

                String email = normalizeEmail(usuario.email());

                if (!emailsNoArquivo.add(email)) {

                    erros.add(new ImportUsersResult.ImportError(
                            numeroLinha,
                            "Email duplicado no arquivo"
                    ));

                    continue;
                }

                RegisterUserCommand comandoNormalizado =
                        new RegisterUserCommand(
                                usuario.nome(),
                                email,
                                usuario.senha()
                        );

                registerUser(comandoNormalizado);

                importados++;

            } catch (EmailAlreadyExistsException exception) {

                erros.add(new ImportUsersResult.ImportError(
                        numeroLinha,
                        "Email já cadastrado no sistema"
                ));

            } catch (DataIntegrityViolationException exception) {

                erros.add(new ImportUsersResult.ImportError(
                        numeroLinha,
                        "Os dados desta linha violam uma restrição do sistema"
                ));

            } catch (Exception exception) {

                erros.add(new ImportUsersResult.ImportError(
                        numeroLinha,
                        "Erro ao processar esta linha"
                ));
            }
        }

        return new ImportUsersResult(
                usuarios.size(),
                importados,
                erros.size(),
                erros
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}