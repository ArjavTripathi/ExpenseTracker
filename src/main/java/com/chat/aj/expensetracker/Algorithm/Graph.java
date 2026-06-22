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
}
