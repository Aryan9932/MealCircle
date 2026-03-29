package com.mealcircle2.mealcircle2.dto;

import com.mealcircle2.mealcircle2.model.Role;
import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String password;
    private Role role;
}