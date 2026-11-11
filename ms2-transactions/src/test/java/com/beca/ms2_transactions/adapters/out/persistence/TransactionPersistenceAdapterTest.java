package com.beca.ms2_transactions.adapters.out.persistence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.beca.ms2_transactions.domain.Transaction;
import com.beca.ms2_transactions.domain.TransactionStatus;
import com.beca.ms2_transactions.domain.TransactionType;

@ExtendWith(MockitoExtension.class)
class TransactionPersistenceAdapterTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionPersistenceAdapter adapter;

    private UUID userId;
    private LocalDate date;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        date = LocalDate.now();
    }

    @Test
    void deveSalvarTransacaoAprovada() {
        Transaction transaction = new Transaction(
                userId,
                TransactionType.DEPOSIT,
                "Depósito inicial",
                new BigDecimal("100.00"),
                date,
                "PIX",
                null
        );

        when(transactionRepository.save(any(TransactionEntity.class)))
                .thenAnswer(invocation -> {
                    TransactionEntity original = invocation.getArgument(0);
                    return criarEntitySalva(original);
                });

        Transaction saved = adapter.save(transaction);

        assertNotNull(saved.getId());
        assertEquals(userId, saved.getUserId());
        assertEquals(TransactionType.DEPOSIT, saved.getType());
        assertEquals(TransactionStatus.APPROVED, saved.getStatus());
        assertEquals(new BigDecimal("100.00"), saved.getAmount());
        assertNotNull(saved.getCreatedAt());

        verify(transactionRepository).save(any(TransactionEntity.class));
    }

    @Test
    void deveSalvarTransacaoRejeitadaComMotivo() {
        Transaction transaction = new Transaction(
                userId,
                TransactionType.WITHDRAWAL,
                "Saque",
                new BigDecimal("500.00"),
                date,
                null,
                null,
                TransactionStatus.REJECTED,
                "Saldo insuficiente"
        );

        when(transactionRepository.save(any(TransactionEntity.class)))
                .thenAnswer(invocation -> {
                    TransactionEntity original = invocation.getArgument(0);
                    return criarEntitySalva(original);
                });

        Transaction saved = adapter.save(transaction);

        assertNotNull(saved.getId());
        assertEquals(TransactionStatus.REJECTED, saved.getStatus());
        assertEquals("Saldo insuficiente", saved.getRejectionReason());
        assertEquals(new BigDecimal("500.00"), saved.getAmount());
        assertNotNull(saved.getCreatedAt());

        verify(transactionRepository).save(any(TransactionEntity.class));
    }

    @Test
    void deveBuscarTransacoesPorUsuario() {
        TransactionEntity entity = criarEntity(
                UUID.randomUUID(),
                TransactionStatus.APPROVED,
                null
        );

        when(transactionRepository.findByUserId(userId))
                .thenReturn(List.of(entity));

        List<Transaction> transactions = adapter.findByUserId(userId);

        assertEquals(1, transactions.size());
        assertEquals(userId, transactions.get(0).getUserId());
        assertEquals(TransactionStatus.APPROVED,
                transactions.get(0).getStatus());

        verify(transactionRepository).findByUserId(userId);
    }

    @Test
    void deveBuscarTransacaoPorId() {
        UUID transactionId = UUID.randomUUID();

        TransactionEntity entity = criarEntity(
                transactionId,
                TransactionStatus.REJECTED,
                "Saldo insuficiente"
        );

        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.of(entity));

        Optional<Transaction> result = adapter.findById(transactionId);

        assertTrue(result.isPresent());
        assertEquals(transactionId, result.get().getId());
        assertEquals(TransactionStatus.REJECTED,
                result.get().getStatus());
        assertEquals("Saldo insuficiente",
                result.get().getRejectionReason());

        verify(transactionRepository).findById(transactionId);
    }

    @Test
    void deveRetornarVazioQuandoTransacaoNaoExistir() {
        UUID transactionId = UUID.randomUUID();

        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.empty());

        Optional<Transaction> result = adapter.findById(transactionId);

        assertTrue(result.isEmpty());

        verify(transactionRepository).findById(transactionId);
    }

    private TransactionEntity criarEntity(
            UUID id,
            TransactionStatus status,
            String rejectionReason) {

        TransactionEntity entity = mock(TransactionEntity.class);

        when(entity.getId()).thenReturn(id);
        when(entity.getUserId()).thenReturn(userId);
        when(entity.getType()).thenReturn(TransactionType.DEPOSIT);
        when(entity.getStatus()).thenReturn(status);
        when(entity.getRejectionReason()).thenReturn(rejectionReason);
        when(entity.getDescription()).thenReturn("Depósito");
        when(entity.getAmount()).thenReturn(new BigDecimal("100.00"));
        when(entity.getDate()).thenReturn(date);
        when(entity.getOrigin()).thenReturn("PIX");
        when(entity.getDestination()).thenReturn(null);
        when(entity.getCreatedAt()).thenReturn(LocalDateTime.now());

        return entity;
    }

    private TransactionEntity criarEntitySalva(
            TransactionEntity original) {

        UUID id = UUID.randomUUID();

        TransactionEntity entity = criarEntity(
                id,
                original.getStatus(),
                original.getRejectionReason()
        );

        when(entity.getDescription()).thenReturn(original.getDescription());
        when(entity.getAmount()).thenReturn(original.getAmount());
        when(entity.getDate()).thenReturn(original.getDate());
        when(entity.getOrigin()).thenReturn(original.getOrigin());
        when(entity.getDestination()).thenReturn(original.getDestination());

        return entity;
    }
}