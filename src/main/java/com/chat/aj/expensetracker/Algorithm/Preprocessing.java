package com.chat.aj.expensetracker.Algorithm;

import com.chat.aj.expensetracker.Groups.GroupService;
import com.chat.aj.expensetracker.common.Entities.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class Preprocessing {
    public final ExpensesRepository expensesRepository;
    public final ExpenseParticipantsRepository expenseParticipantsRepository;
    public final GroupMembersRepository groupMembersRepository;
    public final GroupService groupService;

    public Map<User, BigDecimal> populateMap(Long groupId) {
        HashMap<User, BigDecimal> netBalances = new HashMap<>();
        Group group = groupService.findGroupById(groupId);
        List<GroupMembers> members = groupMembersRepository.findByGroup(group);
        netBalances.put(group.getOwner(), BigDecimal.ZERO);
        netBalances.putAll(members.stream()
                .map(GroupMembers::getMember)
                .collect(Collectors.toMap(u -> u, u -> BigDecimal.ZERO)));

        return netBalances;
    }

    public Map<String, BigDecimal> getBalances(Map<User, BigDecimal> map, Long groupId) {
        List<Expenses> expenses = expensesRepository.findByGroup(groupService.findGroupById(groupId));
        expenses.stream()
                .filter(exp -> exp.getAmount() != null) // Operation 1: Filter out bad data
                .forEach(exp -> {
                    map.merge(exp.getUser(), exp.getAmount(), BigDecimal::add);
                    List<ExpenseParticipants> expPart = expenseParticipantsRepository.findExpenseParticipantsByExpenses(exp);
                    if(expPart.isEmpty()) return;
                    expPart.stream()
                            .filter(ep -> ep.getUser() != null && ep.getAmount() != null)
                            .forEach(ep -> map.merge(
                                    ep.getUser(),
                                    ep.getAmount().negate(),
                                    BigDecimal::add
                            ));
                });
        //For Controller Ease of User
        return map.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().getName(), // Extracts the name as the new key
                        Map.Entry::getValue,
                        BigDecimal::add
                ));
    }

}