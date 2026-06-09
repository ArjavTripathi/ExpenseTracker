package com.chat.aj.expensetracker.Groups;

import com.chat.aj.expensetracker.Groups.DTOs.AddMemberResponse;
import com.chat.aj.expensetracker.Groups.DTOs.CreateGroupResponse;
import com.chat.aj.expensetracker.Groups.DTOs.GroupDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/group")
public class GroupController {
    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<String> createGroup(@RequestBody CreateGroupResponse newGroup, Principal principal){
        groupService.createGroup(newGroup, principal.getName());
        return ResponseEntity.ok("Success!");
    }

    @PutMapping
    public ResponseEntity<String> addMember(@RequestBody AddMemberResponse newMember, Principal principal){
        groupService.addMemberToGroup(newMember, principal.getName());
        return ResponseEntity.ok("Success!");
    }

    @GetMapping
    public ResponseEntity<GroupDTO> getGroup(@RequestParam Long groupId){
        GroupDTO group = groupService.getGroup(groupId);
        return ResponseEntity.ok(group);
    }

    @GetMapping("/me")
    public ResponseEntity<List<GroupDTO>> getMyGroups(Principal principal){
        List<GroupDTO> group = groupService.getMyGroups(principal.getName());
        return ResponseEntity.ok(group);
    }

    @DeleteMapping("/member")
    public ResponseEntity<String> removeMember(@RequestParam Long groupId, @RequestParam String email, Principal principal){
        groupService.removeUser(groupId, email, principal.getName());
        return ResponseEntity.ok("Success!");
    }


}
