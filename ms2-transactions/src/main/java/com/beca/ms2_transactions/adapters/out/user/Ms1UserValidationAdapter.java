package com.beca.ms2_transactions.adapters.out.user;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.beca.ms2_transactions.application.exception.UserNotFoundException;
import com.beca.ms2_transactions.application.exception.UserServiceUnavailableException;
import com.beca.ms2_transactions.application.port.out.UserInfo;
import com.beca.ms2_transactions.application.port.out.UserValidationPort;

@Component
public class Ms1UserValidationAdapter implements UserValidationPort {

    private final RestClient restClient;

    public Ms1UserValidationAdapter(
            RestClient.Builder builder,
            @Value("${ms1.users.base-url:http://localhost:8080}") String baseUrl,
            @Value("${MS1_SERVICE_EMAIL}") String serviceEmail,
            @Value("${MS1_SERVICE_PASSWORD}") String servicePassword) {

        this.restClient = builder.clone()
                .baseUrl(baseUrl)
                .defaultHeaders(headers ->
                        headers.setBasicAuth(serviceEmail, servicePassword))
                .build();
    }

    @Override
    public UserInfo validateUserExists(UUID userId) {

        try {
            UserInfo user = restClient.get()
                    .uri("/users/{id}", userId)
                    .retrieve()
                    .body(UserInfo.class);

            if (user == null || user.id() == null
                    || !userId.equals(user.id())) {
                throw new UserServiceUnavailableException(
                        "O MS1 retornou uma resposta de usuário inválida.",
                        null);
            }

            return user;

        } catch (HttpClientErrorException.NotFound exception) {

            throw new UserNotFoundException(
                    "Usuário não encontrado: " + userId);

        } catch (RestClientException exception) {

            throw new UserServiceUnavailableException(
                    "Não foi possível consultar o MS1.",
                    exception);
        }
    }
}