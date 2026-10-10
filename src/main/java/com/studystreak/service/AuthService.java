package com.studystreak.service;

import static com.studystreak.dto.Dtos.*;

import com.studystreak.model.User;
import com.studystreak.repo.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtUtil jwt;
    private final StudyService study;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtUtil jwt, StudyService study) {
        this.users = users; this.encoder = encoder; this.jwt = jwt; this.study = study;
    }

    public AuthRes register(RegisterReq r) {
        String email = r.email().trim().toLowerCase();
        if (users.existsByEmail(email)) throw new ResponseStatusException(HttpStatus.CONFLICT, "That email is already registered");
        User u = new User();
        u.name = r.name().trim(); u.email = email; u.passwordHash = encoder.encode(r.password());
        users.save(u);
        return res(u);
    }

    public AuthRes login(LoginReq r) {
        User u = users.findByEmail(r.email().trim().toLowerCase())
            .filter(x -> encoder.matches(r.password(), x.passwordHash))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect email or password"));
        return res(u);
    }

    public User current(Authentication a) {
        return users.findByEmail(a.getName()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in again"));
    }

    public UserDto dto(User u) { return new UserDto(u.id, u.name, u.email, study.streakOf(u), u.longestStreak); }

    private AuthRes res(User u) { return new AuthRes(jwt.create(u.email), dto(u)); }
}
