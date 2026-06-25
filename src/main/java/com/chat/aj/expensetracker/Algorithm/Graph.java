package com.chat.aj.expensetracker.Algorithm;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Graph {
    private Node head;
    private ArrayList<Edge> edges;
    private ArrayList<Node> nodes;

    public void addNode(Node node) {
        if (nodes == null) {
            nodes = new ArrayList<>();
        }
        nodes.add(node);
    }

    public void addEdge(Edge edge){
        if(edges == null) {
            edges = new ArrayList<>();
        }
        edges.add(edge);
    }

}
