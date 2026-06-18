package com.chat.aj.expensetracker.Expenses.DTO;

import com.chat.aj.expensetracker.common.Entities.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseReturnDTO {
    private User user;
    private String description;
    private BigDecimal amount;
    private LocalDateTime created_at;

}
