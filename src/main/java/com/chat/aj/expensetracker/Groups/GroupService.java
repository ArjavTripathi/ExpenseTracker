package com.chat.aj.expensetracker.Groups;

import com.chat.aj.expensetracker.Algorithm.Algorithm;
import com.chat.aj.expensetracker.Algorithm.DTO.SettlementDTO;
import com.chat.aj.expensetracker.Auth.AuthService;
import com.chat.aj.expensetracker.Groups.DTOs.*;
import com.chat.aj.expensetracker.Websockets.DTO.NotificationsDTO;
import com.chat.aj.expensetracker.common.Entities.*;
import com.chat.aj.expensetracker.common.Exceptions.ConflictException;
import com.chat.aj.expensetracker.common.Exceptions.ResourceNotFoundException;
import com.chat.aj.expensetracker.common.Exceptions.UnauthorizedException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class GroupService {

    private GroupRepository groupRepository;
    private AuthService userService;
    private GroupMembersRepository groupMembersRepository;
    private ExpensesRepository expensesRepository;
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

    public boolean isGroupMember(Group group, User user) {
        return group.getOwner().equals(user) ||
               groupMembersRepository.findByGroupAndMember(group, user).isPresent();
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
            throw new UnauthorizedException("You are not authorized to carry out this action");
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
    public GroupDTO getGroup(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
        return toGroupDTO(group);
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
            throw new UnauthorizedException("Only the owner can kick a member");
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

    public void deleteGroup(Long groupId, String email) {
        Group group = findGroupById(groupId);
        User owner = userService.findUserByEmail(email);

        if (!group.getOwner().equals(owner)) {
            throw new UnauthorizedException("You are not the owner.");
        }
        messagingTemplate.convertAndSend(
                "/topic/group/" + groupId,
                new NotificationsDTO("GROUP_DELETED", "The group was deleted", groupId)
        );
        List<GroupMembers> toDelete = groupMembersRepository.findByGroup(group);
        groupMembersRepository.deleteAll(toDelete);
        groupRepository.delete(group);
    }

    public List<FriendSettlementsDTO> getFriendSettlements(String name) {
        User user = userService.findUserByEmail(name);
        List<Group> allGroups = groupRepository.findByMembers_MemberOrOwner(user, user);
        List<FriendSettlementsDTO> friends = new ArrayList<>();

        List<SettlementDTO> settlements = new ArrayList<>();
        for (Group g : allGroups) {
            settlements.addAll(algorithm.getOrComputeCache(g.getGroupId()));
        }
        for (SettlementDTO s : settlements) {
            if (s.getOwed().getName().equals(user.getName())) {
                friends.add(new FriendSettlementsDTO(s.getOwer().getName(), s.getAmount()));
            } else if (s.getOwer().getName().equals(user.getName())) {
                friends.add(new FriendSettlementsDTO(s.getOwed().getName(), s.getAmount()));
            }
        }

        return friends;
    }
}
