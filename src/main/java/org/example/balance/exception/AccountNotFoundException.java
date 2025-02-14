package org.example.balance.exception;

import java.util.UUID;

public class AccountNotFoundException extends RuntimeException{

    public AccountNotFoundException(UUID id) {
        super("Такой счет не найден: " + id);
    }
}
