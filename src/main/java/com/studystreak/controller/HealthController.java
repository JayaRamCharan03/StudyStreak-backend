package com.studystreak.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }

    @GetMapping("/")
    public Map<String, String> root() {
        return Map.of("status", "UP", "service", "StudyStreak Backend");
    }
}
