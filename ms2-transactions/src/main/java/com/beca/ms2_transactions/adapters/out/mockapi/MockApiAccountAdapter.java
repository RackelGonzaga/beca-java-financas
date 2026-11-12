package com.beca.ms2_transactions.adapters.out.mockapi;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.beca.ms2_transactions.application.exception.BalanceServiceUnavailableException;
import com.beca.ms2_transactions.application.port.out.AccountBalancePort;
import com.beca.ms2_transactions.domain.AccountBalance;

@Component
public class MockApiAccountAdapter implements AccountBalancePort {

    private final RestClient restClient;

    public MockApiAccountAdapter(
            RestClient.Builder builder,
            @Value("${mockapi.base-url}") String baseUrl) {

        this.restClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    public Optional<AccountBalance> findByEmail(String email) {

        try {
            List<MockApiAccountResponse> accounts = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("email", email)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<MockApiAccountResponse>>() {});

            if (accounts == null) {
                throw new BalanceServiceUnavailableException(
                        "A MockAPI retornou uma resposta vazia.", null);
            }

            Optional<MockApiAccountResponse> accountOptional = accounts.stream()
                    .filter(account -> account != null
                            && account.email() != null
                            && account.email().equalsIgnoreCase(email))
                    .findFirst();

            if (accountOptional.isEmpty()) {
                return Optional.empty();
            }

            MockApiAccountResponse account = accountOptional.get();

            if (account.saldo() == null
                    || account.limiteDiario() == null
                    || account.limitePorTransacao() == null) {

                throw new BalanceServiceUnavailableException(
                        "A conta retornada pela MockAPI possui dados incompletos.",
                        null);
            }

            try {
                AccountBalance balance = new AccountBalance(
                        account.saldo(),
                        account.limiteDiario(),
                        account.limitePorTransacao());

                return Optional.of(balance);

            } catch (IllegalArgumentException exception) {
                throw new BalanceServiceUnavailableException(
                        "A MockAPI retornou valores inválidos para saldo ou limites.",
                        exception);
            }

        } catch (RestClientException exception) {
            throw new BalanceServiceUnavailableException(
                    "Não foi possível consultar o serviço de saldo.",
                    exception);
        }
    }
}