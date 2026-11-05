package com.beca.ms1_users.adapters.in;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.beca.ms1_users.domain.EmailAlreadyExistsException;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void deveRetornarHttp409QuandoEmailJaEstiverCadastrado() {

        EmailAlreadyExistsException exception =
                new EmailAlreadyExistsException("Email ja cadastrado");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                handler.handleEmailAlreadyExists(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());

        assertNotNull(response.getBody());

        assertEquals(409, response.getBody().status());

        assertEquals(
                "Email ja cadastrado",
                response.getBody().message()
        );
    }
}