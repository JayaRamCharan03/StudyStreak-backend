package com.studystreak.controller;

import static com.studystreak.dto.Dtos.*;

import com.studystreak.service.AuthService;
import com.studystreak.service.DashboardService;
import com.studystreak.service.GroupService;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class DashboardController {
    private final AuthService auth;
    private final DashboardService dashboard;
    private final GroupService groups;

    public DashboardController(AuthService auth, DashboardService dashboard, GroupService groups) {
        this.auth = auth; this.dashboard = dashboard; this.groups = groups;
    }

    @GetMapping("/dashboard")
    public DashboardDto dashboard(Authentication a) { return dashboard.build(auth.current(a)); }

    /** scope = "groups" (people in your groups) or anything else for everyone */
    @GetMapping("/leaderboard")
    public List<LeaderDto> leaderboard(Authentication a, @RequestParam(defaultValue = "global") String scope) {
        return groups.leaderboard(auth.current(a), scope);
    }

    @GetMapping("/health")
    public Map<String, String> health() { return Map.of("status", "ok"); }
}