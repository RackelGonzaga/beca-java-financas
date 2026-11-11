package com.beca.ms2_transactions.application.service;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.beca.ms2_transactions.application.exception.UserNotFoundException;
import com.beca.ms2_transactions.application.port.out.TransactionRepositoryPort;
import com.beca.ms2_transactions.application.port.out.UserValidationPort;
import com.beca.ms2_transactions.domain.Transaction;
import com.beca.ms2_transactions.domain.TransactionStatus;
import com.beca.ms2_transactions.domain.TransactionType;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepositoryPort transactionRepository;

    @Mock
    private UserValidationPort userValidationPort;

    private TransactionService transactionService;

    private final UUID userId = UUID.randomUUID();
    private final UUID destinationId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(
                transactionRepository,
                userValidationPort
        );
    }

    @Test
    void deveCriarTransacaoQuandoUsuarioExiste() {

        Transaction transaction = criarTransacao(
                TransactionType.DEPOSIT,
                userId,
                null,
                "100.00"
        );

        when(transactionRepository.save(transaction))
                .thenReturn(transaction);

        Transaction result = transactionService.createTransaction(transaction);

        assertSame(transaction, result);
        verify(userValidationPort).validateUserExists(userId);
        verify(transactionRepository).save(transaction);
    }

    @Test
    void naoDeveCriarTransacaoQuandoUsuarioNaoExiste() {

        Transaction transaction = criarTransacao(
                TransactionType.DEPOSIT,
                userId,
                null,
                "100.00"
        );

        org.mockito.Mockito.doThrow(
                new UserNotFoundException("Usuário não encontrado")
        ).when(userValidationPort).validateUserExists(userId);

        assertThrows(
                UserNotFoundException.class,
                () -> transactionService.createTransaction(transaction)
        );

        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void deveValidarRemetenteEDestinatarioNaTransferencia() {

        Transaction transaction = criarTransacao(
                TransactionType.TRANSFER,
                userId,
                destinationId,
                "50.00"
        );

        Transaction deposito = criarTransacao(
                TransactionType.DEPOSIT,
                userId,
                null,
                "100.00"
        );

        when(transactionRepository.findByUserId(userId))
                .thenReturn(List.of(deposito));

        when(transactionRepository.findByDestination(userId))
                .thenReturn(List.of());

        when(transactionRepository.save(transaction))
                .thenReturn(transaction);

        Transaction result = transactionService.createTransaction(transaction);

        assertSame(transaction, result);
        assertEquals(TransactionStatus.APPROVED, result.getStatus());

        verify(userValidationPort).validateUserExists(userId);
        verify(userValidationPort).validateUserExists(destinationId);
        verify(transactionRepository).save(transaction);
    }

    @Test
    void deveRejeitarTransferenciaParaSiMesmo() {

        Transaction transaction = criarTransacao(
                TransactionType.TRANSFER,
                userId,
                userId,
                "50.00"
        );

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = transactionService.createTransaction(transaction);

        assertEquals(TransactionStatus.REJECTED, result.getStatus());
        assertEquals(
                "Não é permitido transferir para si mesmo.",
                result.getRejectionReason()
        );

        verify(userValidationPort).validateUserExists(userId);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void deveRejeitarSaqueQuandoNaoExisteSaldoSuficiente() {

        Transaction transaction = criarTransacao(
                TransactionType.WITHDRAWAL,
                userId,
                null,
                "100.00"
        );

        when(transactionRepository.findByUserId(userId))
                .thenReturn(List.of());

        when(transactionRepository.findByDestination(userId))
                .thenReturn(List.of());

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = transactionService.createTransaction(transaction);

        assertEquals(TransactionStatus.REJECTED, result.getStatus());
        assertEquals(
                "Saldo insuficiente para realizar a transação.",
                result.getRejectionReason()
        );

        verify(userValidationPort).validateUserExists(userId);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void deveBuscarTransacaoPorId() {

        UUID transactionId = UUID.randomUUID();

        Transaction transaction = criarTransacao(
                TransactionType.DEPOSIT,
                userId,
                null,
                "100.00"
        );

        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.of(transaction));

        Optional<Transaction> result =
                transactionService.findTransactionById(transactionId);

        assertEquals(Optional.of(transaction), result);
        verify(transactionRepository).findById(transactionId);
    }

    @Test
    void deveCalcularSaldoComEntradasESaidasAprovadas() {

        Transaction deposito = criarTransacao(
                TransactionType.DEPOSIT, userId, null, "500.00"
        );

        Transaction saque = criarTransacao(
                TransactionType.WITHDRAWAL, userId, null, "100.00"
        );

        Transaction compra = criarTransacao(
                TransactionType.PURCHASE, userId, null, "50.00"
        );

        Transaction transferenciaEnviada = criarTransacao(
                TransactionType.TRANSFER, userId, destinationId, "30.00"
        );

        Transaction transferenciaRecebida = criarTransacao(
                TransactionType.TRANSFER, destinationId, userId, "80.00"
        );

        when(transactionRepository.findByUserId(userId))
                .thenReturn(List.of(
                        deposito,
                        saque,
                        compra,
                        transferenciaEnviada
                ));

        when(transactionRepository.findByDestination(userId))
                .thenReturn(List.of(transferenciaRecebida));

        BigDecimal saldo = transactionService.getBalance(userId);

        assertEquals(new BigDecimal("400.00"), saldo);
    }

    @Test
    void naoDeveConsiderarTransacoesRejeitadasNoSaldo() {

        Transaction deposito = criarTransacao(
                TransactionType.DEPOSIT, userId, null, "200.00"
        );

        Transaction rejeitada = new Transaction(
                userId,
                TransactionType.WITHDRAWAL,
                "Saque rejeitado",
                new BigDecimal("100.00"),
                LocalDate.now(),
                null,
                null,
                TransactionStatus.REJECTED,
                "Saldo insuficiente"
        );

        when(transactionRepository.findByUserId(userId))
                .thenReturn(List.of(deposito, rejeitada));

        when(transactionRepository.findByDestination(userId))
                .thenReturn(List.of());

        BigDecimal saldo = transactionService.getBalance(userId);

        assertEquals(new BigDecimal("200.00"), saldo);
    }

    private Transaction criarTransacao(
            TransactionType type,
            UUID transactionUserId,
            UUID destination,
            String amount) {

        return new Transaction(
                transactionUserId,
                type,
                "Transação de teste",
                new BigDecimal(amount),
                LocalDate.now(),
                null,
                destination
        );
    }
}