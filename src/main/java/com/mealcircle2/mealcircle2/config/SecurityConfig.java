package com.mealcircle2.mealcircle2.config;

import com.mealcircle2.mealcircle2.service.CustomUserDetailsService;
import com.mealcircle2.mealcircle2.util.JwtUtil;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

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
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/mess/all", "/api/mess/{id}", "/api/mess/nearby").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/mess/*/reviews", "/api/mess/*/rating").permitAll()
                        .requestMatchers("/api/mess/create").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/mess/*/reviews").hasRole("CUSTOMER")
                        .requestMatchers("/api/mess/*/join").hasRole("CUSTOMER")
                        .requestMatchers("/api/payment/razorpay/**").hasRole("CUSTOMER")
                        .requestMatchers("/api/mess/customer/my-mess").hasRole("CUSTOMER")
                        .requestMatchers("/api/mess/customer/best-rated").hasRole("CUSTOMER")
                        .requestMatchers("/api/mess/owner/menu-notice").hasRole("OWNER")
                        .requestMatchers("/api/mess/update/**", "/api/mess/delete/**").hasRole("OWNER")
                        .anyRequest().authenticated()

                )
                .addFilterBefore(new JwtAuthFilter(jwtUtil, userService),
                        org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "http://127.0.0.1:*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}