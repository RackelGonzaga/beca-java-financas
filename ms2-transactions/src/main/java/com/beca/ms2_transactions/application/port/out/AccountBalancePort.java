package com.beca.ms2_transactions.application.port.out;

import java.util.Optional;

import com.beca.ms2_transactions.domain.AccountBalance;

public interface AccountBalancePort {

    Optional<AccountBalance> findByEmail(String email);
    
}