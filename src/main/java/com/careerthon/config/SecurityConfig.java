package com.careerthon.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Public Marketing, Static Assets & Authentication Endpoints
                .requestMatchers(
                    "/", "/index.html", "/about", "/careers", "/features", "/blog/**", "/sitemap.xml", "/error",
                    "/login", "/signup", "/api/auth/**",
                    "/css/**", "/js/**", "/images/**", "/favicon.ico",
                    "/health", "/actuator/health", "/h2-console/**"
                ).permitAll()

                // Admin Only Features
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // LMS & Student Portal
                .requestMatchers("/student/**", "/lms/**").hasAnyRole("STUDENT", "USER", "ADMIN")

                // Protected Core SaaS & AI Features (Require Authentication)
                .requestMatchers(
                    "/review/**",
                    "/report/**",
                    "/resume/**",
                    "/job-match", "/api/job-match/**",
                    "/ai-tools/**", "/recruiter-outreach/**", "/outreach/**", "/roadmap/**", "/referral-finder/**", "/referrals/**", "/referral/**",
                    "/api/ai/**",
                    "/user/**"
                ).hasAnyRole("STUDENT", "USER", "ADMIN")

                // All other requests must be authenticated
                .anyRequest().authenticated()
            )
            .formLogin(login -> login
                .loginPage("/login")
                .successHandler(new CustomAuthenticationSuccessHandler())
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            )
            .csrf(csrf -> csrf.disable()) // Only for simplicity in this demo/H2
            .headers(headers -> headers.frameOptions(f -> f.disable())); // Needed for H2 console

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
