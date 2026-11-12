package com.beca.ms2_transactions.adapters.out.mockapi;

import java.math.BigDecimal;
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

            return accounts.stream()
                    .filter(account -> account != null
                            && account.email() != null
                            && account.email().equalsIgnoreCase(email))
                    .findFirst()
                    .map(this::toDomain);

        } catch (RestClientException exception) {
            throw new BalanceServiceUnavailableException(
                    "Não foi possível consultar o serviço de saldo.",
                    exception);
        }
    }

    @Override
    public void updateBalance(AccountBalance account, BigDecimal novoSaldo) {
        if (account == null || account.accountId() == null
                || novoSaldo == null
                || novoSaldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Dados inválidos para atualizar o saldo.");
        }

        try {
            MockApiAccountResponse current = restClient.get()
                    .uri("/{id}", account.accountId())
                    .retrieve()
                    .body(MockApiAccountResponse.class);

            if (current == null || !account.accountId().equals(current.id())) {
                throw new BalanceServiceUnavailableException(
                        "Não foi possível localizar a conta para atualização.", null);
            }

            MockApiAccountResponse updated = current.withSaldo(novoSaldo);

            restClient.put()
                    .uri("/{id}", account.accountId())
                    .body(updated)
                    .retrieve()
                    .toBodilessEntity();

        } catch (RestClientException exception) {
            throw new BalanceServiceUnavailableException(
                    "Não foi possível atualizar o saldo na MockAPI.",
                    exception);
        }
    }

    private AccountBalance toDomain(MockApiAccountResponse account) {
        if (account.id() == null
                || account.saldo() == null
                || account.limiteDiario() == null
                || account.limitePorTransacao() == null) {
            throw new BalanceServiceUnavailableException(
                    "A conta retornada pela MockAPI possui dados incompletos.",
                    null);
        }

        try {
            return new AccountBalance(
                    account.id(),
                    account.saldo(),
                    account.limiteDiario(),
                    account.limitePorTransacao());

        } catch (IllegalArgumentException exception) {
            throw new BalanceServiceUnavailableException(
                    "A MockAPI retornou valores inválidos para saldo ou limites.",
                    exception);
        }
    }
}