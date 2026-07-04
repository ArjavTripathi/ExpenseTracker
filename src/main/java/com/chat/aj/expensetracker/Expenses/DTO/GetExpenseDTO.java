package com.chat.aj.expensetracker.Expenses.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GetExpenseDTO {
    private Long id;
    private String description;
    private BigDecimal totalAmount;
    private Long payerId;
    private String payerName;
    private LocalDateTime createdAt;
}
