package com.beca.ms2_transactions.application.port.in;

import java.util.UUID;
import com.beca.ms2_transactions.domain.Transaction;

public interface UpdateTransactionUseCase {
    
    Transaction updateTransaction(UUID id, Transaction transaction);

}
