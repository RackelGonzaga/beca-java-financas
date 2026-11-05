package com.beca.ms1_users.application.port.in;

import java.util.List;

public record ImportUsersResult(
        int totalLinhas,
        int importados,
        int rejeitados,
        List<ImportError> erros
) {
    public record ImportError(
            int linha,
            String motivo
    ) {
    }
}