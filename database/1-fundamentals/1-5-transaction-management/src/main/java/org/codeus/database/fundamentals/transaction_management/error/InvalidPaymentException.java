package org.codeus.database.fundamentals.transaction_management.error;

public class InvalidPaymentException extends RuntimeException {
    public InvalidPaymentException(String message) {
        super(message);
    }
}
