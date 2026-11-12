package com.beca.ms2_transactions.domain;


import java.math.BigDecimal;
import java.util.Optional;

public record AccountBalance(
        String accountId,
        BigDecimal saldo,
        BigDecimal limiteDiario,
        BigDecimal limitePorTransacao) {

    public AccountBalance {
        if (saldo == null || limiteDiario == null || limitePorTransacao == null) {
            throw new IllegalArgumentException(
                    "Os dados de saldo e limites são obrigatórios.");
        }

        if (saldo.compareTo(BigDecimal.ZERO) < 0
                || limiteDiario.compareTo(BigDecimal.ZERO) < 0
                || limitePorTransacao.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Saldo e limites não podem ser negativos.");
        }
    }

    // Compatibilidade com os testes e usos anteriores.
    public AccountBalance(
            BigDecimal saldo,
            BigDecimal limiteDiario,
            BigDecimal limitePorTransacao) {
        this(null, saldo, limiteDiario, limitePorTransacao);
    }

    public Optional<String> validarSaida(
            BigDecimal valor,
            BigDecimal totalGastoHoje) {

        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.of("O valor da transação deve ser maior que zero.");
        }

        if (totalGastoHoje == null
                || totalGastoHoje.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("O total gasto hoje é inválido.");
        }

        if (valor.compareTo(saldo) > 0) {
            return Optional.of("Saldo insuficiente");
        }

        if (valor.compareTo(limitePorTransacao) > 0) {
            return Optional.of("Valor acima do limite por transação");
        }

        if (totalGastoHoje.add(valor).compareTo(limiteDiario) > 0) {
            return Optional.of("Limite diário excedido");
        }

        return Optional.empty();
    }

    public AccountBalance saldoApos(
            TransactionType tipo,
            BigDecimal valor) {

        if (tipo == null || valor == null
                || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Tipo e valor da operação são obrigatórios.");
        }

        BigDecimal novoSaldo = switch (tipo) {
            case DEPOSIT -> saldo.add(valor);
            case WITHDRAWAL, PURCHASE, TRANSFER -> saldo.subtract(valor);
        };

        if (novoSaldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("O saldo não pode ficar negativo.");
        }

        return new AccountBalance(
                accountId, novoSaldo, limiteDiario, limitePorTransacao);
    }
}