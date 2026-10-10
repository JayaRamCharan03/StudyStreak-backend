package com.studystreak.security;

import com.studystreak.service.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Reads "Authorization: Bearer <token>" and marks the request as logged in as that email. */
@Component
public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtil jwt;

    public JwtFilter(JwtUtil jwt) { this.jwt = jwt; }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                String email = jwt.parse(header.substring(7));
                SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(email, null, List.of()));
            } catch (Exception ignored) {
                // bad or expired token: the request stays logged out and gets a 401
            }
        }
        chain.doFilter(req, res);
    }
}