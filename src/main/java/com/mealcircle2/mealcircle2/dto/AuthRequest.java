package com.mealcircle2.mealcircle2.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String email;
    private String password;
}