package com.chat.aj.expensetracker.Algorithm;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class Graph {
    private final List<Edge> settlements = new ArrayList<>();

    public void addSettlement(Edge edge) {
        settlements.add(edge);
    }

}
