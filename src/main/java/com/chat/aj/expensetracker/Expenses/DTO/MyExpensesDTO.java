package com.chat.aj.expensetracker.Expenses.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MyExpensesDTO {
    private String groupName;
    private String payer;
    private BigDecimal totalAmount;
    private BigDecimal myShare;
}
