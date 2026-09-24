package com.chat.aj.expensetracker.Expenses;

import com.chat.aj.expensetracker.Algorithm.Algorithm;
import com.chat.aj.expensetracker.Auth.AuthService;
import com.chat.aj.expensetracker.Expenses.DTO.CreateExpenseDTO;
import com.chat.aj.expensetracker.Expenses.DTO.ParticipantShareDTO;
import com.chat.aj.expensetracker.Groups.GroupService;
import com.chat.aj.expensetracker.common.Entities.ExpenseParticipantsRepository;
import com.chat.aj.expensetracker.common.Entities.Expenses;
import com.chat.aj.expensetracker.common.Entities.ExpensesRepository;
import com.chat.aj.expensetracker.common.Entities.Group;
import com.chat.aj.expensetracker.common.Entities.User;
import com.chat.aj.expensetracker.common.Exceptions.ForbiddenException;
import com.chat.aj.expensetracker.common.Exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {
    @Mock
    private GroupService groupService;
    @Mock
    private AuthService authService;
    @Mock
    private ExpenseParticipantsRepository expenseParticipantsRepository;
    @Mock
    private ExpensesRepository expensesRepository;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private Algorithm algorithm;
    @InjectMocks
    private ExpenseService expenseService;

    private Group pathGroup;
    private User owner;
    private User creator;
    private User outsider;

    @BeforeEach
    void setUp() {
        owner = user(1L, "Alice", "alice@example.com");
        creator = user(2L, "Bob", "bob@example.com");
        outsider = user(3L, "Carol", "carol@example.com");
        pathGroup = group(10L, owner);
    }

    @Test
    void deleteRejectsAnExpenseThatBelongsToAnotherGroup() {
        Group otherGroup = group(99L, owner);
        Expenses expense = expense(5L, otherGroup, creator);
        when(groupService.findGroupById(10L)).thenReturn(pathGroup);
        when(authService.findUserByEmail(owner.getEmail())).thenReturn(owner);
        when(expensesRepository.findExpenseById(5L)).thenReturn(Optional.of(expense));

        assertThatThrownBy(() -> expenseService.deleteExpense(10L, 5L, owner.getEmail()))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(expensesRepository, never()).delete(any());
    }

    @Test
    void deleteAllowsTheGroupOwnerOfThatExpense() {
        Expenses expense = expense(5L, pathGroup, creator);
        when(groupService.findGroupById(10L)).thenReturn(pathGroup);
        when(authService.findUserByEmail(owner.getEmail())).thenReturn(owner);
        when(expensesRepository.findExpenseById(5L)).thenReturn(Optional.of(expense));
        when(groupService.isGroupMember(pathGroup, owner)).thenReturn(true);
        when(expenseParticipantsRepository.findExpenseParticipantsByExpenses(expense)).thenReturn(List.of());

        expenseService.deleteExpense(10L, 5L, owner.getEmail());

        verify(expensesRepository).delete(expense);
        verify(algorithm).invalidateCache(10L);
    }

    @Test
    void deleteRejectsAMemberWhoNeitherCreatedNorOwns() {
        Expenses expense = expense(5L, pathGroup, creator);
        when(groupService.findGroupById(10L)).thenReturn(pathGroup);
        when(authService.findUserByEmail(outsider.getEmail())).thenReturn(outsider);
        when(expensesRepository.findExpenseById(5L)).thenReturn(Optional.of(expense));
        when(groupService.isGroupMember(pathGroup, outsider)).thenReturn(true);

        assertThatThrownBy(() -> expenseService.deleteExpense(10L, 5L, outsider.getEmail()))
                .isInstanceOf(ForbiddenException.class);
        verify(expensesRepository, never()).delete(any());
    }

    @Test
    void createRejectsAParticipantWhoIsNotInTheGroup() {
        CreateExpenseDTO dto = new CreateExpenseDTO(
                new BigDecimal("20.00"),
                "Dinner",
                List.of(
                        new ParticipantShareDTO(owner.getId(), new BigDecimal("10.00")),
                        new ParticipantShareDTO(outsider.getId(), new BigDecimal("10.00"))
                )
        );
        when(groupService.findGroupById(10L)).thenReturn(pathGroup);
        when(authService.findUserByEmail(owner.getEmail())).thenReturn(owner);
        when(groupService.isGroupMember(pathGroup, owner)).thenReturn(true);
        when(authService.findUsersByIds(List.of(owner.getId(), outsider.getId())))
                .thenReturn(Map.of(owner.getId(), owner, outsider.getId(), outsider));
        when(groupService.isGroupMember(pathGroup, outsider)).thenReturn(false);

        assertThatThrownBy(() -> expenseService.createExpense(dto, 10L, owner.getEmail()))
                .isInstanceOf(ForbiddenException.class);
        verify(expensesRepository, never()).save(any());
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

    private static Expenses expense(Long id, Group group, User payer) {
        Expenses expense = new Expenses();
        expense.setId(id);
        expense.setGroup(group);
        expense.setUser(payer);
        return expense;
    }
}
