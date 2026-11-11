package com.beca.ms2_transactions.application.service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.beca.ms2_transactions.application.port.in.CreateTransactionUseCase;
import com.beca.ms2_transactions.application.port.in.FindTransactionUseCase;
import com.beca.ms2_transactions.application.port.out.TransactionRepositoryPort;
import com.beca.ms2_transactions.application.port.out.UserValidationPort;
import com.beca.ms2_transactions.domain.Transaction;
import com.beca.ms2_transactions.domain.TransactionStatus;
import com.beca.ms2_transactions.domain.TransactionType;

@Service
public class TransactionService implements CreateTransactionUseCase,
        FindTransactionUseCase {

    private final TransactionRepositoryPort transactionRepository;
    private final UserValidationPort userValidationPort;

    public TransactionService(
            TransactionRepositoryPort transactionRepository,
            UserValidationPort userValidationPort) {

        this.transactionRepository = transactionRepository;
        this.userValidationPort = userValidationPort;
    }

    @Override
    public Transaction createTransaction(Transaction transaction) {

        // Valida se o usuário responsável pela transação existe.
        userValidationPort.validateUserExists(transaction.getUserId());

        // Verifica as regras específicas de transferência.
        if (transaction.getType() == TransactionType.TRANSFER) {

            // Rejeita transferência para a própria conta.
            if (transaction.getDestination().equals(transaction.getUserId())) {
                return salvarRejeitada(
                        transaction,
                        "Não é permitido transferir para si mesmo."
                );
            }

            // Valida se o destinatário existe.
            userValidationPort.validateUserExists(
                    transaction.getDestination()
            );
        }

        // Saques, compras e transferências exigem saldo suficiente.
        if (exigeSaldo(transaction.getType())) {

            BigDecimal saldo = getBalance(transaction.getUserId());

            if (saldo.compareTo(transaction.getAmount()) < 0) {
                return salvarRejeitada(
                        transaction,
                        "Saldo insuficiente para realizar a transação."
                );
            }
        }

        // Salva a transação aprovada.
        return transactionRepository.save(transaction);
    }

    private boolean exigeSaldo(TransactionType type) {
        return type == TransactionType.WITHDRAWAL
                || type == TransactionType.PURCHASE
                || type == TransactionType.TRANSFER;
    }

    private Transaction salvarRejeitada(
            Transaction transaction,
            String motivo) {

        Transaction rejeitada = new Transaction(
                transaction.getUserId(),
                transaction.getType(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getDate(),
                transaction.getOrigin(),
                transaction.getDestination(),
                TransactionStatus.REJECTED,
                motivo
        );

        return transactionRepository.save(rejeitada);
    }

    @Override
    public Optional<Transaction> findTransactionById(UUID id) {
        return transactionRepository.findById(id);
    }

    @Override
    public BigDecimal getBalance(UUID userId) {

        BigDecimal saldoEnviadas = transactionRepository.findByUserId(userId)
                .stream()
                .filter(transaction ->
                        transaction.getStatus() == TransactionStatus.APPROVED)
                .map(transaction -> calcularMovimento(transaction))
                .reduce(
                        BigDecimal.ZERO,
                        (total, valor) -> total.add(valor)
                );

        BigDecimal saldoRecebidas = transactionRepository
                .findByDestination(userId)
                .stream()
                .filter(transaction ->
                        transaction.getStatus() == TransactionStatus.APPROVED)
                .filter(transaction ->
                        transaction.getType() == TransactionType.TRANSFER)
                .map(transaction -> transaction.getAmount())
                .reduce(
                        BigDecimal.ZERO,
                        (total, valor) -> total.add(valor)
                );

        return saldoEnviadas.add(saldoRecebidas);
    }

    private BigDecimal calcularMovimento(Transaction transaction) {

        return switch (transaction.getType()) {
            case DEPOSIT -> transaction.getAmount();

            case WITHDRAWAL, PURCHASE, TRANSFER ->
                    transaction.getAmount().negate();
        };
    }
}