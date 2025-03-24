package org.codeus.database.fundamentals.transaction_management.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Getter
@Setter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class Account {

    private int accountId;
    private int customerId;
    private AccountType accountType;
    private BigDecimal balance;
    private Timestamp createdAt;
}
