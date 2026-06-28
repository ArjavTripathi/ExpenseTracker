package com.chat.aj.expensetracker.Algorithm;

import com.chat.aj.expensetracker.Algorithm.DTO.SettlementDTO;
import com.chat.aj.expensetracker.Groups.GroupService;
import com.chat.aj.expensetracker.common.Entities.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class Algorithm {
    public final ExpensesRepository expensesRepository;
    public final ExpenseParticipantsRepository expenseParticipantsRepository;
    public final GroupMembersRepository groupMembersRepository;
    public final GroupService groupService;
    private final ConcurrentHashMap<Long, List<SettlementDTO>> cache = new ConcurrentHashMap<>();

    /*
    Modular testing class for checking if hashmap populates group members correctly
     */
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
    /*
    Modular testing class to check if hashmap updates balances correctly
     */
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
        //For Ease of User while testing
        return map.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().getName(), // Extracts the name as the new key
                        Map.Entry::getValue,
                        BigDecimal::add
                ));
    }

    public Map<User, BigDecimal> preprocess(Long groupId){

        HashMap<User, BigDecimal> netBalances = new HashMap<>();
        List<Expenses> expenses = expensesRepository.findByGroup(groupService.findGroupById(groupId));

        Group group = groupService.findGroupById(groupId);
        List<GroupMembers> members = groupMembersRepository.findByGroup(group);
        netBalances.put(group.getOwner(), BigDecimal.ZERO);
        netBalances.putAll(members.stream()
                .map(GroupMembers::getMember)
                .collect(Collectors.toMap(u -> u, u -> BigDecimal.ZERO)));


        expenses.stream()
                .filter(exp -> exp.getAmount() != null) // Operation 1: Filter out bad data
                .forEach(exp -> {
                    netBalances.merge(exp.getUser(), exp.getAmount(), BigDecimal::add);
                    List<ExpenseParticipants> expPart = expenseParticipantsRepository.findExpenseParticipantsByExpenses(exp);
                    if(expPart.isEmpty()) return;
                    expPart.stream()
                            .filter(ep -> ep.getUser() != null && ep.getAmount() != null)
                            .forEach(ep -> netBalances.merge(
                                    ep.getUser(),
                                    ep.getAmount().negate(),
                                    BigDecimal::add
                            ));
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
                    new Node(debtor.getName()),
                    new Node(creditor.getName()),
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

    public List<SettlementDTO> getOrComputeCache(Long groupId){
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