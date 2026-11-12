package com.beca.ms2_transactions.adapters.out.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.beca.ms2_transactions.application.exception.UserNotFoundException;
import com.beca.ms2_transactions.application.exception.UserServiceUnavailableException;
import com.beca.ms2_transactions.application.port.out.UserInfo;

class Ms1UserValidationAdapterTest {

    private static final String BASE_URL = "http://localhost:8080";
    private static final String EMAIL = "ms2.integracao@beca-financas.com";
    private static final String PASSWORD = "senha-teste";

    private RestClient.Builder builder;
    private MockRestServiceServer server;
    private Ms1UserValidationAdapter adapter;

    @BeforeEach
    void setUp() {
        builder = RestClient.builder();

        server = MockRestServiceServer.bindTo(builder).build();

        adapter = new Ms1UserValidationAdapter(
                builder,
                BASE_URL,
                EMAIL,
                PASSWORD
        );
    }

    @Test
    void deveValidarUsuarioComSucesso() {
        UUID userId = UUID.randomUUID();

        String response = """
                {
                    "id": "%s",
                    "nome": "Usuário Teste",
                    "email": "teste@email.com"
                }
                """.formatted(userId);

        server.expect(requestTo(BASE_URL + "/users/" + userId))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(
                        "Authorization",
                        basicAuthorization()
                ))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        UserInfo user = adapter.validateUserExists(userId);

        assertEquals(userId, user.id());
        assertEquals("Usuário Teste", user.nome());
        assertEquals("teste@email.com", user.email());

        server.verify();
    }

    @Test
    void deveLancarUserNotFoundQuandoUsuarioNaoExiste() {
        UUID userId = UUID.randomUUID();

        server.expect(requestTo(BASE_URL + "/users/" + userId))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(
                        "Authorization",
                        basicAuthorization()
                ))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThrows(
                UserNotFoundException.class,
                () -> adapter.validateUserExists(userId)
        );

        server.verify();
    }

    @Test
    void deveLancarUserServiceUnavailableQuandoMs1RetornarErro503() {
        UUID userId = UUID.randomUUID();

        server.expect(requestTo(BASE_URL + "/users/" + userId))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(
                        "Authorization",
                        basicAuthorization()
                ))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThrows(
                UserServiceUnavailableException.class,
                () -> adapter.validateUserExists(userId)
        );

        server.verify();
    }

    private String basicAuthorization() {
        String credentials = EMAIL + ":" + PASSWORD;

        String encoded = Base64.getEncoder().encodeToString(
                credentials.getBytes(StandardCharsets.UTF_8)
        );

        return "Basic " + encoded;
    }
}