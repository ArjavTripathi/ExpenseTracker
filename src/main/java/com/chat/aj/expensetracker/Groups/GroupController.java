package com.chat.aj.expensetracker.Groups;

import com.chat.aj.expensetracker.Groups.DTOs.AddMemberResponse;
import com.chat.aj.expensetracker.Groups.DTOs.CreateGroupResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

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
    public ResponseEntity<String> addMember(@RequestBody AddMemberResponse newMember){
        groupService.addMemberToGroup(newMember);
        return ResponseEntity.ok("Success!");
    }
}
