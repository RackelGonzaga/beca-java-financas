package com.beca.ms2_transactions.application.port.in;

import java.util.UUID;

public interface DeleteTransactionUseCase {
    void execute(UUID id);
}
