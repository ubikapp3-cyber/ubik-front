package com.ubik.usermanagement.domain.exception;

public class AccountDeletedException extends RuntimeException {
    public AccountDeletedException(String message) {
        super(message);
    }
}
