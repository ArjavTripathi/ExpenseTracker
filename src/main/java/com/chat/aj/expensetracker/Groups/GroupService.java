package com.chat.aj.expensetracker.Groups;

import com.chat.aj.expensetracker.Algorithm.Algorithm;
import com.chat.aj.expensetracker.Algorithm.DTO.SettlementDTO;
import com.chat.aj.expensetracker.Auth.AuthService;
import com.chat.aj.expensetracker.Groups.DTOs.*;
import com.chat.aj.expensetracker.Websockets.DTO.NotificationsDTO;
import com.chat.aj.expensetracker.common.Entities.*;
import com.chat.aj.expensetracker.common.Exceptions.ConflictException;
import com.chat.aj.expensetracker.common.Exceptions.ForbiddenException;
import com.chat.aj.expensetracker.common.Exceptions.ResourceNotFoundException;
import com.chat.aj.expensetracker.common.Utility.AfterCommit;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class GroupService {

    private GroupRepository groupRepository;
    private AuthService userService;
    private GroupMembersRepository groupMembersRepository;
    private ExpensesRepository expensesRepository;
    private ExpenseParticipantsRepository expenseParticipantsRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final Algorithm algorithm;

    public Group findGroupById(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find group"));
    }

    public GroupMembers findGroupMemberInGroup(Group group, User user) {
        return groupMembersRepository.findByGroupAndMember(group, user)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find user in group"));
    }

    public User findUserByEmail(String email){
        return userService.findUserByEmail(email);
    }

    public boolean isGroupMember(Group group, User user) {
        return group.getOwner().equals(user) ||
               groupMembersRepository.findByGroupAndMember(group, user).isPresent();
    }

    public void requireMember(Long groupId, String email) {
        Group group = findGroupById(groupId);
        User user = userService.findUserByEmail(email);
        if (!isGroupMember(group, user)) {
            throw new ForbiddenException("You are not a member of this group");
        }
    }

    private List<MemberDTO> buildMemberList(Group group) {
        List<MemberDTO> members = new ArrayList<>();
        members.add(new MemberDTO(group.getOwner().getId(), group.getOwner().getName()));
        groupMembersRepository.findByGroup(group).forEach(gm ->
                members.add(new MemberDTO(gm.getMember().getId(), gm.getMember().getName()))
        );
        return members;
    }

    private GroupDTO toGroupDTO(Group group) {
        List<MemberDTO> members = buildMemberList(group);
        List<Expenses> expenses = expensesRepository.findByGroup(group);
        BigDecimal totalSpent = expenses.stream()
                .map(Expenses::getAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new GroupDTO(group.getGroupId(), group.getName(), group.getCreatedAt(), members, expenses.size(), totalSpent);
    }

    public void createGroup(CreateGroupResponse newGroup, String email) {
        User owner = userService.findUserByEmail(email);

        Group group = new Group();
        group.setCreatedAt(LocalDateTime.now());
        group.setName(newGroup.getName());
        group.setOwner(owner);
        groupRepository.save(group);
    }

    public void addMemberToGroup(Long groupId, String memberEmail, String callerEmail) {
        User newUser = userService.findUserByEmail(memberEmail);
        Group group = findGroupById(groupId);
        User currentUser = userService.findUserByEmail(callerEmail);

        if (!group.getOwner().equals(currentUser)) {
            throw new ForbiddenException("You are not authorized to carry out this action");
        }

        if (groupMembersRepository.findByGroupAndMember(group, newUser).isPresent() || group.getOwner().equals(newUser)) {
            throw new ConflictException("User is already in the group");
        }

        GroupMembers groupMembers = new GroupMembers();
        groupMembers.setGroup(group);
        groupMembers.setMember(newUser);
        groupMembersRepository.save(groupMembers);

        messagingTemplate.convertAndSend(
                "/topic/group/" + groupId,
                new NotificationsDTO("MEMBER_ADDED", "A new member was added to the group", groupId)
        );
    }

    @Transactional
    public GroupDTO getGroup(Long groupId, String email) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
        if(isGroupMember(group, userService.findUserByEmail(email))) {
            return toGroupDTO(group);
        } else {
            throw new ForbiddenException("You are not a member of this group");
        }
    }

    @Transactional
    public List<GroupDTO> getMyGroups(String email) {
        User user = userService.findUserByEmail(email);
        List<Group> groups = groupRepository.findByMembers_MemberOrOwner(user, user);
        return groups.stream().map(this::toGroupDTO).collect(Collectors.toList());
    }

    public void removeUser(Long groupId, Long memberId, String originEmail) {
        User currentUser = userService.findUserByEmail(originEmail);
        User toRemove = userService.findUserById(memberId);
        Group group = findGroupById(groupId);

        if (!group.getOwner().equals(currentUser)) {
            throw new ForbiddenException("Only the owner can kick a member");
        } else if (group.getOwner().equals(toRemove)) {
            throw new ConflictException("You cannot remove the owner");
        }

        GroupMembers target = findGroupMemberInGroup(group, toRemove);
        groupMembersRepository.delete(target);
        messagingTemplate.convertAndSend(
                "/topic/group/" + groupId,
                new NotificationsDTO("MEMBER_REMOVED", "A member was removed from the group", groupId)
        );
    }

    @Transactional
    public void deleteGroup(Long groupId, String email) {
        Group group = findGroupById(groupId);
        User owner = userService.findUserByEmail(email);

        if (!group.getOwner().equals(owner)) {
            throw new ForbiddenException("You are not the owner.");
        }

        List<Expenses> expenses = expensesRepository.findByGroup(group);
        for (Expenses expense : expenses) {
            expenseParticipantsRepository.deleteAll(
                    expenseParticipantsRepository.findExpenseParticipantsByExpenses(expense));
        }
        expensesRepository.deleteAll(expenses);

        List<GroupMembers> toDelete = groupMembersRepository.findByGroup(group);
        groupMembersRepository.deleteAll(toDelete);
        groupRepository.delete(group);

        AfterCommit.run(() -> {
            messagingTemplate.convertAndSend(
                    "/topic/group/" + groupId,
                    new NotificationsDTO("GROUP_DELETED", "The group was deleted", groupId)
            );
            algorithm.invalidateCache(groupId);
        });
    }

    public List<FriendSettlementsDTO> getFriendSettlements(String name) {
        User user = userService.findUserByEmail(name);
        List<Group> allGroups = groupRepository.findByMembers_MemberOrOwner(user, user);
        List<FriendSettlementsDTO> friends = new ArrayList<>();

        Map<Long, BigDecimal> nets = new LinkedHashMap<>();
        Map<Long, String> names = new LinkedHashMap<>();
        for (Group g : allGroups) {
            for (SettlementDTO s : algorithm.getOrComputeCache(g.getGroupId())) {
                if (s.getOwed().getId().equals(user.getId())) {
                    nets.merge(s.getOwer().getId(), s.getAmount(), BigDecimal::add);
                    names.putIfAbsent(s.getOwer().getId(), s.getOwer().getName());
                } else if (s.getOwer().getId().equals(user.getId())) {
                    nets.merge(s.getOwed().getId(), s.getAmount().negate(), BigDecimal::add);
                    names.putIfAbsent(s.getOwed().getId(), s.getOwed().getName());
                }
            }
        }

        nets.forEach((friendId, amount) -> {
            if (amount.compareTo(BigDecimal.ZERO) != 0) {
                friends.add(new FriendSettlementsDTO(names.get(friendId), amount));
            }
        });
        return friends;
    }
}
