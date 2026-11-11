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
        assertEquals(TransactionStatus.APPROVED, transaction.getStatus());
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
                "Salário",
                null,
                LocalDate.now(),
                null,
                null
            )
        );
    }

    @Test
    void deveCriarTransacaoRejeitadaComMotivo() {
        Transaction transaction = new Transaction(
            UUID.randomUUID(),
            TransactionType.WITHDRAWAL,
            "Compra",
            new BigDecimal("100.00"),
            LocalDate.now(),
            null,
            null,
            TransactionStatus.REJECTED,
            "Saldo insuficiente"
        );

        assertNotNull(transaction);
        assertEquals(TransactionStatus.REJECTED, transaction.getStatus());
        assertEquals("Saldo insuficiente", transaction.getRejectionReason());
    }

    @Test
    void naoDevePermitirTransacaoRejeitadaSemMotivo() {
        assertThrows(IllegalArgumentException.class, () ->
            new Transaction(
                UUID.randomUUID(),
                TransactionType.WITHDRAWAL,
                "Compra",
                new BigDecimal("100.00"),
                LocalDate.now(),
                null,
                null,
                TransactionStatus.REJECTED,
                null
            )
        );
    }

    @Test
    void naoDevePermitirTransacaoAprovadaComMotivoDeRejeicao() {
        assertThrows(IllegalArgumentException.class, () ->
            new Transaction(
                UUID.randomUUID(),
                TransactionType.DEPOSIT,
                "Salário",
                new BigDecimal("2500.00"),
                LocalDate.now(),
                null,
                null,
                TransactionStatus.APPROVED,
                "Saldo insuficiente"
            )
        );
    }

    @Test
    void naoDevePermitirStatusNulo() {
        assertThrows(IllegalArgumentException.class, () ->
            new Transaction(
                UUID.randomUUID(),
                TransactionType.DEPOSIT,
                "Salário",
                new BigDecimal("2500.00"),
                LocalDate.now(),
                null,
                null,
                null,
                null
            )
        );
    }

    @Test
    void naoDevePermitirTransferenciaAprovadaSemDestino() {
        assertThrows(IllegalArgumentException.class, () ->
            new Transaction(
                UUID.randomUUID(),
                TransactionType.TRANSFER,
                "Transferência",
                new BigDecimal("100.00"),
                LocalDate.now(),
                null,
                null,
                TransactionStatus.APPROVED,
                null
            )
        );
    }

    @Test
    void devePermitirRepresentarTransferenciaParaSiMesmo() {
        UUID userId = UUID.randomUUID();

        Transaction transaction = new Transaction(
            userId,
            TransactionType.TRANSFER,
            "Transferência",
            new BigDecimal("100.00"),
            LocalDate.now(),
            null,
            userId,
            TransactionStatus.APPROVED,
            null
        );

        assertNotNull(transaction);
        assertEquals(userId, transaction.getUserId());
        assertEquals(userId, transaction.getDestination());
    }

    @Test
    void devePermitirRepresentarTransferenciaRejeitada() {
        Transaction transaction = new Transaction(
            UUID.randomUUID(),
            TransactionType.TRANSFER,
            "Transferência",
            new BigDecimal("100.00"),
            LocalDate.now(),
            null,
            null,
            TransactionStatus.REJECTED,
            "Destinatário inválido"
        );

        assertNotNull(transaction);
        assertEquals(TransactionStatus.REJECTED, transaction.getStatus());
        assertEquals("Destinatário inválido", transaction.getRejectionReason());
    }
}