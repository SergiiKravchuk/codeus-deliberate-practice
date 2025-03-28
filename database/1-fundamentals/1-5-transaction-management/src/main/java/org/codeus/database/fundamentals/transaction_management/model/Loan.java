package org.codeus.database.fundamentals.transaction_management.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class Loan {
    private int loanId;
    private int customerId;
    private String loanType;
    private BigDecimal loanAmount;
    private BigDecimal interestRate;
    private LocalDate loanStartDate;
    private LocalDate loanEndDate;
}
