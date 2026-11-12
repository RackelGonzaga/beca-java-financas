package com.beca.ms2_transactions.adapters.in.web;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.beca.ms2_transactions.adapters.in.dto.CreateTransactionRequest;
import com.beca.ms2_transactions.adapters.in.dto.TransactionResponse;
import com.beca.ms2_transactions.application.port.in.CreateTransactionUseCase;
import com.beca.ms2_transactions.application.port.in.FindTransactionUseCase;
import com.beca.ms2_transactions.application.port.out.UserInfo;
import com.beca.ms2_transactions.domain.Transaction;
import com.beca.ms2_transactions.domain.AccountBalance;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final CreateTransactionUseCase createTransactionUseCase;
    private final FindTransactionUseCase findTransactionUseCase;

    public TransactionController(
            CreateTransactionUseCase createTransactionUseCase,
            FindTransactionUseCase findTransactionUseCase) {

        this.createTransactionUseCase = createTransactionUseCase;
        this.findTransactionUseCase = findTransactionUseCase;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(
            @Valid @RequestBody CreateTransactionRequest request,
            @AuthenticationPrincipal UserInfo authenticatedUser) {

        Transaction transaction = request.toDomain(authenticatedUser.id());

        Transaction created = createTransactionUseCase
                .createTransaction(transaction);

        URI location = URI.create("/api/transactions/" + created.getId());

        return ResponseEntity.created(location)
                .body(TransactionResponse.from(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> findById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserInfo authenticatedUser) {

        return findTransactionUseCase.findTransactionById(id)
                .filter(transaction ->
                        transaction.getUserId().equals(authenticatedUser.id()))
                .map(TransactionResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/balance")
    public ResponseEntity<AccountBalance> getBalance(
            @AuthenticationPrincipal UserInfo authenticatedUser) {

        return findTransactionUseCase.getBalance(authenticatedUser.id())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}