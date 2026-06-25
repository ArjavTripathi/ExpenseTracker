package com.chat.aj.expensetracker.Algorithm.DTO;

import com.chat.aj.expensetracker.Algorithm.Edge;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class SettlementDTO {
    private String ower;
    private String owed;
    private BigDecimal amount;

    public SettlementDTO(Edge e){
        this.ower = e.getOwer().getPersonName();
        this.owed = e.getOwed().getPersonName();
        this.amount = e.getAmount();
    }
}
