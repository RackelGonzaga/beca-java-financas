
package com.beca.ms1_users.adapters.in;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.beca.ms1_users.application.port.in.RegisterUserCommand;
import com.beca.ms1_users.application.port.in.UserUseCase;
import com.beca.ms1_users.application.port.in.UpdateUserCommand;
import com.beca.ms1_users.domain.EmailAlreadyExistsException;
import com.beca.ms1_users.domain.User;
import com.beca.ms1_users.domain.UserNotFoundException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserUseCase registerUserUseCase;


    // CADASTRO

    @Test
    void deveCadastrarUsuarioComSucesso() throws Exception {

        UUID id = UUID.randomUUID();

        User user = new User(
            id,
            "Maria",
            "maria@email.com",
            "$2a$10$senhaCriptografada"
        );

        when(registerUserUseCase.registerUser(any(RegisterUserCommand.class)))
            .thenReturn(user);

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "nome": "Maria",
                        "email": "maria@email.com",
                        "senha": "123456"
                    }
                    """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.nome").value("Maria"))
                .andExpect(jsonPath("$.email").value("maria@email.com"))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }


    @Test
    void deveRetornar400QuandoDadosDoCadastroForemInvalidos() throws Exception {

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "nome": "",
                        "email": "email-invalido",
                        "senha": ""
                    }
                    """))
                .andExpect(status().isBadRequest());
    }


    @Test
    void deveRetornar409QuandoEmailJaEstiverCadastrado() throws Exception {

        when(registerUserUseCase.registerUser(any(RegisterUserCommand.class)))
            .thenThrow(new EmailAlreadyExistsException("Email ja cadastrado"));

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "nome": "Maria",
                        "email": "maria@email.com",
                        "senha": "123456"
                    }
                    """))
                .andExpect(status().isConflict());
    }


    // LISTAGEM

    @Test
    void deveListarTodosOsUsuarios() throws Exception {

        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        User user1 = new User(id1, "Maria", "maria@email.com", "senha1");
        User user2 = new User(id2, "Joao", "joao@email.com", "senha2");

        when(registerUserUseCase.findAllUsers())
            .thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nome").value("Maria"))
                .andExpect(jsonPath("$[0].email").value("maria@email.com"))
                .andExpect(jsonPath("$[1].nome").value("Joao"))
                .andExpect(jsonPath("$[1].email").value("joao@email.com"))
                .andExpect(jsonPath("$[0].senha").doesNotExist());
    }


    // BUSCA POR ID

    @Test
    void deveBuscarUsuarioPorId() throws Exception {

        UUID id = UUID.randomUUID();

        User user = new User(
            id,
            "Maria",
            "maria@email.com",
            "senhaCriptografada"
        );

        when(registerUserUseCase.findUserById(id))
            .thenReturn(user);

        mockMvc.perform(get("/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.nome").value("Maria"))
                .andExpect(jsonPath("$.email").value("maria@email.com"))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }


    @Test
    void deveRetornar404QuandoUsuarioNaoExistir() throws Exception {

        UUID id = UUID.randomUUID();

        when(registerUserUseCase.findUserById(id))
            .thenThrow(new UserNotFoundException("Usuario nao encontrado"));

        mockMvc.perform(get("/users/{id}", id))
                .andExpect(status().isNotFound());
    }


    // ATUALIZACAO

    @Test
    void deveAtualizarUsuarioComSucesso() throws Exception {

        UUID id = UUID.randomUUID();

        User userAtualizado = new User(
            id,
            "Maria Silva",
            "maria.silva@email.com",
            "senhaCriptografada"
        );

        when(registerUserUseCase.updateUser(
                eq(id),
                any(UpdateUserCommand.class)
        )).thenReturn(userAtualizado);

        mockMvc.perform(put("/users/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "nome": "Maria Silva",
                        "email": "maria.silva@email.com"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.nome").value("Maria Silva"))
                .andExpect(jsonPath("$.email").value("maria.silva@email.com"))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }


    @Test
    void deveRetornar404AoAtualizarUsuarioInexistente() throws Exception {

        UUID id = UUID.randomUUID();

        when(registerUserUseCase.updateUser(
                eq(id),
                any(UpdateUserCommand.class)
        )).thenThrow(new UserNotFoundException("Usuario nao encontrado"));

        mockMvc.perform(put("/users/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "nome": "Maria Silva",
                        "email": "maria.silva@email.com"
                    }
                    """))
                .andExpect(status().isNotFound());
    }


    // EXCLUSAO

    @Test
    void deveExcluirUsuarioComSucesso() throws Exception {

        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/users/{id}", id))
                .andExpect(status().isNoContent());

        verify(registerUserUseCase).deleteUser(id);
    }


    @Test
    void deveRetornar404AoExcluirUsuarioInexistente() throws Exception {

        UUID id = UUID.randomUUID();

        doThrow(new UserNotFoundException("Usuario nao encontrado"))
            .when(registerUserUseCase).deleteUser(id);

        mockMvc.perform(delete("/users/{id}", id))
                .andExpect(status().isNotFound());
    }

}