package com.mealcircle2.mealcircle2.config;

import com.mealcircle2.mealcircle2.service.CustomUserDetailsService;
import com.mealcircle2.mealcircle2.util.JwtUtil;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService userService;
    private final JwtUtil jwtUtil;

    public SecurityConfig(CustomUserDetailsService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/mess/all", "/api/mess/{id}").permitAll()
                        .requestMatchers("/api/mess/create").permitAll()
                        .requestMatchers("/api/mess/update/**", "/api/mess/delete/**").hasRole("OWNER")
                        .anyRequest().authenticated()


                )
                .addFilterBefore(new JwtAuthFilter(jwtUtil, userService),
                        org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}