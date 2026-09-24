package com.chat.aj.expensetracker.Groups;

import com.chat.aj.expensetracker.Algorithm.Algorithm;
import com.chat.aj.expensetracker.Algorithm.DTO.SettlementDTO;
import com.chat.aj.expensetracker.Auth.AuthService;
import com.chat.aj.expensetracker.Groups.DTOs.FriendSettlementsDTO;
import com.chat.aj.expensetracker.common.Entities.ExpenseParticipantsRepository;
import com.chat.aj.expensetracker.common.Entities.Expenses;
import com.chat.aj.expensetracker.common.Entities.ExpensesRepository;
import com.chat.aj.expensetracker.common.Entities.Group;
import com.chat.aj.expensetracker.common.Entities.GroupMembersRepository;
import com.chat.aj.expensetracker.common.Entities.GroupRepository;
import com.chat.aj.expensetracker.common.Entities.User;
import com.chat.aj.expensetracker.common.Exceptions.ForbiddenException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private AuthService userService;
    @Mock
    private GroupMembersRepository groupMembersRepository;
    @Mock
    private ExpensesRepository expensesRepository;
    @Mock
    private ExpenseParticipantsRepository expenseParticipantsRepository;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private Algorithm algorithm;
    @InjectMocks
    private GroupService groupService;

    @Test
    void requireMemberRejectsOutsiders() {
        User owner = user(1L, "Alice", "alice@example.com");
        User outsider = user(2L, "Bob", "bob@example.com");
        Group group = group(4L, owner);
        when(groupRepository.findById(4L)).thenReturn(Optional.of(group));
        when(userService.findUserByEmail(outsider.getEmail())).thenReturn(outsider);
        when(groupMembersRepository.findByGroupAndMember(group, outsider)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupService.requireMember(4L, outsider.getEmail()))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void deleteGroupRemovesExpensesBeforeTheGroup() {
        User owner = user(1L, "Alice", "alice@example.com");
        Group group = group(4L, owner);
        Expenses expense = new Expenses();
        expense.setId(8L);
        expense.setGroup(group);
        when(groupRepository.findById(4L)).thenReturn(Optional.of(group));
        when(userService.findUserByEmail(owner.getEmail())).thenReturn(owner);
        when(expensesRepository.findByGroup(group)).thenReturn(List.of(expense));
        when(expenseParticipantsRepository.findExpenseParticipantsByExpenses(expense)).thenReturn(List.of());
        when(groupMembersRepository.findByGroup(group)).thenReturn(List.of());

        groupService.deleteGroup(4L, owner.getEmail());

        InOrder order = inOrder(expenseParticipantsRepository, expensesRepository, groupMembersRepository, groupRepository);
        order.verify(expenseParticipantsRepository).deleteAll(List.of());
        order.verify(expensesRepository).deleteAll(List.of(expense));
        order.verify(groupMembersRepository).deleteAll(List.of());
        order.verify(groupRepository).delete(group);
        verify(algorithm).invalidateCache(4L);
    }

    @Test
    void friendSettlementsUseIdsAndKeepDirection() {
        User alice = user(1L, "Alex", "alice@example.com");
        User otherAlex = user(2L, "Alex", "alex@example.com");
        User bob = user(3L, "Bob", "bob@example.com");
        Group first = group(10L, alice);
        Group second = group(11L, alice);

        when(userService.findUserByEmail(alice.getEmail())).thenReturn(alice);
        when(groupRepository.findByMembers_MemberOrOwner(alice, alice)).thenReturn(List.of(first, second));
        when(algorithm.getOrComputeCache(10L)).thenReturn(List.of(
                settlement(otherAlex, alice, "15.00")
        ));
        when(algorithm.getOrComputeCache(11L)).thenReturn(List.of(
                settlement(alice, bob, "4.00"),
                settlement(bob, alice, "1.00")
        ));

        List<FriendSettlementsDTO> friends = groupService.getFriendSettlements(alice.getEmail());

        assertThat(friends).hasSize(2);
        assertThat(friends).anySatisfy(row -> {
            assertThat(row.getFriend()).isEqualTo("Alex");
            assertThat(row.getAmount()).isEqualByComparingTo("15.00");
        });
        assertThat(friends).anySatisfy(row -> {
            assertThat(row.getFriend()).isEqualTo("Bob");
            assertThat(row.getAmount()).isEqualByComparingTo("-3.00");
        });
    }

    private static SettlementDTO settlement(User ower, User owed, String amount) {
        return new SettlementDTO(
                new SettlementDTO.UserRef(ower.getId(), ower.getName()),
                new SettlementDTO.UserRef(owed.getId(), owed.getName()),
                new BigDecimal(amount)
        );
    }

    private static User user(Long id, String name, String email) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        return user;
    }

    private static Group group(Long id, User owner) {
        Group group = new Group();
        group.setGroupId(id);
        group.setOwner(owner);
        return group;
    }
}
