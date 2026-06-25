package com.chat.aj.expensetracker.Algorithm;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Node {
    private String personName;
    private Edge owedToOther;

    public Node(String personName) {
        this.personName = personName;
    }

    public Node(Graph graph, String personName) {
        this.personName = personName;
        graph.addNode(this);
    }
}
