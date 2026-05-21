package com.mealcircle2.mealcircle2.service;

import com.mealcircle2.mealcircle2.dto.*;
import com.mealcircle2.mealcircle2.model.User;
import com.mealcircle2.mealcircle2.repository.UserRepository;
import com.mealcircle2.mealcircle2.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository repo, PasswordEncoder encoder, JwtUtil jwtUtil) {
        this.repo = repo;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
    }

    public String register(RegisterRequest req) {
        if (repo.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = User.builder()
                .email(req.getEmail())
                .password(encoder.encode(req.getPassword()))
                .role(req.getRole())
                .build();

        repo.save(user);
        return jwtUtil.generateToken(user.getEmail());
    }

    public String login(AuthRequest req) {
        User user = repo.findFirstByEmailOrderByIdDesc(req.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!encoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return jwtUtil.generateToken(user.getEmail());
    }
}