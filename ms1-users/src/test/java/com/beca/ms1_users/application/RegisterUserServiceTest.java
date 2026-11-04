
package com.beca.ms1_users.application;

import java.util.UUID;
import java.util.Optional;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.beca.ms1_users.application.port.in.RegisterUserCommand;
import com.beca.ms1_users.application.port.in.UpdateUserCommand;
import com.beca.ms1_users.application.port.out.UserRepositoryPort;
import com.beca.ms1_users.domain.EmailAlreadyExistsException;
import com.beca.ms1_users.domain.User;
import com.beca.ms1_users.domain.UserNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterUserServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    private PasswordEncoder passwordEncoder;

    private RegisterUserService registerUserService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();

        registerUserService = new RegisterUserService(
                userRepositoryPort,
                passwordEncoder
        );
    }

    @Test
    void deveCadastrarUsuarioComSucesso() {

        RegisterUserCommand command = new RegisterUserCommand(
                "Maria Silva",
                " MARIA@EMAIL.COM ",
                "123456"
        );

        when(userRepositoryPort.existsByEmail("maria@email.com"))
                .thenReturn(false);

        when(userRepositoryPort.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User response = registerUserService.registerUser(command);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        verify(userRepositoryPort).save(userCaptor.capture());

        User usuarioSalvo = userCaptor.getValue();

        assertEquals("maria@email.com", usuarioSalvo.getEmail());
        assertEquals("Maria Silva", response.getNome());
        assertEquals("maria@email.com", response.getEmail());

        assertNotEquals("123456", usuarioSalvo.getSenha());
        assertTrue(passwordEncoder.matches("123456", usuarioSalvo.getSenha()));

        verify(userRepositoryPort).existsByEmail("maria@email.com");
    }

    @Test
    void deveLancarExcecaoQuandoEmailJaEstiverCadastrado() {

        RegisterUserCommand command = new RegisterUserCommand(
                "Maria Silva",
                "maria@email.com",
                "123456"
        );

        when(userRepositoryPort.existsByEmail("maria@email.com"))
                .thenReturn(true);

        EmailAlreadyExistsException exception = assertThrows(
                EmailAlreadyExistsException.class,
                () -> registerUserService.registerUser(command)
        );

        assertEquals("Email ja cadastrado", exception.getMessage());

        verify(userRepositoryPort, never()).save(any(User.class));
    }

    @Test
    void deveLancarExcecaoAoAtualizarComEmailJaCadastrado() {

        UUID id = UUID.randomUUID();

        User usuario = new User(
                "Maria Silva",
                "maria@email.com",
                "123456"
        );

        UpdateUserCommand command = new UpdateUserCommand(
                "Maria Silva",
                "joao@email.com"
        );

        when(userRepositoryPort.findById(id))
                .thenReturn(Optional.of(usuario));

        when(userRepositoryPort.existsByEmail("joao@email.com"))
                .thenReturn(true);

        EmailAlreadyExistsException exception = assertThrows(
                EmailAlreadyExistsException.class,
                () -> registerUserService.updateUser(id, command)
        );

        assertEquals("Email ja cadastrado", exception.getMessage());

        verify(userRepositoryPort, never()).save(any(User.class));
    }

    @Test
    void deveAtualizarUsuarioComSucesso() {

        UUID id = UUID.randomUUID();

        User usuario = new User(
                "Maria Silva",
                "maria@email.com",
                "123456"
        );

        UpdateUserCommand command = new UpdateUserCommand(
                "Maria Silva",
                "maria@email.com"
        );

        when(userRepositoryPort.findById(id))
                .thenReturn(Optional.of(usuario));

        when(userRepositoryPort.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User response = registerUserService.updateUser(id, command);

        assertEquals("Maria Silva", response.getNome());
        assertEquals("maria@email.com", response.getEmail());

        verify(userRepositoryPort).save(usuario);
    }

    @Test
    void deveAtualizarEmailParaUmNovoEmailComSucesso() {

        UUID id = UUID.randomUUID();

        User usuario = new User(
                "Maria Silva",
                "maria@email.com",
                "123456"
        );

        UpdateUserCommand command = new UpdateUserCommand(
                "Maria Souza",
                "joao@email.com"
        );

        when(userRepositoryPort.findById(id))
                .thenReturn(Optional.of(usuario));

        when(userRepositoryPort.existsByEmail("joao@email.com"))
                .thenReturn(false);

        when(userRepositoryPort.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User response = registerUserService.updateUser(id, command);

        assertEquals("Maria Souza", response.getNome());
        assertEquals("joao@email.com", response.getEmail());

        assertEquals("Maria Souza", usuario.getNome());
        assertEquals("joao@email.com", usuario.getEmail());

        verify(userRepositoryPort).existsByEmail("joao@email.com");
        verify(userRepositoryPort).save(usuario);
    }

    @Test
    void deveBuscarUsuarioPorIdComSucesso() {

        UUID id = UUID.randomUUID();

        User usuario = new User(
                "Maria Silva",
                "maria@email.com",
                "123456"
        );

        when(userRepositoryPort.findById(id))
                .thenReturn(Optional.of(usuario));

        User response = registerUserService.findUserById(id);

        assertEquals("Maria Silva", response.getNome());
        assertEquals("maria@email.com", response.getEmail());

        verify(userRepositoryPort).findById(id);
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoForEncontrado() {

        UUID id = UUID.randomUUID();

        when(userRepositoryPort.findById(id))
                .thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> registerUserService.findUserById(id)
        );

        assertEquals("Usuario nao encontrado", exception.getMessage());

        verify(userRepositoryPort).findById(id);
    }

    @Test
    void deveListarTodosOsUsuariosComSucesso() {

        User maria = new User(
                "Maria Silva",
                "maria@email.com",
                "123456"
        );

        User joao = new User(
                "João Souza",
                "joao@email.com",
                "654321"
        );

        when(userRepositoryPort.findAll())
                .thenReturn(List.of(maria, joao));

        List<User> response = registerUserService.findAllUsers();

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

        List<User> response = registerUserService.findAllUsers();

        assertEquals(0, response.size());

        verify(userRepositoryPort).findAll();
    }

    @Test
    void deveExcluirUsuarioComSucesso() {

        UUID id = UUID.randomUUID();

        User usuario = new User(
                "Maria Silva",
                "maria@email.com",
                "123456"
        );

        when(userRepositoryPort.findById(id))
                .thenReturn(Optional.of(usuario));

        registerUserService.deleteUser(id);

        verify(userRepositoryPort).delete(usuario);
    }

    @Test
    void deveLancarExcecaoAoExcluirUsuarioNaoEncontrado() {

        UUID id = UUID.randomUUID();

        when(userRepositoryPort.findById(id))
                .thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> registerUserService.deleteUser(id)
        );

        assertEquals("Usuario nao encontrado", exception.getMessage());

        verify(userRepositoryPort, never()).delete(any(User.class));
    }
}