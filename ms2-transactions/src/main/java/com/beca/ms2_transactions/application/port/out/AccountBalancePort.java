package com.beca.ms2_transactions.application.port.out;

import java.math.BigDecimal;
import java.util.Optional;

import com.beca.ms2_transactions.domain.AccountBalance;

public interface AccountBalancePort {

    Optional<AccountBalance> findByEmail(String email);

    void updateBalance(AccountBalance account, BigDecimal novoSaldo);
}