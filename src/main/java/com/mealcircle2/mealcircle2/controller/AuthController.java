package com.mealcircle2.mealcircle2.controller;

import com.mealcircle2.mealcircle2.dto.*;
import com.mealcircle2.mealcircle2.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest req) {
        return new AuthResponse(service.register(req));
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest req) {
        return new AuthResponse(service.login(req));
    }
}