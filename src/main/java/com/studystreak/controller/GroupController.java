package com.studystreak.controller;

import static com.studystreak.dto.Dtos.*;

import com.studystreak.service.AuthService;
import com.studystreak.service.GroupService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/groups")
public class GroupController {
    private final AuthService auth;
    private final GroupService groups;

    public GroupController(AuthService auth, GroupService groups) { this.auth = auth; this.groups = groups; }

    @GetMapping
    public List<GroupDto> mine(Authentication a) { return groups.mine(auth.current(a)); }

    @PostMapping
    public GroupDto create(Authentication a, @Valid @RequestBody GroupReq r) { return groups.create(auth.current(a), r.name()); }

    @PostMapping("/join")
    public GroupDto join(Authentication a, @Valid @RequestBody JoinReq r) { return groups.join(auth.current(a), r.code()); }

    @GetMapping("/{id}")
    public GroupDetailDto detail(Authentication a, @PathVariable Long id) { return groups.detail(auth.current(a), id); }
}