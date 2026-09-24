package com.chat.aj.expensetracker.Algorithm;

import com.chat.aj.expensetracker.common.Entities.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AlgorithmTest {
    private final Algorithm algorithm = new Algorithm(null, null, null, null);

    @Test
    void settlesOneCreditorAgainstTwoDebtors() {
        User alice = user(1L, "Alice");
        User bob = user(2L, "Bob");
        User carol = user(3L, "Carol");

        List<Edge> settlements = algorithm.algorithm(Map.of(
                alice, new BigDecimal("30.00"),
                bob, new BigDecimal("-10.00"),
                carol, new BigDecimal("-20.00")
        ));

        assertThat(settlements).hasSize(2);
        assertThat(amountFrom(settlements, carol, alice)).isEqualByComparingTo("20.00");
        assertThat(amountFrom(settlements, bob, alice)).isEqualByComparingTo("10.00");
    }

    @Test
    void collapsesACycleToTheMinimumTransfers() {
        User alice = user(1L, "Alice");
        User bob = user(2L, "Bob");
        User carol = user(3L, "Carol");

        List<Edge> settlements = algorithm.algorithm(Map.of(
                alice, new BigDecimal("10.00"),
                bob, new BigDecimal("5.00"),
                carol, new BigDecimal("-15.00")
        ));

        assertThat(settlements).hasSize(2);
        assertThat(amountFrom(settlements, carol, alice)).isEqualByComparingTo("10.00");
        assertThat(amountFrom(settlements, carol, bob)).isEqualByComparingTo("5.00");
    }

    @Test
    void splitsTenDollarsThreeWaysWithoutLeavingARemainder() {
        User alice = user(1L, "Alice");
        User bob = user(2L, "Bob");
        User carol = user(3L, "Carol");

        List<Edge> settlements = algorithm.algorithm(Map.of(
                alice, new BigDecimal("6.66"),
                bob, new BigDecimal("-3.33"),
                carol, new BigDecimal("-3.33")
        ));

        assertThat(settlements).hasSize(2);
        assertThat(amountFrom(settlements, bob, alice)).isEqualByComparingTo("3.33");
        assertThat(amountFrom(settlements, carol, alice)).isEqualByComparingTo("3.33");
    }

    @Test
    void leavesZeroBalancesOutOfBothHeaps() {
        User alice = user(1L, "Alice");
        User bob = user(2L, "Bob");
        User carol = user(3L, "Carol");

        List<Edge> settlements = algorithm.algorithm(Map.of(
                alice, new BigDecimal("10.00"),
                bob, new BigDecimal("-10.00"),
                carol, BigDecimal.ZERO
        ));

        assertThat(settlements).hasSize(1);
        assertThat(settlements.get(0).getOwer().getId()).isEqualTo(bob.getId());
        assertThat(settlements.get(0).getOwed().getId()).isEqualTo(alice.getId());
        assertThat(settlements.get(0).getAmount()).isEqualByComparingTo("10.00");
    }

    @Test
    void returnsNothingWhenEveryoneIsSettled() {
        User alice = user(1L, "Alice");
        User bob = user(2L, "Bob");

        List<Edge> settlements = algorithm.algorithm(Map.of(
                alice, BigDecimal.ZERO,
                bob, BigDecimal.ZERO
        ));

        assertThat(settlements).isEmpty();
    }

    private static BigDecimal amountFrom(List<Edge> settlements, User ower, User owed) {
        return settlements.stream()
                .filter(edge -> edge.getOwer().getId().equals(ower.getId())
                        && edge.getOwed().getId().equals(owed.getId()))
                .map(Edge::getAmount)
                .findFirst()
                .orElseThrow();
    }

    private static User user(Long id, String name) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        return user;
    }
}
