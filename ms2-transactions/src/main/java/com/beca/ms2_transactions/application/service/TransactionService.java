package com.beca.ms2_transactions.application.service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.beca.ms2_transactions.domain.Transaction;
import com.beca.ms2_transactions.domain.TransactionType;

import com.beca.ms2_transactions.application.port.in.CreateTransactionUseCase;
import com.beca.ms2_transactions.application.port.in.FindTransactionUseCase;
import com.beca.ms2_transactions.application.port.in.UpdateTransactionUseCase;
import com.beca.ms2_transactions.application.port.in.DeleteTransactionUseCase;
import com.beca.ms2_transactions.application.port.out.TransactionRepositoryPort;

@Service
public class TransactionService implements CreateTransactionUseCase,
        FindTransactionUseCase, UpdateTransactionUseCase,
        DeleteTransactionUseCase {

    private final TransactionRepositoryPort transactionRepository;

    public TransactionService(TransactionRepositoryPort transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Transaction createTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    @Override
    public Optional<Transaction> findTransactionById(UUID id) {
        return transactionRepository.findById(id);
    }

    @Override
    public Transaction updateTransaction(UUID id, Transaction transaction) {

        Transaction existingTransaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transação não encontrada."));

        existingTransaction.update(
                transaction.getType(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getDate(),
                transaction.getOrigin(),
                transaction.getDestination()
        );

        return transactionRepository.save(existingTransaction);
    }

    @Override
    public void execute(UUID id) {
        transactionRepository.deleteById(id);
    }

    @Override
    public BigDecimal getBalance(UUID userId) {
        return transactionRepository.findByUserId(userId)
                .stream()
                .map(transaction -> {
                    if (transaction.getType() == TransactionType.DEPOSIT) {
                        return transaction.getAmount();
                    }
                    return transaction.getAmount().negate();
                })
                .reduce(BigDecimal.ZERO, (total, valor) -> total.add(valor));
    }
}