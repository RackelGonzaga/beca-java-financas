package com.beca.ms1_users.adapters.in;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.beca.ms1_users.application.port.in.ImportUserRowCommand;
import com.beca.ms1_users.application.port.in.ImportUsersCommand;
import com.beca.ms1_users.application.port.in.ImportUsersResult;
import com.beca.ms1_users.application.port.in.RegisterUserCommand;
import com.beca.ms1_users.application.port.in.UpdateUserCommand;
import com.beca.ms1_users.application.port.in.UserUseCase;
import com.beca.ms1_users.domain.EmailAlreadyExistsException;
import com.beca.ms1_users.domain.User;
import com.beca.ms1_users.domain.UserNotFoundException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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

    @MockitoBean
    private UserCsvParser userCsvParser;

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

    // IMPORTACAO CSV

    @Test
    void deveImportarUsuariosPorArquivoCsv() throws Exception {

        MockMultipartFile arquivo = new MockMultipartFile(
                "file",
                "usuarios.csv",
                "text/csv",
                """
                nome,email,senha
                Maria,maria@email.com,123456
                """.getBytes()
        );

        List<ImportUserRowCommand> usuarios = List.of(
                new ImportUserRowCommand(
                        2,
                        "Maria",
                        "maria@email.com",
                        "123456"
                )
        );

        ImportUsersResult resultado = new ImportUsersResult(
                1,
                1,
                0,
                List.of()
        );

        when(userCsvParser.parse(any(byte[].class)))
                .thenReturn(usuarios);

        when(registerUserUseCase.importUsers(any(ImportUsersCommand.class)))
                .thenReturn(resultado);

        mockMvc.perform(multipart("/users/import")
                        .file(arquivo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalLinhas").value(1))
                .andExpect(jsonPath("$.importados").value(1))
                .andExpect(jsonPath("$.rejeitados").value(0))
                .andExpect(jsonPath("$.erros").isEmpty());

        verify(userCsvParser).parse(any(byte[].class));

        verify(registerUserUseCase).importUsers(
                argThat(command ->
                        command.usuarios().size() == 1
                                && command.usuarios().get(0).numeroLinha() == 2
                                && command.usuarios().get(0).nome().equals("Maria")
                                && command.usuarios().get(0).email().equals("maria@email.com")
                )
        );
    }

    @Test
    void deveRetornar400QuandoArquivoCsvEstiverVazio() throws Exception {

        MockMultipartFile arquivo = new MockMultipartFile(
                "file",
                "usuarios.csv",
                "text/csv",
                new byte[0]
        );

        mockMvc.perform(multipart("/users/import")
                        .file(arquivo))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar400QuandoArquivoNaoForCsv() throws Exception {

        MockMultipartFile arquivo = new MockMultipartFile(
                "file",
                "usuarios.txt",
                "text/plain",
                "conteudo".getBytes()
        );

        mockMvc.perform(multipart("/users/import")
                        .file(arquivo))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornarResultadoComErrosQuandoAlgumasLinhasForemRejeitadas()
            throws Exception {

        MockMultipartFile arquivo = new MockMultipartFile(
                "file",
                "usuarios.csv",
                "text/csv",
                """
                nome,email,senha
                Maria,maria@email.com,123456
                Joao,joao@email.com,123456
                """.getBytes()
        );

        List<ImportUserRowCommand> usuarios = List.of(
                new ImportUserRowCommand(2, "Maria", "maria@email.com", "123456"),
                new ImportUserRowCommand(3, "Joao", "joao@email.com", "123456")
        );

        ImportUsersResult resultado = new ImportUsersResult(
                2,
                1,
                1,
                List.of(
                        new ImportUsersResult.ImportError(
                                3,
                                "Email já cadastrado no sistema"
                        )
                )
        );

        when(userCsvParser.parse(any(byte[].class)))
                .thenReturn(usuarios);

        when(registerUserUseCase.importUsers(any(ImportUsersCommand.class)))
                .thenReturn(resultado);

        mockMvc.perform(multipart("/users/import")
                        .file(arquivo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalLinhas").value(2))
                .andExpect(jsonPath("$.importados").value(1))
                .andExpect(jsonPath("$.rejeitados").value(1))
                .andExpect(jsonPath("$.erros[0].linha").value(3))
                .andExpect(jsonPath("$.erros[0].motivo")
                        .value("Email já cadastrado no sistema"));
    }
}