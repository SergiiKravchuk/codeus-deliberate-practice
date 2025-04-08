package org.codeus.database.fundamentals.transaction_management.error;

public class LoanNotFoundException extends RuntimeException {
    public LoanNotFoundException(String message) {
        super(message);
    }
}
