package com.beca.ms2_transactions.adapters.out.user;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.beca.ms2_transactions.application.exception.UserNotFoundException;
import com.beca.ms2_transactions.application.exception.UserServiceUnavailableException;
import com.beca.ms2_transactions.application.port.out.UserValidationPort;

@Component
public class Ms1UserValidationAdapter implements UserValidationPort {

    private final RestClient restClient;

    public Ms1UserValidationAdapter(
            RestClient.Builder builder,
            @Value("${ms1.users.base-url:http://localhost:8080}") String baseUrl) {

        this.restClient = builder
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public void validateUserExists(UUID userId) {

        try {
            restClient.get()
                    .uri("/users/{id}", userId)
                    .retrieve()
                    .toBodilessEntity();

        } catch (HttpClientErrorException.NotFound exception) {

            throw new UserNotFoundException(
                    "Usuário não encontrado: " + userId
            );

        } catch (RestClientException exception) {

            throw new UserServiceUnavailableException(
                    "Não foi possível consultar o MS1.",
                    exception
            );
        }
    }
}