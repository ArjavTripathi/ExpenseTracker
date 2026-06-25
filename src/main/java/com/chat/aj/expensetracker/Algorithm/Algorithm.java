package com.chat.aj.expensetracker.Algorithm;

import com.chat.aj.expensetracker.Groups.GroupService;
import com.chat.aj.expensetracker.common.Entities.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class Algorithm {
    public final ExpensesRepository expensesRepository;
    public final ExpenseParticipantsRepository expenseParticipantsRepository;
    public final GroupMembersRepository groupMembersRepository;
    public final GroupService groupService;

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



    public Graph algorithm(Map<User, BigDecimal> map){
        Graph graph = new Graph();

        PriorityQueue<Map.Entry<User, BigDecimal>> creditors =
                new PriorityQueue<>((a, b) -> b.getValue().compareTo(a.getValue()));

        PriorityQueue<Map.Entry<User, BigDecimal>> debtors =
                new PriorityQueue<>(Map.Entry.comparingByValue());

        for (Map.Entry<User, BigDecimal> entry : map.entrySet()) {
            if(entry.getValue().compareTo(BigDecimal.ZERO) > 0) {
                creditors.add(entry);
            } else if(entry.getValue().compareTo(BigDecimal.ZERO) < 0) {
                debtors.add(entry);
            }
        }

        while(!creditors.isEmpty() && !debtors.isEmpty()){
            User debtor = debtors.peek().getKey();
            User creditor = creditors.peek().getKey();
            Node d = new Node(debtor.getName());
            Node c = new Node(creditor.getName());

            if(map.get(creditor).compareTo(map.get(debtor).abs()) == 0){
                graph.addEdge(new Edge(new Node(graph, debtor.getName()), new Node(graph, creditor.getName()), map.get(creditor)));

                debtors.poll();

                creditors.poll();
            } else if(map.get(creditor).compareTo(map.get(debtor).abs()) > 0) {
                map.put(creditor, map.get(creditor).subtract(map.get(debtor).abs()));

                BigDecimal debtorBalance = debtors.poll().getValue().abs();
                BigDecimal newCreditorBalance = creditors.poll().getValue().subtract(debtorBalance);

                graph.addEdge(new Edge(new Node(graph, debtor.getName()), new Node(graph, creditor.getName()), debtorBalance));


                if (newCreditorBalance.compareTo(BigDecimal.ZERO) > 0) {
                    creditors.offer(Map.entry(creditor, newCreditorBalance));
                }
                debtors.poll();

            } else {
                map.put(debtor, map.get(debtor).add(map.get(creditor)));

                Edge e = new Edge(d, c, map.get(debtor));
                graph.addEdge(e);

                BigDecimal creditorBalance = creditors.poll().getValue();
                BigDecimal debtorBalance = debtors.poll().getValue().add(creditorBalance);

                graph.addEdge(new Edge(new Node(graph, debtor.getName()), new Node(graph, creditor.getName()), creditorBalance));

                if (debtorBalance.compareTo(BigDecimal.ZERO) < 0) {
                    debtors.offer(Map.entry(debtor, debtorBalance));
                }

                creditors.poll();
            }
        }

        return graph;
    }

}