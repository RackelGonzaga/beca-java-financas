package com.beca.ms1_users.application.port.in;

import java.util.List;

public record ImportUsersCommand(
        List<ImportUserRowCommand> usuarios
) {
}