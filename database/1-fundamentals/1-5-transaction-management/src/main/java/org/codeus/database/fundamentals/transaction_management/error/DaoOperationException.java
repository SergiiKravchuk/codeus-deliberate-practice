package org.codeus.database.fundamentals.transaction_management.error;

public class DaoOperationException extends RuntimeException {

    public DaoOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public DaoOperationException(String message) {
        super(message);
    }

    public DaoOperationException(Throwable cause) {
        super(cause);
    }
}
