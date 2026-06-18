package com.chat.aj.expensetracker.Expenses.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateExpenseDTO {
    private Long groupId;
    private BigDecimal totalAmount;
    private String description;
    private List<ParticipantShareDTO> participants;
}
