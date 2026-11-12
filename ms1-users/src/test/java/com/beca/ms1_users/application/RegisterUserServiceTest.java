package com.beca.ms1_users.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.ArgumentCaptor;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import com.beca.ms1_users.application.port.in.ImportUserRowCommand;
import com.beca.ms1_users.application.port.in.ImportUsersCommand;
import com.beca.ms1_users.application.port.in.ImportUsersResult;
import com.beca.ms1_users.application.port.in.RegisterUserCommand;
import com.beca.ms1_users.application.port.in.UpdateUserCommand;
import com.beca.ms1_users.application.port.out.UserRepositoryPort;
import com.beca.ms1_users.domain.EmailAlreadyExistsException;
import com.beca.ms1_users.domain.User;
import com.beca.ms1_users.domain.UserNotFoundException;
import com.beca.ms1_users.application.port.in.AuthenticateUserCommand;
import com.beca.ms1_users.domain.InvalidCredentialsException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RegisterUserServiceTest {

        private UserRepositoryPort userRepositoryPort;
        private PasswordEncoder passwordEncoder;
        private Validator validator;
        private ValidatorFactory validatorFactory;
        private UserService userService;

        @BeforeEach
        void setUp() {

                userRepositoryPort = mock(UserRepositoryPort.class);
                passwordEncoder = new BCryptPasswordEncoder();

                validatorFactory = Validation.buildDefaultValidatorFactory();
                validator = validatorFactory.getValidator();

                userService = new UserService(
                                userRepositoryPort,
                                passwordEncoder,
                                validator);
        }

        @AfterEach
        void tearDown() {
                validatorFactory.close();
        }

        // CADASTRO

        @Test
        void deveCadastrarUsuarioComSucesso() {

                RegisterUserCommand command = new RegisterUserCommand(
                                "Maria Silva",
                                "MARIA@EMAIL.COM",
                                "123456");

                when(userRepositoryPort.existsByEmail("maria@email.com"))
                                .thenReturn(false);

                when(userRepositoryPort.save(any(User.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                User response = userService.registerUser(command);

                ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

                verify(userRepositoryPort).save(captor.capture());

                User usuarioSalvo = captor.getValue();

                assertEquals("maria@email.com", usuarioSalvo.getEmail());
                assertEquals("Maria Silva", response.getNome());
                assertEquals("maria@email.com", response.getEmail());

                assertNotEquals("123456", usuarioSalvo.getSenha());
                assertTrue(passwordEncoder.matches(
                                "123456",
                                usuarioSalvo.getSenha()));

                verify(userRepositoryPort).existsByEmail("maria@email.com");
        }

        @Test
        void deveLancarExcecaoQuandoEmailJaEstiverCadastrado() {

                RegisterUserCommand command = new RegisterUserCommand(
                                "Maria Silva",
                                "maria@email.com",
                                "123456");

                when(userRepositoryPort.existsByEmail("maria@email.com"))
                                .thenReturn(true);

                EmailAlreadyExistsException exception = assertThrows(
                                EmailAlreadyExistsException.class,
                                () -> userService.registerUser(command));

                assertEquals("Email ja cadastrado", exception.getMessage());

                verify(userRepositoryPort, never()).save(any(User.class));
        }

        // ATUALIZAÇÃO

        @Test
        void deveLancarExcecaoAoAtualizarParaEmailJaCadastrado() {

                UUID id = UUID.randomUUID();

                User usuario = new User(
                                "Maria Silva",
                                "maria@email.com",
                                "123456");

                UpdateUserCommand command = new UpdateUserCommand(
                                "Maria Silva",
                                "joao@email.com");

                when(userRepositoryPort.findById(id))
                                .thenReturn(Optional.of(usuario));

                when(userRepositoryPort.existsByEmail("joao@email.com"))
                                .thenReturn(true);

                EmailAlreadyExistsException exception = assertThrows(
                                EmailAlreadyExistsException.class,
                                () -> userService.updateUser(id, command));

                assertEquals("Email ja cadastrado", exception.getMessage());

                verify(userRepositoryPort, never()).save(any(User.class));
        }

        @Test
        void deveAtualizarUsuarioComSucesso() {

                UUID id = UUID.randomUUID();

                User usuario = new User(
                                "Maria Silva",
                                "maria@email.com",
                                "123456");

                UpdateUserCommand command = new UpdateUserCommand(
                                "Maria Souza",
                                "joao@email.com");

                when(userRepositoryPort.findById(id))
                                .thenReturn(Optional.of(usuario));

                when(userRepositoryPort.existsByEmail("joao@email.com"))
                                .thenReturn(false);

                when(userRepositoryPort.save(any(User.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                User response = userService.updateUser(id, command);

                assertEquals("Maria Souza", response.getNome());
                assertEquals("joao@email.com", response.getEmail());

                verify(userRepositoryPort).save(usuario);
        }

        @Test
        void deveAtualizarUsuarioMantendoMesmoEmail() {

                UUID id = UUID.randomUUID();

                User usuario = new User(
                                "Maria Silva",
                                "maria@email.com",
                                "123456");

                UpdateUserCommand command = new UpdateUserCommand(
                                "Maria Souza",
                                "maria@email.com");

                when(userRepositoryPort.findById(id))
                                .thenReturn(Optional.of(usuario));

                when(userRepositoryPort.save(any(User.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                User response = userService.updateUser(id, command);

                assertEquals("Maria Souza", response.getNome());
                assertEquals("maria@email.com", response.getEmail());

                verify(userRepositoryPort, never())
                                .existsByEmail("maria@email.com");

                verify(userRepositoryPort).save(usuario);
        }

        // BUSCA

        @Test
        void deveBuscarUsuarioPorIdComSucesso() {

                UUID id = UUID.randomUUID();

                User usuario = new User(
                                "Maria Silva",
                                "maria@email.com",
                                "123456");

                when(userRepositoryPort.findById(id))
                                .thenReturn(Optional.of(usuario));

                User response = userService.findUserById(id);

                assertEquals("Maria Silva", response.getNome());
                assertEquals("maria@email.com", response.getEmail());
        }

        @Test
        void deveLancarExcecaoQuandoUsuarioNaoExistirNaBusca() {

                UUID id = UUID.randomUUID();

                when(userRepositoryPort.findById(id))
                                .thenReturn(Optional.empty());

                UserNotFoundException exception = assertThrows(
                                UserNotFoundException.class,
                                () -> userService.findUserById(id));

                assertEquals("Usuario nao encontrado", exception.getMessage());
        }

        @Test
        void deveListarTodosOsUsuariosComSucesso() {

                User maria = new User(
                                "Maria Silva",
                                "maria@email.com",
                                "123456");

                User joao = new User(
                                "João Souza",
                                "joao@email.com",
                                "654321");

                when(userRepositoryPort.findAll())
                                .thenReturn(List.of(maria, joao));

                List<User> response = userService.findAllUsers();

                assertEquals(2, response.size());

                assertEquals("Maria Silva", response.get(0).getNome());
                assertEquals("maria@email.com", response.get(0).getEmail());

                assertEquals("João Souza", response.get(1).getNome());
                assertEquals("joao@email.com", response.get(1).getEmail());

                verify(userRepositoryPort).findAll();
        }

        @Test
        void deveRetornarListaVaziaQuandoNaoExistiremUsuarios() {

                when(userRepositoryPort.findAll())
                                .thenReturn(List.of());

                List<User> response = userService.findAllUsers();

                assertEquals(0, response.size());

                verify(userRepositoryPort).findAll();
        }

        // EXCLUSÃO

        @Test
        void deveExcluirUsuarioComSucesso() {

                UUID id = UUID.randomUUID();

                User usuario = new User(
                                "Maria Silva",
                                "maria@email.com",
                                "123456");

                when(userRepositoryPort.findById(id))
                                .thenReturn(Optional.of(usuario));

                userService.deleteUser(id);

                verify(userRepositoryPort).delete(usuario);
        }

        @Test
        void deveLancarExcecaoAoExcluirUsuarioInexistente() {

                UUID id = UUID.randomUUID();

                when(userRepositoryPort.findById(id))
                                .thenReturn(Optional.empty());

                UserNotFoundException exception = assertThrows(
                                UserNotFoundException.class,
                                () -> userService.deleteUser(id));

                assertEquals("Usuario nao encontrado", exception.getMessage());
        }

        // IMPORTAÇÃO CSV

        @Test
        void deveImportarUsuarioComSucesso() {

                ImportUsersCommand command = new ImportUsersCommand(
                                List.of(
                                                new ImportUserRowCommand(
                                                                2,
                                                                "Maria Silva",
                                                                "maria@email.com",
                                                                "123456")));

                when(userRepositoryPort.existsByEmail("maria@email.com"))
                                .thenReturn(false);

                when(userRepositoryPort.save(any(User.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                ImportUsersResult resultado = userService.importUsers(command);

                assertEquals(1, resultado.totalLinhas());
                assertEquals(1, resultado.importados());
                assertEquals(0, resultado.rejeitados());
                assertTrue(resultado.erros().isEmpty());

                ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

                verify(userRepositoryPort).save(captor.capture());

                User usuarioSalvo = captor.getValue();

                assertEquals("Maria Silva", usuarioSalvo.getNome());
                assertEquals("maria@email.com", usuarioSalvo.getEmail());

                assertNotEquals("123456", usuarioSalvo.getSenha());
                assertTrue(passwordEncoder.matches(
                                "123456",
                                usuarioSalvo.getSenha()));
        }

        @Test
        void deveRealizarImportacaoParcialQuandoExistiremLinhasInvalidas() {

                ImportUsersCommand command = new ImportUsersCommand(
                                List.of(
                                                new ImportUserRowCommand(
                                                                2, "Maria Silva", "maria@email.com", "123456"),
                                                new ImportUserRowCommand(
                                                                3, "Ana Souza", "email-invalido", "123456"),
                                                new ImportUserRowCommand(
                                                                4, "Joao Silva", "joao@email.com", "123456")));

                when(userRepositoryPort.existsByEmail("maria@email.com"))
                                .thenReturn(false);

                when(userRepositoryPort.existsByEmail("joao@email.com"))
                                .thenReturn(false);

                when(userRepositoryPort.save(any(User.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                ImportUsersResult resultado = userService.importUsers(command);

                assertEquals(3, resultado.totalLinhas());
                assertEquals(2, resultado.importados());
                assertEquals(1, resultado.rejeitados());
                assertEquals(3, resultado.erros().get(0).linha());

                verify(userRepositoryPort, times(2)).save(any(User.class));
        }

        @Test
        void deveContinuarImportacaoAposErroInesperado() {

                ImportUsersCommand command = new ImportUsersCommand(
                                List.of(
                                                new ImportUserRowCommand(
                                                                2, "Maria Silva", "erro@email.com", "123456"),
                                                new ImportUserRowCommand(
                                                                3, "Ana Souza", "ana@email.com", "123456")));

                when(userRepositoryPort.existsByEmail("erro@email.com"))
                                .thenThrow(new RuntimeException("Falha simulada"));

                when(userRepositoryPort.existsByEmail("ana@email.com"))
                                .thenReturn(false);

                when(userRepositoryPort.save(any(User.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                ImportUsersResult resultado = userService.importUsers(command);

                assertEquals(2, resultado.totalLinhas());
                assertEquals(1, resultado.importados());
                assertEquals(1, resultado.rejeitados());

                assertEquals(2, resultado.erros().get(0).linha());
                assertEquals(
                                "Erro ao processar esta linha",
                                resultado.erros().get(0).motivo());

                verify(userRepositoryPort).existsByEmail("erro@email.com");
                verify(userRepositoryPort).existsByEmail("ana@email.com");
                verify(userRepositoryPort).save(any(User.class));
        }

        @Test
        void deveProcessarExatamenteMilRegistros() {

                List<ImportUserRowCommand> usuarios = new ArrayList<>();

                for (int i = 1; i <= 1000; i++) {
                        usuarios.add(new ImportUserRowCommand(
                                        i + 1,
                                        "Usuario " + i,
                                        "email-invalido",
                                        "123456"));
                }

                ImportUsersCommand command = new ImportUsersCommand(usuarios);

                ImportUsersResult resultado = userService.importUsers(command);

                assertEquals(1000, resultado.totalLinhas());
                assertEquals(0, resultado.importados());
                assertEquals(1000, resultado.rejeitados());

                verify(userRepositoryPort, never()).save(any(User.class));
        }

        @Test
        void deveRejeitarImportacaoComMaisDeMilRegistros() {

                List<ImportUserRowCommand> usuarios = new ArrayList<>();

                for (int i = 1; i <= 1001; i++) {
                        usuarios.add(new ImportUserRowCommand(
                                        i + 1,
                                        "Usuario " + i,
                                        "email-invalido",
                                        "123456"));
                }

                ImportUsersCommand command = new ImportUsersCommand(usuarios);

                assertThrows(
                                IllegalArgumentException.class,
                                () -> userService.importUsers(command));

                verify(userRepositoryPort, never()).save(any(User.class));
        }

        @Test
        void deveRejeitarEmailDuplicadoNoMesmoArquivo() {

                ImportUsersCommand command = new ImportUsersCommand(
                                List.of(
                                                new ImportUserRowCommand(
                                                                2,
                                                                "Maria Silva",
                                                                "maria@email.com",
                                                                "123456"),
                                                new ImportUserRowCommand(
                                                                3,
                                                                "Maria Souza",
                                                                "MARIA@EMAIL.COM",
                                                                "654321")));

                when(userRepositoryPort.existsByEmail("maria@email.com"))
                                .thenReturn(false);

                when(userRepositoryPort.save(any(User.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                ImportUsersResult resultado = userService.importUsers(command);

                assertEquals(2, resultado.totalLinhas());
                assertEquals(1, resultado.importados());
                assertEquals(1, resultado.rejeitados());

                assertEquals(3, resultado.erros().get(0).linha());
                assertEquals(
                                "Email duplicado no arquivo",
                                resultado.erros().get(0).motivo());

                verify(userRepositoryPort, times(1))
                                .existsByEmail("maria@email.com");

                verify(userRepositoryPort, times(1))
                                .save(any(User.class));
        }

        @Test
        void deveRejeitarEmailQueJaExisteNoBanco() {

                ImportUsersCommand command = new ImportUsersCommand(
                                List.of(
                                                new ImportUserRowCommand(
                                                                2,
                                                                "Maria Silva",
                                                                "maria@email.com",
                                                                "123456")));

                when(userRepositoryPort.existsByEmail("maria@email.com"))
                                .thenReturn(true);

                ImportUsersResult resultado = userService.importUsers(command);

                assertEquals(1, resultado.totalLinhas());
                assertEquals(0, resultado.importados());
                assertEquals(1, resultado.rejeitados());

                assertEquals(2, resultado.erros().get(0).linha());
                assertEquals(
                                "Email já cadastrado no sistema",
                                resultado.erros().get(0).motivo());

                verify(userRepositoryPort)
                                .existsByEmail("maria@email.com");

                verify(userRepositoryPort, never()).save(any(User.class));
        }

        @Test
        void deveContinuarImportacaoQuandoExistirRegistroNulo() {

                List<ImportUserRowCommand> usuarios = new ArrayList<>();

                usuarios.add(new ImportUserRowCommand(
                                2,
                                "Maria Silva",
                                "maria@email.com",
                                "123456"));

                usuarios.add(null);

                usuarios.add(new ImportUserRowCommand(
                                4,
                                "Ana Souza",
                                "ana@email.com",
                                "123456"));

                ImportUsersCommand command = new ImportUsersCommand(usuarios);

                when(userRepositoryPort.existsByEmail("maria@email.com"))
                                .thenReturn(false);

                when(userRepositoryPort.existsByEmail("ana@email.com"))
                                .thenReturn(false);

                when(userRepositoryPort.save(any(User.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                ImportUsersResult resultado = userService.importUsers(command);

                assertEquals(3, resultado.totalLinhas());
                assertEquals(2, resultado.importados());
                assertEquals(1, resultado.rejeitados());

                assertEquals(3, resultado.erros().get(0).linha());

                assertEquals(
                                "Erro ao processar esta linha",
                                resultado.erros().get(0).motivo());

                verify(userRepositoryPort, times(2))
                                .save(any(User.class));
        }

        // AUTENTICAÇÃO

        @Test
        void deveAutenticarUsuarioComSucesso() {

                String senha = "123456";

                String senhaCriptografada = passwordEncoder.encode(senha);

                User usuario = new User(
                                "Maria Silva",
                                "maria@email.com",
                                senhaCriptografada);

                AuthenticateUserCommand command = new AuthenticateUserCommand(
                                "MARIA@EMAIL.COM",
                                senha);

                when(userRepositoryPort.findByEmail("maria@email.com"))
                                .thenReturn(Optional.of(usuario));

                User response = userService.authenticateUser(command);

                assertEquals("Maria Silva", response.getNome());
                assertEquals("maria@email.com", response.getEmail());
                assertTrue(passwordEncoder.matches(
                                senha,
                                response.getSenha()));

                verify(userRepositoryPort).findByEmail("maria@email.com");
        }

        @Test
        void deveLancarExcecaoQuandoSenhaEstiverIncorreta() {

                String senhaCriptografada = passwordEncoder.encode("123456");

                User usuario = new User(
                                "Maria Silva",
                                "maria@email.com",
                                senhaCriptografada);

                AuthenticateUserCommand command = new AuthenticateUserCommand(
                                "maria@email.com",
                                "senhaerrada");

                when(userRepositoryPort.findByEmail("maria@email.com"))
                                .thenReturn(Optional.of(usuario));

                InvalidCredentialsException exception = assertThrows(
                                InvalidCredentialsException.class,
                                () -> userService.authenticateUser(command));

                assertEquals("Email ou senha inválidos", exception.getMessage());

                verify(userRepositoryPort).findByEmail("maria@email.com");
        }

        @Test
        void deveLancarExcecaoQuandoEmailNaoEstiverCadastrado() {

                AuthenticateUserCommand command = new AuthenticateUserCommand(
                                "inexistente@email.com",
                                "123456");

                when(userRepositoryPort.findByEmail("inexistente@email.com"))
                                .thenReturn(Optional.empty());

                InvalidCredentialsException exception = assertThrows(
                                InvalidCredentialsException.class,
                                () -> userService.authenticateUser(command));

                assertEquals("Email ou senha inválidos", exception.getMessage());

                verify(userRepositoryPort).findByEmail("inexistente@email.com");
        }
}