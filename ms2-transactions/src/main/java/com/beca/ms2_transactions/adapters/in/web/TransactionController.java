package com.beca.ms2_transactions.adapters.in.web;

import java.math.BigDecimal;
import java.net.URI;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
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
import com.beca.ms2_transactions.domain.Transaction;

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
            @Valid @RequestBody CreateTransactionRequest request) {

        Transaction transaction = request.toDomain();

        Transaction created = createTransactionUseCase
                .createTransaction(transaction);

        URI location = URI.create("/api/transactions/" + created.getId());

        return ResponseEntity.created(location)
                .body(TransactionResponse.from(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> findById(
            @PathVariable UUID id) {

        return findTransactionUseCase.findTransactionById(id)
                .map(TransactionResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/users/{userId}/balance")
    public ResponseEntity<BigDecimal> getBalance(
            @PathVariable UUID userId) {

        return ResponseEntity.ok(
                findTransactionUseCase.getBalance(userId)
        );
    }
}