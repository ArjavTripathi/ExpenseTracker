package com.chat.aj.expensetracker.Algorithm;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class Edge {
    private Node ower;
    private Node owed;
    private BigDecimal amount;
}
