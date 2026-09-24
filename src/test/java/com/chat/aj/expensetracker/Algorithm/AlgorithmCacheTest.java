package com.chat.aj.expensetracker.Algorithm;

import com.chat.aj.expensetracker.Algorithm.DTO.SettlementDTO;
import com.chat.aj.expensetracker.common.Entities.ExpenseParticipants;
import com.chat.aj.expensetracker.common.Entities.ExpenseParticipantsRepository;
import com.chat.aj.expensetracker.common.Entities.Expenses;
import com.chat.aj.expensetracker.common.Entities.ExpensesRepository;
import com.chat.aj.expensetracker.common.Entities.Group;
import com.chat.aj.expensetracker.common.Entities.GroupMembersRepository;
import com.chat.aj.expensetracker.common.Entities.GroupRepository;
import com.chat.aj.expensetracker.common.Entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlgorithmCacheTest {
    @Mock
    private ExpensesRepository expensesRepository;
    @Mock
    private ExpenseParticipantsRepository expenseParticipantsRepository;
    @Mock
    private GroupMembersRepository groupMembersRepository;
    @Mock
    private GroupRepository groupRepository;
    @InjectMocks
    private Algorithm algorithm;

    private Group group;
    private Expenses expense;

    @BeforeEach
    void setUp() {
        User alice = user(1L, "Alice");
        User bob = user(2L, "Bob");
        group = new Group();
        group.setGroupId(7L);
        group.setOwner(alice);

        expense = new Expenses();
        expense.setId(10L);
        expense.setGroup(group);
        expense.setUser(alice);
        expense.setAmount(new BigDecimal("20.00"));

        ExpenseParticipants aliceShare = participant(expense, alice, "10.00");
        ExpenseParticipants bobShare = participant(expense, bob, "10.00");

        when(groupRepository.findById(7L)).thenReturn(Optional.of(group));
        when(groupMembersRepository.findByGroup(group)).thenReturn(List.of());
        when(expensesRepository.findByGroup(group)).thenReturn(List.of(expense));
        when(expenseParticipantsRepository.findByExpensesGroup(group)).thenReturn(List.of(aliceShare, bobShare));
    }

    @Test
    void recomputesOnlyAfterInvalidation() {
        List<SettlementDTO> first = algorithm.getOrComputeCache(7L);
        List<SettlementDTO> second = algorithm.getOrComputeCache(7L);

        assertThat(second).isSameAs(first);
        assertThat(first).singleElement().satisfies(settlement -> {
            assertThat(settlement.getOwer().getId()).isEqualTo(2L);
            assertThat(settlement.getOwed().getId()).isEqualTo(1L);
            assertThat(settlement.getAmount()).isEqualByComparingTo("10.00");
        });
        verify(expensesRepository, times(1)).findByGroup(group);

        algorithm.invalidateCache(7L);
        algorithm.getOrComputeCache(7L);
        verify(expensesRepository, times(2)).findByGroup(group);
    }

    private static User user(Long id, String name) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        return user;
    }

    private static ExpenseParticipants participant(Expenses expense, User user, String amount) {
        ExpenseParticipants participant = new ExpenseParticipants();
        participant.setExpenses(expense);
        participant.setUser(user);
        participant.setAmount(new BigDecimal(amount));
        return participant;
    }
}
