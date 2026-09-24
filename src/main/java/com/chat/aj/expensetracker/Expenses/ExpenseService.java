package com.chat.aj.expensetracker.Expenses;

import com.chat.aj.expensetracker.Algorithm.Algorithm;
import com.chat.aj.expensetracker.Auth.AuthService;
import com.chat.aj.expensetracker.Expenses.DTO.*;
import com.chat.aj.expensetracker.Groups.GroupService;
import com.chat.aj.expensetracker.Websockets.DTO.NotificationsDTO;
import com.chat.aj.expensetracker.common.Entities.*;
import com.chat.aj.expensetracker.common.Exceptions.ForbiddenException;
import com.chat.aj.expensetracker.common.Exceptions.ResourceNotFoundException;
import com.chat.aj.expensetracker.common.Utility.AfterCommit;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
            throw new ForbiddenException("You are not a member of this group");
        }
        validateShares(dto.getParticipants(), dto.getTotalAmount());
        List<Long> userIds = dto.getParticipants().stream()
                .map(ParticipantShareDTO::getUserId)
                .toList();
        Map<Long, User> userMap = authService.findUsersByIds(userIds);
        requireParticipantMembers(group, userMap, userIds);

        Expenses expense = new Expenses();
        expense.setGroup(group);
        expense.setUser(caller);
        expense.setAmount(dto.getTotalAmount());
        expense.setDescription(dto.getDescription());
        expense.setCreatedAt(LocalDateTime.now());
        expensesRepository.save(expense);

        for (ParticipantShareDTO p : dto.getParticipants()) {
            ExpenseParticipants ep = new ExpenseParticipants();
            ep.setExpenses(expense);
            ep.setUser(userMap.get(p.getUserId()));
            ep.setAmount(p.getShareAmount());
            expenseParticipantsRepository.save(ep);
        }

        publishAfterCommit(groupId, new NotificationsDTO("EXPENSE_ADDED", "A new expense was added", groupId));
    }

    public List<GetExpenseDTO> getAllExpenses(Long groupId, String callerEmail) {
        Group group = groupService.findGroupById(groupId);
        User caller = authService.findUserByEmail(callerEmail);
        if (!groupService.isGroupMember(group, caller)) {
            throw new ForbiddenException("You are not a member of this group");
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
            throw new ForbiddenException("You are not a member of this group");
        }
        Expenses expense = expensesRepository.findExpenseById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find expense"));
        if (!expense.getGroup().getGroupId().equals(group.getGroupId())) {
            throw new ResourceNotFoundException("Cannot find expense");
        }
        List<ExpenseParticipants> participants = expenseParticipantsRepository.findExpenseParticipantsByExpenses(expense);
        List<ExpenseParticipantsDTO> participantDTOs = participants.stream()
                .map(p -> new ExpenseParticipantsDTO(p.getUser().getId(), p.getAmount()))
                .collect(Collectors.toList());
        return new ExpenseReturnDTO(expense.getId(), groupId, expense.getUser().getId(), expense.getUser().getName(), expense.getDescription(), expense.getAmount(), expense.getCreatedAt(), participantDTOs);
    }

    @Transactional
    public void updateExpense(UpdateExpenseDTO dto, Long groupId, Long expenseId, String callerEmail) {
        Group group = groupService.findGroupById(groupId);
        User caller = authService.findUserByEmail(callerEmail);
        if (!groupService.isGroupMember(group, caller)) {
            throw new ForbiddenException("You are not a member of this group");
        }
        Expenses expense = expensesRepository.findExpenseById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find expense"));
        if (!expense.getGroup().getGroupId().equals(group.getGroupId())) {
            throw new ResourceNotFoundException("Cannot find expense");
        }
        if (!expense.getUser().equals(caller)) {
            throw new ForbiddenException("Only the expense creator can update this expense");
        }
        validateShares(dto.getParticipants(), dto.getTotalAmount());
        List<Long> userIds = dto.getParticipants().stream()
                .map(ParticipantShareDTO::getUserId)
                .toList();
        Map<Long, User> userMap = authService.findUsersByIds(userIds);
        requireParticipantMembers(group, userMap, userIds);

        expense.setAmount(dto.getTotalAmount());
        expense.setDescription(dto.getDescription());
        expensesRepository.save(expense);

        List<ExpenseParticipants> oldParticipants = expenseParticipantsRepository.findExpenseParticipantsByExpenses(expense);
        expenseParticipantsRepository.deleteAll(oldParticipants);

        for (ParticipantShareDTO p : dto.getParticipants()) {
            ExpenseParticipants ep = new ExpenseParticipants();
            ep.setExpenses(expense);
            ep.setUser(userMap.get(p.getUserId()));
            ep.setAmount(p.getShareAmount());
            expenseParticipantsRepository.save(ep);
        }

        publishAfterCommit(groupId, new NotificationsDTO("EXPENSE_UPDATED", "An existing expense was updated", groupId));
    }

    @Transactional
    public void deleteExpense(Long groupId, Long expenseId, String callerEmail) {
        Group group = groupService.findGroupById(groupId);
        User caller = authService.findUserByEmail(callerEmail);
        Expenses expense = expensesRepository.findExpenseById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find expense"));
        if (!expense.getGroup().getGroupId().equals(group.getGroupId())) {
            throw new ResourceNotFoundException("Cannot find expense");
        }
        if (!groupService.isGroupMember(group, caller)) {
            throw new ForbiddenException("You are not a member of this group");
        }
        if (!expense.getUser().equals(caller) && !group.getOwner().equals(caller)) {
            throw new ForbiddenException("Only the expense creator or group owner can delete this expense");
        }
        List<ExpenseParticipants> participants = expenseParticipantsRepository.findExpenseParticipantsByExpenses(expense);
        expenseParticipantsRepository.deleteAll(participants);
        expensesRepository.delete(expense);
        publishAfterCommit(groupId, new NotificationsDTO("EXPENSE_DELETED", "An expense was deleted", groupId));
    }

    private void validateShares(List<ParticipantShareDTO> participants, BigDecimal totalAmount) {
        Set<Long> seen = new HashSet<>();
        for (ParticipantShareDTO participant : participants) {
            if (!seen.add(participant.getUserId())) {
                throw new ValidationException("Duplicate participant: " + participant.getUserId());
            }
        }
        BigDecimal totalShares = participants.stream()
                .map(ParticipantShareDTO::getShareAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalShares.compareTo(totalAmount) != 0) {
            throw new ValidationException("Participant shares must equal total amount");
        }
    }

    private void requireParticipantMembers(Group group, Map<Long, User> userMap, List<Long> userIds) {
        for (Long userId : userIds) {
            User participant = userMap.get(userId);
            if (participant == null) {
                throw new ResourceNotFoundException("Cannot find user: " + userId);
            }
            if (!groupService.isGroupMember(group, participant)) {
                throw new ForbiddenException("Participant is not a member of this group");
            }
        }
    }

    private void publishAfterCommit(Long groupId, NotificationsDTO notification) {
        AfterCommit.run(() -> {
            messagingTemplate.convertAndSend("/topic/group/" + groupId, notification);
            algorithm.invalidateCache(groupId);
        });
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
