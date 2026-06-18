package com.chat.aj.expensetracker.Groups;

import com.chat.aj.expensetracker.Auth.AuthService;
import com.chat.aj.expensetracker.Groups.DTOs.AddMemberResponse;
import com.chat.aj.expensetracker.Groups.DTOs.CreateGroupResponse;
import com.chat.aj.expensetracker.Groups.DTOs.GroupDTO;
import com.chat.aj.expensetracker.common.Entities.*;
import com.chat.aj.expensetracker.common.Exceptions.ConflictException;
import com.chat.aj.expensetracker.common.Exceptions.ResourceNotFoundException;
import com.chat.aj.expensetracker.common.Exceptions.UnauthorizedException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

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

    public Group findGroupById(Long id){
        return groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find group"));
    }

    public GroupMembers findGroupMemberInGroup(Group group, User user){
        return groupMembersRepository.findByGroupAndMember(group, user)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find user in group"));
    }

    public boolean isGroupMember(Group group, User user){
        return group.getOwner().equals(user) ||
               groupMembersRepository.findByGroupAndMember(group, user).isPresent();
    }

    public void createGroup(CreateGroupResponse newGroup, String email) {

        User owner = userService.findUserByEmail(email);

        Group group = new Group();
        group.setCreated_at(LocalDateTime.now());
        group.setName(newGroup.getName());
        group.setOwner(owner);
        groupRepository.save(group);

    }

    public void addMemberToGroup(AddMemberResponse newMember, String email) {

        User newUser = userService.findUserByEmail(newMember.getEmail());

        Group group = findGroupById(newMember.getGroupId());
        User currentUser = userService.findUserByEmail(email);

        if(!group.getOwner().equals(currentUser)){
            throw new UnauthorizedException("You are not authorized to carry out this action");
        }


        if(groupMembersRepository.findByGroupAndMember(group, newUser).isPresent() || group.getOwner().equals(newUser)){
            throw new ConflictException("User is already in the group");
        }

        GroupMembers groupMembers = new GroupMembers();
        groupMembers.setGroup(group);
        groupMembers.setMember(newUser);
        groupMembersRepository.save(groupMembers);

    }

    @Transactional
    public GroupDTO getGroup(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        List<String> memberNames = group.getMembers().stream()
                .map(m -> m.getMember().getName())
                .collect(Collectors.toList());
        
        return new GroupDTO(group.getName(), group.getOwner().getName(), memberNames);
    }


    public List<GroupDTO> getMyGroups(String name) {
        User user = userService.findUserByEmail(name);

        List<Group> ownedGroups = groupRepository.findByOwner(user);
        List<GroupMembers> memberedGroups = groupMembersRepository.findByMember(user);

        if(ownedGroups.isEmpty() && memberedGroups.isEmpty()){
            throw new ResourceNotFoundException("This user is not in any groups");
        }
        List<GroupDTO> returnable = new ArrayList<>();
        if(!ownedGroups.isEmpty()){
            for(Group g: ownedGroups){
                List<String> memberNames = g.getMembers().stream()
                        .map(m -> m.getMember().getName())
                        .toList();
                returnable.add(new GroupDTO(g.getName(), g.getOwner().getName(), memberNames));
            }
        }

        if(!memberedGroups.isEmpty()){
            for(GroupMembers group: memberedGroups){
                Group g = group.getGroup();
                List<String> memberNames = g.getMembers().stream()
                        .map(m -> m.getMember().getName())
                        .toList();
                returnable.add(new GroupDTO(g.getName(), g.getOwner().getName(), memberNames));
            }
        }

        return returnable;
    }

    public void removeUser(Long groupId, String targetEmail, String originEmail) {
        User currentUser = userService.findUserByEmail(originEmail);
        User toRemove = userService.findUserByEmail(targetEmail);
        Group group = findGroupById(groupId);

        if(!group.getOwner().equals(currentUser)){
            throw new UnauthorizedException("Only the owner can kick a member");
        } else if(group.getOwner().equals(toRemove)) {
            throw new ConflictException("You cannot remove the owner");
        }

        GroupMembers target = findGroupMemberInGroup(group, toRemove);
        groupMembersRepository.delete(target);
    }

    public void deleteGroup(Long groupId, String email) {
        Group group = findGroupById(groupId);
        User owner = userService.findUserByEmail(email);

        if(!group.getOwner().equals(owner)){
            throw new UnauthorizedException("You are not the owner.");
        }

        List<GroupMembers> toDelete = groupMembersRepository.findByGroup(group);
        groupMembersRepository.deleteAll(toDelete);

        groupRepository.delete(group);
    }

}
