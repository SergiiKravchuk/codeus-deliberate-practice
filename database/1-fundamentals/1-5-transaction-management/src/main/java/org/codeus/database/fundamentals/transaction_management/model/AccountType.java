package org.codeus.database.fundamentals.transaction_management.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum AccountType {
    /**
     * Everyday transaction account
     */
    CHECKING("checking"),
    /**
     * Standard interest-bearing account
     */
    SAVINGS("savings");

    private final String type;

    public static AccountType fromString(String type) {
        return Arrays.stream(values())
                .filter(accountType -> accountType.type.equalsIgnoreCase(type))
                .findFirst()
                .orElse(null);
    }
}
