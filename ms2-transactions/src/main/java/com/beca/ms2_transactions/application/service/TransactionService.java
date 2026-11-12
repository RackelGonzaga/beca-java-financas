package com.beca.ms2_transactions.application.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.beca.ms2_transactions.application.port.out.UserInfo;
import com.beca.ms2_transactions.domain.AccountBalance;
import com.beca.ms2_transactions.domain.Transaction;
import com.beca.ms2_transactions.domain.TransactionStatus;
import com.beca.ms2_transactions.domain.TransactionType;

@Service
public class TransactionService implements CreateTransactionUseCase, FindTransactionUseCase {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

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

        try {
            UserInfo user = userValidationPort.validateUserExists(transaction.getUserId());

            Optional<AccountBalance> destino = Optional.empty();

            if (transaction.getType() == TransactionType.TRANSFER) {

                if (transaction.getDestination() == null) {
                    return salvarRejeitada(transaction, "Destinatário não informado.");
                }

                if (transaction.getDestination().equals(transaction.getUserId())) {
                    return salvarRejeitada(transaction, "Não é permitido transferir para si mesmo.");
                }

                UserInfo destinationUser;

                try {
                    destinationUser = userValidationPort
                            .validateUserExists(transaction.getDestination());

                } catch (UserNotFoundException exception) {
                    return salvarRejeitada(transaction, "Destinatário não encontrado no MS1.");
                }

                destino = accountBalancePort.findByEmail(destinationUser.email());

                if (destino.isEmpty()) {
                    return salvarRejeitada(
                            transaction,
                            "Conta do destinatário não encontrada na MockAPI.");
                }
            }

            bloquearUsuarios(transaction);

            Optional<AccountBalance> origemEncontrada =
                    accountBalancePort.findByEmail(user.email());

            if (origemEncontrada.isEmpty()) {
                return salvarRejeitada(
                        transaction,
                        "Conta de origem não encontrada na MockAPI.");
            }

            AccountBalance origem = origemEncontrada.orElseThrow();

            LocalDate hoje = LocalDate.now(ZoneId.of("America/Sao_Paulo"));

            if (exigeSaldo(transaction.getType())) {

                BigDecimal totalGastoHoje =
                        transactionRepository.sumApprovedOutflowsByUserAndDate(
                                transaction.getUserId(), hoje);

                Optional<String> motivo = origem.validarSaida(
                        transaction.getAmount(), totalGastoHoje);

                if (motivo.isPresent()) {
                    return salvarRejeitada(transaction, motivo.orElseThrow());
                }
            }

            return atualizarSaldosESalvar(transaction, origem, destino);

        } catch (UserNotFoundException exception) {
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

    private Transaction atualizarSaldosESalvar(
            Transaction transaction,
            AccountBalance origem,
            Optional<AccountBalance> destino) {

        AccountBalance novaOrigem =
                origem.saldoApos(transaction.getType(), transaction.getAmount());

        Optional<AccountBalance> novoDestino = destino.map(conta ->
                conta.saldoApos(TransactionType.DEPOSIT, transaction.getAmount()));

        boolean origemAtualizacaoTentada = false;
        boolean destinoAtualizacaoTentada = false;

        try {
            origemAtualizacaoTentada = true;
            accountBalancePort.updateBalance(origem, novaOrigem.saldo());

            if (destino.isPresent()) {
                AccountBalance contaDestino = destino.orElseThrow();
                AccountBalance saldoDestino = novoDestino.orElseThrow();

                destinoAtualizacaoTentada = true;
                accountBalancePort.updateBalance(contaDestino, saldoDestino.saldo());
            }

            return transactionRepository.save(transaction);

        } catch (RuntimeException exception) {

            if (destinoAtualizacaoTentada) {
                restaurarSaldo(destino.orElseThrow(),
                        destino.orElseThrow().saldo());
            }

            if (origemAtualizacaoTentada) {
                restaurarSaldo(origem, origem.saldo());
            }

            if (exception instanceof BalanceServiceUnavailableException) {
                return salvarRejeitada(
                        transaction,
                        "Não foi possível atualizar o saldo na MockAPI. Tente novamente.");
            }

            throw exception;
        }
    }

    private void bloquearUsuarios(Transaction transaction) {

        UUID origem = transaction.getUserId();
        UUID destino = transaction.getDestination();

        if (transaction.getType() != TransactionType.TRANSFER || destino == null) {
            transactionRepository.lockUserTransactions(origem);
            return;
        }

        // Ordem determinística para reduzir risco de deadlock.
        if (origem.compareTo(destino) < 0) {
            transactionRepository.lockUserTransactions(origem);
            transactionRepository.lockUserTransactions(destino);
        } else {
            transactionRepository.lockUserTransactions(destino);
            transactionRepository.lockUserTransactions(origem);
        }
    }

    private boolean exigeSaldo(TransactionType type) {
        return type == TransactionType.WITHDRAWAL
                || type == TransactionType.PURCHASE
                || type == TransactionType.TRANSFER;
    }

    private void restaurarSaldo(AccountBalance account, BigDecimal saldoAnterior) {
        try {
            accountBalancePort.updateBalance(account, saldoAnterior);

        } catch (RuntimeException exception) {
            log.error(
                    "AJUSTE MANUAL: não foi possível restaurar o saldo da conta {}.",
                    account.accountId(),
                    exception);
        }
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
                userId, startDate, endDate, type);
    }
}