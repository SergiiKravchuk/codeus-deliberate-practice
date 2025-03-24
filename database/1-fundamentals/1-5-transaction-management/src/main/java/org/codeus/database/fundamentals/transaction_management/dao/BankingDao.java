package org.codeus.database.fundamentals.transaction_management.dao;

import org.codeus.database.fundamentals.transaction_management.error.CustomerNotFoundException;
import org.codeus.database.fundamentals.transaction_management.error.DaoOperationException;
import org.codeus.database.fundamentals.transaction_management.model.Account;

public interface BankingDao {

    /**
     * Creates a new account for a customer with transaction management.
     * <p>
     * This method performs the following steps:
     * <ul>
     * <li>Starts a transaction by setting auto-commit to false.</li>
     * <li>Checks if the customer exists in the database. If the customer does not exist, throws {@code CustomerNotFoundException}.</li>
     * <li>Inserts a new record into the accounts table with the provided account details.</li>
     * <li>Retrieves the generated account ID and sets it to the provided {@code Account} object.</li>
     * <li>Commits the transaction.</li>
     * <li>If any step fails, rolls back the transaction. If the rollback fails, throws {@code DaoOperationException}.</li>
     * </ul>
     *
     * @param account the account to be created
     * @throws CustomerNotFoundException if the customer with the specified ID does not exist
     * @throws DaoOperationException     if there is an error during the transaction or rollback
     */
    void createAccount(Account account);
}
