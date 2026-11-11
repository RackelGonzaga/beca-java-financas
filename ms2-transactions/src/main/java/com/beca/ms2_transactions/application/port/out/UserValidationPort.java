package com.beca.ms2_transactions.application.port.out;

import java.util.UUID;

public interface UserValidationPort {

    void validateUserExists(UUID userId);
}