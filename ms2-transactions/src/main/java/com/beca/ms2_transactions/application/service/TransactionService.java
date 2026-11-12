package com.beca.ms2_transactions.application.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.beca.ms2_transactions.application.exception.BalanceServiceUnavailableException;
import com.beca.ms2_transactions.application.exception.UserNotFoundException;
import com.beca.ms2_transactions.application.exception.UserServiceUnavailableException;
import com.beca.ms2_transactions.application.port.in.CreateTransactionUseCase;
import com.beca.ms2_transactions.application.port.in.FindTransactionUseCase;
import com.beca.ms2_transactions.application.port.out.AccountBalancePort;
import com.beca.ms2_transactions.application.port.out.TransactionRepositoryPort;
import com.beca.ms2_transactions.application.port.out.UserValidationPort;
import com.beca.ms2_transactions.domain.AccountBalance;
import com.beca.ms2_transactions.domain.Transaction;
import com.beca.ms2_transactions.domain.TransactionStatus;
import com.beca.ms2_transactions.domain.TransactionType;

@Service
public class TransactionService implements CreateTransactionUseCase, FindTransactionUseCase {

    private final TransactionRepositoryPort transactionRepository;
    private final UserValidationPort userValidationPort;
    private final AccountBalancePort accountBalancePort;

    public TransactionService(
            TransactionRepositoryPort transactionRepository,
            UserValidationPort userValidationPort,
            AccountBalancePort accountBalancePort) {

        this.transactionRepository = transactionRepository;
        this.userValidationPort = userValidationPort;
        this.accountBalancePort = accountBalancePort;
    }

    @Override
    @Transactional
    public Transaction createTransaction(Transaction transaction) {

        // Valida o usuário responsável pela transação.
        try {
            var user = userValidationPort.validateUserExists(transaction.getUserId());

            // Valida o destinatário de transferências.
            if (transaction.getType() == TransactionType.TRANSFER) {

                if (transaction.getDestination() == null) {
                    return salvarRejeitada(
                            transaction,
                            "Destinatário não informado.");
                }

                if (transaction.getDestination().equals(transaction.getUserId())) {
                    return salvarRejeitada(
                            transaction,
                            "Não é permitido transferir para si mesmo.");
                }

                try {
                    userValidationPort.validateUserExists(transaction.getDestination());

                } catch (UserNotFoundException exception) {
                    return salvarRejeitada(
                            transaction,
                            "Destinatário não encontrado no MS1.");

                } catch (UserServiceUnavailableException exception) {
                    return salvarRejeitada(
                            transaction,
                            "Serviço de usuários indisponível, tente novamente mais tarde.");
                }
            }

            // Depósitos não precisam consultar saldo ou limites.
            if (!exigeSaldo(transaction.getType())) {
                return transactionRepository.save(transaction);
            }

            // Serializa operações de saída do mesmo usuário.
            transactionRepository.lockUserTransactions(transaction.getUserId());

            Optional<AccountBalance> accountOptional =
                    accountBalancePort.findByEmail(user.email());

            if (accountOptional.isEmpty()) {
                return salvarRejeitada(
                        transaction,
                        "Conta não encontrada no serviço de saldo.");
            }

            LocalDate hoje = LocalDate.now(ZoneId.of("America/Sao_Paulo"));

            BigDecimal totalGastoHoje =
                    transactionRepository.sumApprovedOutflowsByUserAndDate(
                            transaction.getUserId(),
                            hoje);

            Optional<String> motivo = accountOptional.get().validarSaida(
                    transaction.getAmount(),
                    totalGastoHoje);

            if (motivo.isPresent()) {
                return salvarRejeitada(transaction, motivo.get());
            }

            return transactionRepository.save(transaction);

        } catch (UserNotFoundException exception) {
            // O usuário responsável não existe.
            // Não registrar uma transação para um usuário inválido.
            throw exception;

        } catch (UserServiceUnavailableException exception) {
            return salvarRejeitada(
                    transaction,
                    "Serviço de usuários indisponível, tente novamente mais tarde.");

        } catch (BalanceServiceUnavailableException exception) {
            return salvarRejeitada(
                    transaction,
                    "Serviço de saldo indisponível, tente novamente mais tarde.");
        }
    }

    private boolean exigeSaldo(TransactionType type) {
        return type == TransactionType.WITHDRAWAL
                || type == TransactionType.PURCHASE
                || type == TransactionType.TRANSFER;
    }

    private Transaction salvarRejeitada(Transaction transaction, String motivo) {

        Transaction rejeitada = new Transaction(
                transaction.getUserId(),
                transaction.getType(),
                transaction.getCategory(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getDate(),
                transaction.getOrigin(),
                transaction.getDestination(),
                TransactionStatus.REJECTED,
                motivo);

        return transactionRepository.save(rejeitada);
    }

    @Override
    public Optional<Transaction> findTransactionById(UUID id) {
        return transactionRepository.findById(id);
    }

    @Override
    public Optional<AccountBalance> getBalance(UUID userId) {

        var user = userValidationPort.validateUserExists(userId);

        return accountBalancePort.findByEmail(user.email());
    }

    @Override
    public List<Transaction> findTransactions(
            UUID userId,
            LocalDate startDate,
            LocalDate endDate,
            TransactionType type) {

        return transactionRepository.findByFilters(
                userId,
                startDate,
                endDate,
                type);
    }
}