package com.beca.ms2_transactions.application.port.in;

import com.beca.ms2_transactions.domain.Transaction;

public interface CreateTransactionUseCase {
    
    Transaction createTransaction(Transaction transaction);
}
