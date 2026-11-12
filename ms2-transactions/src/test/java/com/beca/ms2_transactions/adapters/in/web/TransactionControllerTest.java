package com.beca.ms2_transactions.adapters.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.beca.ms2_transactions.application.port.in.CreateTransactionUseCase;
import com.beca.ms2_transactions.application.port.in.FindTransactionUseCase;
import com.beca.ms2_transactions.application.port.out.UserInfo;
import com.beca.ms2_transactions.domain.AccountBalance;
import com.beca.ms2_transactions.domain.Category;
import com.beca.ms2_transactions.domain.Transaction;
import com.beca.ms2_transactions.domain.TransactionStatus;
import com.beca.ms2_transactions.domain.TransactionType;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private CreateTransactionUseCase createTransactionUseCase;

    @Mock
    private FindTransactionUseCase findTransactionUseCase;

    private MockMvc mockMvc;

    private final UUID userId = UUID.randomUUID();
    private final UUID transactionId = UUID.randomUUID();

    private UserInfo authenticatedUser;

    @BeforeEach
    void setUp() {
        authenticatedUser = new UserInfo(
                userId,
                "Usuário de teste",
                "usuario@email.com");

        TransactionController controller = new TransactionController(
                createTransactionUseCase,
                findTransactionUseCase);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setCustomArgumentResolvers(
                        new AuthenticationPrincipalArgumentResolver())
                .build();

        var authentication = new UsernamePasswordAuthenticationToken(
                authenticatedUser,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Transaction criarTransacao() {
        return new Transaction(
                transactionId,
                userId,
                TransactionType.DEPOSIT,
                Category.OUTROS,
                "Depósito de teste",
                new BigDecimal("100.00"),
                LocalDate.of(2026, 11, 11),
                "Conta principal",
                null,
                LocalDateTime.of(2026, 11, 11, 10, 0),
                TransactionStatus.APPROVED,
                null);
    }

    @Test
    void deveCriarTransacaoERetornar201() throws Exception {

        Transaction transaction = criarTransacao();

        when(createTransactionUseCase.createTransaction(any(Transaction.class)))
                .thenReturn(transaction);

        String requestBody = """
                {
                    "type": "DEPOSIT",
                    "category": "OUTROS",
                    "description": "Depósito de teste",
                    "amount": 100.00,
                    "date": "2026-11-11",
                    "origin": "Conta principal",
                    "destination": null
                }
                """;

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(transactionId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.type").value("DEPOSIT"))
                .andExpect(jsonPath("$.category").value("OUTROS"))
                .andExpect(jsonPath("$.description").value("Depósito de teste"))
                .andExpect(jsonPath("$.date").value("2026-11-11"))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.amount").value(100.00));

        verify(createTransactionUseCase)
                .createTransaction(any(Transaction.class));
    }

    @Test
    void deveBuscarTransacaoDoUsuarioERetornar200() throws Exception {

        Transaction transaction = criarTransacao();

        when(findTransactionUseCase.findTransactionById(transactionId))
                .thenReturn(Optional.of(transaction));

        mockMvc.perform(get("/api/transactions/{id}", transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transactionId.toString()))
                .andExpect(jsonPath("$.description").value("Depósito de teste"))
                .andExpect(jsonPath("$.amount").value(100.00));

        verify(findTransactionUseCase).findTransactionById(transactionId);
    }

    @Test
    void naoDeveEncontrarTransacaoERetornar404() throws Exception {

        when(findTransactionUseCase.findTransactionById(transactionId))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/transactions/{id}", transactionId))
                .andExpect(status().isNotFound());

        verify(findTransactionUseCase).findTransactionById(transactionId);
    }

    @Test
    void naoDevePermitirConsultarTransacaoDeOutroUsuario() throws Exception {

        UUID outroUsuarioId = UUID.randomUUID();

        Transaction transaction = new Transaction(
                transactionId,
                outroUsuarioId,
                TransactionType.DEPOSIT,
                Category.OUTROS,
                "Transação de outro usuário",
                new BigDecimal("100.00"),
                LocalDate.of(2026, 11, 11),
                "Conta principal",
                null,
                LocalDateTime.of(2026, 11, 11, 10, 0),
                TransactionStatus.APPROVED,
                null);

        when(findTransactionUseCase.findTransactionById(transactionId))
                .thenReturn(Optional.of(transaction));

        mockMvc.perform(get("/api/transactions/{id}", transactionId))
                .andExpect(status().isNotFound());

        verify(findTransactionUseCase).findTransactionById(transactionId);
    }

    @Test
    void deveConsultarSaldoERetornar200() throws Exception {

        AccountBalance balance = new AccountBalance(
                new BigDecimal("250.00"),
                new BigDecimal("1500.00"),
                new BigDecimal("800.00"));

        when(findTransactionUseCase.getBalance(userId))
                .thenReturn(Optional.of(balance));

        mockMvc.perform(get("/api/transactions/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(250.00))
                .andExpect(jsonPath("$.limiteDiario").value(1500.00))
                .andExpect(jsonPath("$.limitePorTransacao").value(800.00));

        verify(findTransactionUseCase).getBalance(userId);
    }

    @Test
    void deveRetornar404QuandoContaNaoExiste() throws Exception {

        when(findTransactionUseCase.getBalance(userId))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/transactions/balance"))
                .andExpect(status().isNotFound());

        verify(findTransactionUseCase).getBalance(userId);
    }
}