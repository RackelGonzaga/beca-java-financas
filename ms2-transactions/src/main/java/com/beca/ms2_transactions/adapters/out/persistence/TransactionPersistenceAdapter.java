package com.beca.ms2_transactions.adapters.out.persistence;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;

import com.beca.ms2_transactions.application.port.out.TransactionRepositoryPort;
import com.beca.ms2_transactions.domain.Transaction;
import com.beca.ms2_transactions.domain.TransactionStatus;
import com.beca.ms2_transactions.domain.TransactionType;

@Repository
public class TransactionPersistenceAdapter implements TransactionRepositoryPort {

    private final TransactionRepository transactionRepository;
    private final EntityManager entityManager;

    public TransactionPersistenceAdapter(
            TransactionRepository transactionRepository,
            EntityManager entityManager) {

        this.transactionRepository = transactionRepository;
        this.entityManager = entityManager;
    }

    private TransactionEntity toEntity(Transaction transaction) {
        return new TransactionEntity(
                transaction.getUserId(),
                transaction.getType(),
                transaction.getCategory(),
                transaction.getStatus(),
                transaction.getRejectionReason(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getDate(),
                transaction.getOrigin(),
                transaction.getDestination());
    }

    private Transaction toDomain(TransactionEntity entity) {
        return new Transaction(
                entity.getId(),
                entity.getUserId(),
                entity.getType(),
                entity.getCategory(),
                entity.getDescription(),
                entity.getAmount(),
                entity.getDate(),
                entity.getOrigin(),
                entity.getDestination(),
                entity.getCreatedAt(),
                entity.getStatus(),
                entity.getRejectionReason());
    }

    @Override
    public Transaction save(Transaction transaction) {
        TransactionEntity entity = toEntity(transaction);

        TransactionEntity savedEntity = transactionRepository.save(entity);

        return toDomain(savedEntity);
    }

    @Override
    public List<Transaction> findByFilters(
            UUID userId,
            LocalDate startDate,
            LocalDate endDate,
            TransactionType type) {

        return transactionRepository.findByFilters(
                userId, startDate, endDate, type).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public BigDecimal sumApprovedOutflowsByUserAndDate(
            UUID userId,
            LocalDate date) {

        BigDecimal total = transactionRepository.sumApprovedOutflowsByUserAndDate(
                userId,
                date,
                TransactionStatus.APPROVED,
                List.of(
                        TransactionType.WITHDRAWAL,
                        TransactionType.PURCHASE,
                        TransactionType.TRANSFER));

        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public Optional<Transaction> findById(UUID id) {
        return transactionRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public void lockUserTransactions(UUID userId) {
        entityManager.createNativeQuery("""
                WITH lock_result AS (
                    SELECT pg_advisory_xact_lock(
                        hashtextextended(CAST(:userId AS text), 0)
                    )
                )
                SELECT 1 FROM lock_result
                """)
                .setParameter("userId", userId.toString())
                .getSingleResult();
    }
}