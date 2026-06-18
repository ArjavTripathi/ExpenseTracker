package com.chat.aj.expensetracker.Expenses;

import com.chat.aj.expensetracker.Auth.AuthService;
import com.chat.aj.expensetracker.Expenses.DTO.*;
import com.chat.aj.expensetracker.Groups.GroupService;
import com.chat.aj.expensetracker.common.Entities.*;
import com.chat.aj.expensetracker.common.Exceptions.ResourceNotFoundException;
import com.chat.aj.expensetracker.common.Exceptions.UnauthorizedException;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    private final GroupService groupService;
    private final AuthService authService;
    private final ExpenseParticipantsRepository expenseParticipantsRepository;
    private final ExpensesRepository expensesRepository;

    public void createExpense(CreateExpenseDTO dto, String callerEmail) {
        Group group = groupService.findGroupById(dto.getGroupId());
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
        expense.setCreated_at(LocalDateTime.now());
        expensesRepository.save(expense);

        for (ParticipantShareDTO p : dto.getParticipants()) {
            User participant = authService.findUserById(p.getUserId());
            ExpenseParticipants ep = new ExpenseParticipants();
            ep.setExpenses(expense);
            ep.setUser(participant);
            ep.setAmount(p.getShareAmount());
            expenseParticipantsRepository.save(ep);
        }
    }

    public List<GetExpenseDTO> getAllExpenses(Long groupId, String callerEmail) {
        Group group = groupService.findGroupById(groupId);
        User caller = authService.findUserByEmail(callerEmail);
        if (!groupService.isGroupMember(group, caller)) {
            throw new UnauthorizedException("You are not a member of this group");
        }
        List<Expenses> expenses = expensesRepository.findByGroup(group);
        return expenses.stream()
                .map(e -> new GetExpenseDTO(e.getId(), e.getDescription(), e.getAmount(), e.getUser().getName()))
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
                .map(p -> new ExpenseParticipantsDTO(p.getUser().getName(), p.getAmount()))
                .collect(Collectors.toList());
        return new ExpenseReturnDTO(expense.getUser().getName(), expense.getDescription(), expense.getAmount(), expense.getCreated_at(), participantDTOs);
    }

    @Transactional
    public void updateExpense(UpdateExpenseDTO dto, String callerEmail) {
        groupService.findGroupById(dto.getGroupId());
        User caller = authService.findUserByEmail(callerEmail);
        Expenses expense = expensesRepository.findExpenseById(dto.getExpenseId())
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

        for (ParticipantShareDTO p : dto.getParticipants()) {
            User participant = authService.findUserById(p.getUserId());
            ExpenseParticipants ep = new ExpenseParticipants();
            ep.setExpenses(expense);
            ep.setUser(participant);
            ep.setAmount(p.getShareAmount());
            expenseParticipantsRepository.save(ep);
        }
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
    }

}
