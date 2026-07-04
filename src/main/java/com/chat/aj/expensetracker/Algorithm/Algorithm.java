package com.chat.aj.expensetracker.Algorithm;

import com.chat.aj.expensetracker.Algorithm.DTO.SettlementDTO;
import com.chat.aj.expensetracker.common.Entities.*;
import com.chat.aj.expensetracker.common.Exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class Algorithm {
    private final ExpensesRepository expensesRepository;
    private final ExpenseParticipantsRepository expenseParticipantsRepository;
    private final GroupMembersRepository groupMembersRepository;
    private final GroupRepository groupRepository;
    private final ConcurrentHashMap<Long, List<SettlementDTO>> cache = new ConcurrentHashMap<>();

    private Group findGroupById(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find group"));
    }

    private Map<User, BigDecimal> initBalanceMap(Group group) {
        Map<User, BigDecimal> balances = new HashMap<>();
        List<GroupMembers> members = groupMembersRepository.findByGroup(group);
        balances.put(group.getOwner(), BigDecimal.ZERO);
        balances.putAll(members.stream()
                .map(GroupMembers::getMember)
                .collect(Collectors.toMap(u -> u, u -> BigDecimal.ZERO)));
        return balances;
    }

    public Map<User, BigDecimal> populateMap(Long groupId) {
        return initBalanceMap(findGroupById(groupId));
    }

    public Map<String, BigDecimal> getBalances(Map<User, BigDecimal> map, Long groupId) {
        Group group = findGroupById(groupId);
        List<Expenses> expenses = expensesRepository.findByGroup(group);

        Map<Long, List<ExpenseParticipants>> participantsByExpense = expenseParticipantsRepository
                .findByExpensesGroup(group)
                .stream()
                .collect(Collectors.groupingBy(ep -> ep.getExpenses().getId()));

        expenses.stream()
                .filter(exp -> exp.getAmount() != null)
                .forEach(exp -> {
                    map.merge(exp.getUser(), exp.getAmount(), BigDecimal::add);
                    List<ExpenseParticipants> expPart = participantsByExpense.getOrDefault(exp.getId(), List.of());
                    expPart.stream()
                            .filter(ep -> ep.getUser() != null && ep.getAmount() != null)
                            .forEach(ep -> map.merge(ep.getUser(), ep.getAmount().negate(), BigDecimal::add));
                });

        return map.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().getName(),
                        Map.Entry::getValue,
                        BigDecimal::add
                ));
    }

    public Map<User, BigDecimal> preprocess(Long groupId) {
        Group group = findGroupById(groupId);
        Map<User, BigDecimal> netBalances = initBalanceMap(group);
        List<Expenses> expenses = expensesRepository.findByGroup(group);

        Map<Long, List<ExpenseParticipants>> participantsByExpense = expenseParticipantsRepository
                .findByExpensesGroup(group)
                .stream()
                .collect(Collectors.groupingBy(ep -> ep.getExpenses().getId()));

        expenses.stream()
                .filter(exp -> exp.getAmount() != null)
                .forEach(exp -> {
                    netBalances.merge(exp.getUser(), exp.getAmount(), BigDecimal::add);
                    List<ExpenseParticipants> expPart = participantsByExpense.getOrDefault(exp.getId(), List.of());
                    expPart.stream()
                            .filter(ep -> ep.getUser() != null && ep.getAmount() != null)
                            .forEach(ep -> netBalances.merge(ep.getUser(), ep.getAmount().negate(), BigDecimal::add));
                });

        return netBalances;
    }

    public List<Edge> algorithm(Map<User, BigDecimal> map) {
        List<Edge> settlements = new ArrayList<>();

        PriorityQueue<Map.Entry<User, BigDecimal>> creditors =
                new PriorityQueue<>((a, b) -> b.getValue().compareTo(a.getValue()));

        PriorityQueue<Map.Entry<User, BigDecimal>> debtors =
                new PriorityQueue<>(Map.Entry.comparingByValue());

        for (Map.Entry<User, BigDecimal> entry : map.entrySet()) {
            if (entry.getValue().compareTo(BigDecimal.ZERO) > 0) {
                creditors.offer(entry);
            } else if (entry.getValue().compareTo(BigDecimal.ZERO) < 0) {
                debtors.offer(entry);
            }
        }

        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            Map.Entry<User, BigDecimal> creditorEntry = creditors.poll();
            Map.Entry<User, BigDecimal> debtorEntry = debtors.poll();

            User creditor = creditorEntry.getKey();
            User debtor = debtorEntry.getKey();

            BigDecimal creditorBalance = creditorEntry.getValue();
            BigDecimal debtorBalance = debtorEntry.getValue().abs();

            BigDecimal settlementAmount = creditorBalance.min(debtorBalance);

            settlements.add(new Edge(
                    new Node(debtor.getId(), debtor.getName()),
                    new Node(creditor.getId(), creditor.getName()),
                    settlementAmount
            ));

            BigDecimal newCreditorBalance = creditorBalance.subtract(settlementAmount);
            BigDecimal newDebtorBalance = debtorBalance.subtract(settlementAmount);

            if (newCreditorBalance.compareTo(BigDecimal.ZERO) > 0) {
                creditors.offer(Map.entry(creditor, newCreditorBalance));
            }

            if (newDebtorBalance.compareTo(BigDecimal.ZERO) > 0) {
                debtors.offer(Map.entry(debtor, newDebtorBalance.negate()));
            }
        }

        return settlements;
    }

    public List<SettlementDTO> getOrComputeCache(Long groupId) {
        return cache.computeIfAbsent(groupId, id -> {
            Map<User, BigDecimal> netBalances = preprocess(id);
            return algorithm(netBalances).stream()
                    .map(SettlementDTO::new)
                    .toList();
        });
    }

    public void invalidateCache(Long groupId) {
        cache.remove(groupId);
    }
}
