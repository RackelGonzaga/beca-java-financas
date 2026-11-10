package com.beca.ms2_transactions.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

public class TransactionTest {

    @Test
    void deveCriarTransacaoComValorPositivo() {
    Transaction transaction = new Transaction(
            UUID.randomUUID(),
            TransactionType.DEPOSIT,
            "Salário",
            new BigDecimal("2500.00"),
            LocalDate.now(),
            null,
            null
        );

        assertNotNull(transaction);
        assertEquals(new BigDecimal("2500.00"), transaction.getAmount());
        assertEquals(TransactionType.DEPOSIT, transaction.getType());
    }

    @Test
    void naoDevePermitirValorZero() {
        assertThrows(IllegalArgumentException.class, () ->
            new Transaction(
                    UUID.randomUUID(),
                    TransactionType.DEPOSIT,
                    "Salário",
                    BigDecimal.ZERO,
                    LocalDate.now(),
                    null,
                    null
            )
        );
    }

    @Test
    void naoDevePermitirValorNegativo() {
        assertThrows(IllegalArgumentException.class, () ->
            new Transaction(
                UUID.randomUUID(),
                TransactionType.WITHDRAWAL,
                "Compra",
                new BigDecimal("-10.00"),
                LocalDate.now(),
                null,
                null
            )
        );
    }

    @Test
    void naoDevePermitirValorNulo() {
        assertThrows(IllegalArgumentException.class, () ->
            new Transaction(
                UUID.randomUUID(),
                TransactionType.DEPOSIT,
                "Salario",
                null,
                LocalDate.now(),
                null,
                null
            )
        );
    }

    @Test
    void deveAtualizarTransacaoComValorPositivo() {
        Transaction transaction = criarTransacao();
            transaction.update(
            TransactionType.PURCHASE,
            "Mercado",
            new BigDecimal("150.00"),
            LocalDate.now(),
            null,
            null
        );
        assertEquals(TransactionType.PURCHASE, transaction.getType());
        assertEquals("Mercado", transaction.getDescription());
        assertEquals(new BigDecimal("150.00"), transaction.getAmount());
    }

    @Test
    void naoDeveAtualizarTransacaoComValorZero() {
        Transaction transaction = criarTransacao();
        assertThrows(IllegalArgumentException.class, () ->
        transaction.update(
            TransactionType.PURCHASE,
            "Mercado",
            BigDecimal.ZERO,
            LocalDate.now(),
            null,
            null
            )
        );
        assertEquals(new BigDecimal("100.00"), transaction.getAmount());
    }

    @Test
    void naoDeveAtualizarTransacaoComValorNegativo() {
        Transaction transaction = criarTransacao();
        assertThrows(IllegalArgumentException.class, () ->
        transaction.update(
            TransactionType.PURCHASE,
            "Mercado",
            new BigDecimal("-50.00"),
            LocalDate.now(),
            null,
            null
        )
        );
        assertEquals(new BigDecimal("100.00"), transaction.getAmount());
    }

    private Transaction criarTransacao() {
        return new Transaction(
            UUID.randomUUID(),
            TransactionType.DEPOSIT,
            "Salario",
            new BigDecimal("100.00"),
            LocalDate.now(),
            null,
            null
        );
    }
}