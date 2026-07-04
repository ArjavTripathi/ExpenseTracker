package com.chat.aj.expensetracker.Expenses;

import com.chat.aj.expensetracker.Algorithm.Algorithm;
import com.chat.aj.expensetracker.Auth.AuthService;
import com.chat.aj.expensetracker.Expenses.DTO.*;
import com.chat.aj.expensetracker.Groups.GroupService;
import com.chat.aj.expensetracker.Websockets.DTO.NotificationsDTO;
import com.chat.aj.expensetracker.common.Entities.*;
import com.chat.aj.expensetracker.common.Exceptions.ResourceNotFoundException;
import com.chat.aj.expensetracker.common.Exceptions.UnauthorizedException;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    private final GroupService groupService;
    private final AuthService authService;
    private final ExpenseParticipantsRepository expenseParticipantsRepository;
    private final ExpensesRepository expensesRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final Algorithm algorithm;

    @Transactional
    public void createExpense(CreateExpenseDTO dto, Long groupId, String callerEmail) {
        Group group = groupService.findGroupById(groupId);
        User caller = authService.findUserByEmail(callerEmail);
        if (!groupService.isGroupMember(group, caller)) {
            throw new UnauthorizedException("You are not a member of this group");
        }
        BigDecimal totalShares = dto.getParticipants().stream()
                .map(ParticipantShareDTO::getShareAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalShares.compareTo(dto.getTotalAmount()) != 0) {
            throw new ValidationException("Participant shares must equal total amount");
        }

        Expenses expense = new Expenses();
        expense.setGroup(group);
        expense.setUser(caller);
        expense.setAmount(dto.getTotalAmount());
        expense.setDescription(dto.getDescription());
        expense.setCreatedAt(LocalDateTime.now());
        expensesRepository.save(expense);

        List<Long> userIds = dto.getParticipants().stream()
                .map(ParticipantShareDTO::getUserId)
                .toList();
        Map<Long, User> userMap = authService.findUsersByIds(userIds);

        for (ParticipantShareDTO p : dto.getParticipants()) {
            User participant = userMap.get(p.getUserId());
            if (participant == null) throw new ResourceNotFoundException("Cannot find user: " + p.getUserId());
            ExpenseParticipants ep = new ExpenseParticipants();
            ep.setExpenses(expense);
            ep.setUser(participant);
            ep.setAmount(p.getShareAmount());
            expenseParticipantsRepository.save(ep);
        }

        messagingTemplate.convertAndSend(
                "/topic/group/" + groupId,
                new NotificationsDTO("EXPENSE_ADDED", "A new expense was added", groupId)
        );

        algorithm.invalidateCache(groupId);
    }

    public List<GetExpenseDTO> getAllExpenses(Long groupId, String callerEmail) {
        Group group = groupService.findGroupById(groupId);
        User caller = authService.findUserByEmail(callerEmail);
        if (!groupService.isGroupMember(group, caller)) {
            throw new UnauthorizedException("You are not a member of this group");
        }
        List<Expenses> expenses = expensesRepository.findByGroup(group);
        return expenses.stream()
                .map(e -> new GetExpenseDTO(
                        e.getId(),
                        e.getDescription(),
                        e.getAmount(),
                        e.getUser().getId(),
                        e.getUser().getName(),
                        e.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public ExpenseReturnDTO getExpense(Long groupId, Long expenseId, String callerEmail) {
        Group group = groupService.findGroupById(groupId);
        User caller = authService.findUserByEmail(callerEmail);
        if (!groupService.isGroupMember(group, caller)) {
            throw new UnauthorizedException("You are not a member of this group");
        }
        Expenses expense = expensesRepository.findExpenseById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find expense"));
        List<ExpenseParticipants> participants = expenseParticipantsRepository.findExpenseParticipantsByExpenses(expense);
        List<ExpenseParticipantsDTO> participantDTOs = participants.stream()
                .map(p -> new ExpenseParticipantsDTO(p.getUser().getId(), p.getAmount()))
                .collect(Collectors.toList());
        return new ExpenseReturnDTO(groupId, expense.getUser().getName(), expense.getDescription(), expense.getAmount(), expense.getCreatedAt(), participantDTOs);
    }

    @Transactional
    public void updateExpense(UpdateExpenseDTO dto, Long groupId, Long expenseId, String callerEmail) {
        groupService.findGroupById(groupId);
        User caller = authService.findUserByEmail(callerEmail);
        Expenses expense = expensesRepository.findExpenseById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find expense"));
        if (!expense.getUser().equals(caller)) {
            throw new UnauthorizedException("Only the expense creator can update this expense");
        }
        BigDecimal totalShares = dto.getParticipants().stream()
                .map(ParticipantShareDTO::getShareAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalShares.compareTo(dto.getTotalAmount()) != 0) {
            throw new ValidationException("Participant shares must equal total amount");
        }
        expense.setAmount(dto.getTotalAmount());
        expense.setDescription(dto.getDescription());
        expensesRepository.save(expense);

        List<ExpenseParticipants> oldParticipants = expenseParticipantsRepository.findExpenseParticipantsByExpenses(expense);
        expenseParticipantsRepository.deleteAll(oldParticipants);

        List<Long> userIds = dto.getParticipants().stream()
                .map(ParticipantShareDTO::getUserId)
                .toList();
        Map<Long, User> userMap = authService.findUsersByIds(userIds);

        for (ParticipantShareDTO p : dto.getParticipants()) {
            User participant = userMap.get(p.getUserId());
            if (participant == null) throw new ResourceNotFoundException("Cannot find user: " + p.getUserId());
            ExpenseParticipants ep = new ExpenseParticipants();
            ep.setExpenses(expense);
            ep.setUser(participant);
            ep.setAmount(p.getShareAmount());
            expenseParticipantsRepository.save(ep);
        }

        messagingTemplate.convertAndSend(
                "/topic/group/" + groupId,
                new NotificationsDTO("EXPENSE_UPDATED", "An existing expense was updated", groupId)
        );

        algorithm.invalidateCache(groupId);
    }

    @Transactional
    public void deleteExpense(Long groupId, Long expenseId, String callerEmail) {
        Group group = groupService.findGroupById(groupId);
        User caller = authService.findUserByEmail(callerEmail);
        Expenses expense = expensesRepository.findExpenseById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find expense"));
        if (!expense.getUser().equals(caller) && !group.getOwner().equals(caller)) {
            throw new UnauthorizedException("Only the expense creator or group owner can delete this expense");
        }
        List<ExpenseParticipants> participants = expenseParticipantsRepository.findExpenseParticipantsByExpenses(expense);
        expenseParticipantsRepository.deleteAll(participants);
        expensesRepository.delete(expense);
        messagingTemplate.convertAndSend(
                "/topic/group/" + groupId,
                new NotificationsDTO("EXPENSE_DELETED", "An expense was deleted", groupId)
        );
        algorithm.invalidateCache(groupId);
    }

    public List<MyExpensesDTO> getRecentExpenses(String name) {
        User user = authService.findUserByEmail(name);
        List<ExpenseParticipants> participantRecords = expenseParticipantsRepository.findByUser(user);
        return participantRecords.stream()
                .map(record -> {
                    Expenses expense = record.getExpenses();
                    return new MyExpensesDTO(
                            expense.getGroup().getName(),
                            expense.getUser().getName(),
                            expense.getAmount(),
                            record.getAmount()
                    );
                })
                .collect(Collectors.toList());
    }
}
