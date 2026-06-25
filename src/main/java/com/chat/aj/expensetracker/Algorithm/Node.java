package com.chat.aj.expensetracker.Algorithm;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Node {
    private String personName;

    public Node(String personName) {
        this.personName = personName;
    }


}
