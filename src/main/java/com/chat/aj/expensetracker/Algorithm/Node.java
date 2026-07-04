package com.chat.aj.expensetracker.Algorithm;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Node {
    private Long id;
    private String personName;

    public Node(Long id, String personName) {
        this.id = id;
        this.personName = personName;
    }
}
