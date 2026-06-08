package com.chat.aj.expensetracker.Groups;

import com.chat.aj.expensetracker.Auth.AuthService;
import com.chat.aj.expensetracker.Groups.DTOs.AddMemberResponse;
import com.chat.aj.expensetracker.Groups.DTOs.CreateGroupResponse;
import com.chat.aj.expensetracker.common.Entities.Group;
import com.chat.aj.expensetracker.common.Entities.GroupRepository;
import com.chat.aj.expensetracker.common.Entities.User;
import com.chat.aj.expensetracker.common.Exceptions.ConflictException;
import com.chat.aj.expensetracker.common.Exceptions.ResourceNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class GroupService {

    private GroupRepository groupRepository;
    private AuthService userService;

    public Group findGroupById(Long id){
        return groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find group"));
    }

    public void createGroup(CreateGroupResponse newGroup, String email) {

        User owner = userService.findUserByEmail(email);

        Group group = new Group();
        group.setCreated_at(LocalDateTime.now());
        group.setName(newGroup.getName());
        group.setOwner(owner);
        groupRepository.save(group);

    }

    public void addMemberToGroup(AddMemberResponse newMember) {

        User newUser = userService.findUserByEmail(newMember.getEmail());
        Group group = findGroupById(newMember.getGroupId());

        if(group.getMembers().contains(newUser) || group.getOwner().equals(newUser)){
            throw new ConflictException("User is already in the group");
        }

    }
}
