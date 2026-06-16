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
    public User user;
    public String description;
    public BigDecimal amount;
    public LocalDateTime created_at;

}
