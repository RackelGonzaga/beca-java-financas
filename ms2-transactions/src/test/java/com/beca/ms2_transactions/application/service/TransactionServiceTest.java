package com.beca.ms2_transactions.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.beca.ms2_transactions.application.exception.BalanceServiceUnavailableException;
import com.beca.ms2_transactions.application.exception.UserNotFoundException;
import com.beca.ms2_transactions.application.port.out.AccountBalancePort;
import com.beca.ms2_transactions.application.port.out.TransactionRepositoryPort;
import com.beca.ms2_transactions.application.port.out.UserInfo;
import com.beca.ms2_transactions.application.port.out.UserValidationPort;
import com.beca.ms2_transactions.domain.AccountBalance;
import com.beca.ms2_transactions.domain.Category;
import com.beca.ms2_transactions.domain.Transaction;
import com.beca.ms2_transactions.domain.TransactionStatus;
import com.beca.ms2_transactions.domain.TransactionType;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepositoryPort transactionRepository;

    @Mock
    private UserValidationPort userValidationPort;

    @Mock
    private AccountBalancePort accountBalancePort;

    private TransactionService transactionService;

    private final UUID userId = UUID.randomUUID();
    private final UUID destinationId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(
                transactionRepository,
                userValidationPort,
                accountBalancePort);
    }

    @Test
    void deveCriarDepositoQuandoUsuarioExiste() {

        Transaction transaction = criarTransacao(
                TransactionType.DEPOSIT, userId, null, "100.00");

        when(userValidationPort.validateUserExists(userId))
                .thenReturn(criarUsuario(userId));

        when(transactionRepository.save(transaction))
                .thenReturn(transaction);

        Transaction result = transactionService.createTransaction(transaction);

        assertSame(transaction, result);

        verify(userValidationPort).validateUserExists(userId);
        verify(transactionRepository).save(transaction);
        verify(transactionRepository, never()).lockUserTransactions(userId);
        verify(accountBalancePort, never()).findByEmail(any());
    }

    @Test
    void naoDeveCriarTransacaoQuandoUsuarioNaoExiste() {

        Transaction transaction = criarTransacao(
                TransactionType.DEPOSIT, userId, null, "100.00");

        org.mockito.Mockito.doThrow(
                new UserNotFoundException("Usuário não encontrado"))
                .when(userValidationPort).validateUserExists(userId);

        assertThrows(
                UserNotFoundException.class,
                () -> transactionService.createTransaction(transaction));

        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(transactionRepository, never()).lockUserTransactions(userId);
    }

    @Test
    void deveValidarRemetenteEDestinatarioNaTransferencia() {

        Transaction transaction = criarTransacao(
                TransactionType.TRANSFER, userId, destinationId, "50.00");

        when(userValidationPort.validateUserExists(userId))
                .thenReturn(criarUsuario(userId));

        when(userValidationPort.validateUserExists(destinationId))
                .thenReturn(criarUsuario(destinationId));

        when(accountBalancePort.findByEmail("usuario@email.com"))
                .thenReturn(Optional.of(contaComSaldo()));

        when(transactionRepository.sumApprovedOutflowsByUserAndDate(
                any(UUID.class), any(LocalDate.class)))
                .thenReturn(BigDecimal.ZERO);

        when(transactionRepository.save(transaction))
                .thenReturn(transaction);

        Transaction result = transactionService.createTransaction(transaction);

        assertSame(transaction, result);

        verify(userValidationPort).validateUserExists(userId);
        verify(userValidationPort).validateUserExists(destinationId);
        verify(transactionRepository).lockUserTransactions(userId);
        verify(transactionRepository).save(transaction);
    }

    @Test
    void deveRejeitarTransferenciaParaSiMesmo() {

        Transaction transaction = criarTransacao(
                TransactionType.TRANSFER, userId, userId, "50.00");

        when(userValidationPort.validateUserExists(userId))
                .thenReturn(criarUsuario(userId));

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = transactionService.createTransaction(transaction);

        assertEquals(TransactionStatus.REJECTED, result.getStatus());
        assertEquals(
                "Não é permitido transferir para si mesmo.",
                result.getRejectionReason());

        verify(userValidationPort).validateUserExists(userId);
        verify(userValidationPort, never()).validateUserExists(destinationId);
        verify(transactionRepository, never()).lockUserTransactions(userId);
    }

    @Test
    void deveRejeitarSaqueQuandoNaoExisteSaldoSuficiente() {

        Transaction transaction = criarTransacao(
                TransactionType.WITHDRAWAL, userId, null, "100.00");

        when(userValidationPort.validateUserExists(userId))
                .thenReturn(criarUsuario(userId));

        when(accountBalancePort.findByEmail("usuario@email.com"))
                .thenReturn(Optional.of(
                        new AccountBalance(
                                BigDecimal.ZERO,
                                new BigDecimal("1500.00"),
                                new BigDecimal("800.00"))));

        when(transactionRepository.sumApprovedOutflowsByUserAndDate(
                any(UUID.class), any(LocalDate.class)))
                .thenReturn(BigDecimal.ZERO);

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = transactionService.createTransaction(transaction);

        assertEquals(TransactionStatus.REJECTED, result.getStatus());
        assertNotNull(result.getRejectionReason());

        verify(transactionRepository).lockUserTransactions(userId);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void deveRejeitarSaqueQuandoContaNaoExiste() {

        Transaction transaction = criarTransacao(
                TransactionType.WITHDRAWAL, userId, null, "100.00");

        when(userValidationPort.validateUserExists(userId))
                .thenReturn(criarUsuario(userId));

        when(accountBalancePort.findByEmail("usuario@email.com"))
                .thenReturn(Optional.empty());

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = transactionService.createTransaction(transaction);

        assertEquals(TransactionStatus.REJECTED, result.getStatus());
        assertEquals(
                "Conta não encontrada no serviço de saldo.",
                result.getRejectionReason());

        verify(transactionRepository).lockUserTransactions(userId);
    }

    @Test
    void deveRejeitarTransacaoQuandoServicoDeSaldoEstaIndisponivel() {

        Transaction transaction = criarTransacao(
                TransactionType.WITHDRAWAL, userId, null, "100.00");

        when(userValidationPort.validateUserExists(userId))
                .thenReturn(criarUsuario(userId));

        when(accountBalancePort.findByEmail("usuario@email.com"))
                .thenThrow(new BalanceServiceUnavailableException(
                        "Serviço indisponível", null));

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = transactionService.createTransaction(transaction);

        assertEquals(TransactionStatus.REJECTED, result.getStatus());
        assertEquals(
                "Serviço de saldo indisponível, tente novamente mais tarde.",
                result.getRejectionReason());

        verify(transactionRepository).lockUserTransactions(userId);
    }

    @Test
    void deveBuscarTransacaoPorId() {

        UUID transactionId = UUID.randomUUID();

        Transaction transaction = criarTransacao(
                TransactionType.DEPOSIT, userId, null, "100.00");

        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.of(transaction));

        Optional<Transaction> result =
                transactionService.findTransactionById(transactionId);

        assertEquals(Optional.of(transaction), result);

        verify(transactionRepository).findById(transactionId);
    }

    @Test
    void deveBuscarSaldoDoUsuarioPeloEmail() {

        AccountBalance balance = contaComSaldo();

        when(userValidationPort.validateUserExists(userId))
                .thenReturn(criarUsuario(userId));

        when(accountBalancePort.findByEmail("usuario@email.com"))
                .thenReturn(Optional.of(balance));

        Optional<AccountBalance> result = transactionService.getBalance(userId);

        assertTrue(result.isPresent());
        assertSame(balance, result.get());

        verify(userValidationPort).validateUserExists(userId);
        verify(accountBalancePort).findByEmail("usuario@email.com");
    }

    @Test
    void deveBuscarTransacoesComFiltros() {

        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        List<Transaction> transactions = List.of(
                criarTransacao(TransactionType.DEPOSIT, userId, null, "100.00"));

        when(transactionRepository.findByFilters(
                userId, startDate, endDate, TransactionType.DEPOSIT))
                .thenReturn(transactions);

        List<Transaction> result = transactionService.findTransactions(
                userId, startDate, endDate, TransactionType.DEPOSIT);

        assertEquals(transactions, result);

        verify(transactionRepository).findByFilters(
                userId, startDate, endDate, TransactionType.DEPOSIT);
    }

    private UserInfo criarUsuario(UUID id) {
        return new UserInfo(id, "Usuário de teste", "usuario@email.com");
    }

    private AccountBalance contaComSaldo() {
        return new AccountBalance(
                new BigDecimal("1000.00"),
                new BigDecimal("1500.00"),
                new BigDecimal("800.00"));
    }

    private Transaction criarTransacao(
            TransactionType type,
            UUID transactionUserId,
            UUID destination,
            String amount) {

        return new Transaction(
                transactionUserId,
                type,
                Category.OUTROS,
                "Transação de teste",
                new BigDecimal(amount),
                LocalDate.now(ZoneId.of("America/Sao_Paulo")),
                null,
                destination);
    }
}