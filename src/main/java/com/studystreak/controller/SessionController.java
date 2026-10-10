package com.studystreak.controller;

import static com.studystreak.dto.Dtos.*;

import com.studystreak.service.AuthService;
import com.studystreak.service.StudyService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {
    private final AuthService auth;
    private final StudyService study;

    public SessionController(AuthService auth, StudyService study) { this.auth = auth; this.study = study; }

    @GetMapping
    public List<SessionDto> recent(Authentication a) { return study.recentSessions(auth.current(a)); }

    @GetMapping("/today")
    public List<SessionDto> today(Authentication a) { return study.todaySessions(auth.current(a)); }

    @PostMapping
    public SessionDto log(Authentication a, @Valid @RequestBody SessionReq r) { return study.logSession(auth.current(a), r); }
}