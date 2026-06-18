package com.chat.aj.expensetracker.Expenses.DTO;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseRequestDTO {
    private Long groupId;
    private Long expenseId;
}
