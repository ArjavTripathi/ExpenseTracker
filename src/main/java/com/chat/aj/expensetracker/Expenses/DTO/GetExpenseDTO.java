package com.chat.aj.expensetracker.Expenses.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GetExpenseDTO {
    private Long id;
    private String description;
    private BigDecimal amount;
    private String payerName;
}
