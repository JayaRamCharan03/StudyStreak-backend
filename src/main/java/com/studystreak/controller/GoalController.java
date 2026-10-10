package com.studystreak.controller;

import static com.studystreak.dto.Dtos.*;

import com.studystreak.service.AuthService;
import com.studystreak.service.StudyService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/goals")
public class GoalController {
    private final AuthService auth;
    private final StudyService study;

    public GoalController(AuthService auth, StudyService study) { this.auth = auth; this.study = study; }

    @GetMapping
    public List<GoalDto> list(Authentication a) { return study.list(auth.current(a)); }

    @PostMapping
    public GoalDto create(Authentication a, @Valid @RequestBody GoalReq r) { return study.create(auth.current(a), r); }

    @PostMapping("/{id}/checkin")
    public GoalDto checkIn(Authentication a, @PathVariable Long id, @Valid @RequestBody(required = false) CheckInReq r) {
        return study.checkIn(auth.current(a), id, r == null ? null : r.amount());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication a, @PathVariable Long id) {
        study.delete(auth.current(a), id);
        return ResponseEntity.noContent().build();
    }
}