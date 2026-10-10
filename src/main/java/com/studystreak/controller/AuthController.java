package com.studystreak.controller;

import static com.studystreak.dto.Dtos.*;

import com.studystreak.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;

    public AuthController(AuthService auth) { this.auth = auth; }

    @PostMapping("/register")
    public AuthRes register(@Valid @RequestBody RegisterReq r) { return auth.register(r); }

    @PostMapping("/login")
    public AuthRes login(@Valid @RequestBody LoginReq r) { return auth.login(r); }

    @GetMapping("/me")
    public UserDto me(Authentication a) { return auth.dto(auth.current(a)); }
}