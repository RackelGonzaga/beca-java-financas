package com.beca.ms2_transactions.adapters.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.beca.ms2_transactions.application.port.out.TransactionRepositoryPort;
import com.beca.ms2_transactions.domain.Transaction;

@Repository
public class TransactionPersistenceAdapter
        implements TransactionRepositoryPort {

    private final TransactionRepository transactionRepository;

    public TransactionPersistenceAdapter(
            TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    private TransactionEntity toEntity(Transaction transaction) {
        return new TransactionEntity(
                transaction.getUserId(),
                transaction.getType(),
                transaction.getStatus(),
                transaction.getRejectionReason(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getDate(),
                transaction.getOrigin(),
                transaction.getDestination()
        );
    }

    private Transaction toDomain(TransactionEntity entity) {
        return new Transaction(
                entity.getId(),
                entity.getUserId(),
                entity.getType(),
                entity.getDescription(),
                entity.getAmount(),
                entity.getDate(),
                entity.getOrigin(),
                entity.getDestination(),
                entity.getCreatedAt(),
                entity.getStatus(),
                entity.getRejectionReason()
        );
    }

    @Override
    public Transaction save(Transaction transaction) {
        TransactionEntity entity = toEntity(transaction);

        TransactionEntity savedEntity =
                transactionRepository.save(entity);

        return toDomain(savedEntity);
    }

    @Override
    public List<Transaction> findByUserId(UUID userId) {
        return transactionRepository.findByUserId(userId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Transaction> findByDestination(UUID destination) {
        return transactionRepository.findByDestination(destination)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Transaction> findById(UUID id) {
        return transactionRepository.findById(id)
                .map(this::toDomain);
    }
}