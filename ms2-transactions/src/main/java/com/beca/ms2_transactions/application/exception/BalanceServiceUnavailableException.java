package com.beca.ms2_transactions.application.exception;

public class BalanceServiceUnavailableException extends RuntimeException {

    public BalanceServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}