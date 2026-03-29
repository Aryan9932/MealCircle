package com.mealcircle2.mealcircle2.model;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    private String email;
    private String password;
    // ✅ ADD THIS
    private Role role;   // ✅ ADD THIS

}