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
                // Public Marketing, Scanning Tools, Static Assets & Authentication Endpoints
                .requestMatchers(
                    "/", "/index.html", "/about", "/careers", "/features", "/blog/**", "/sitemap.xml", "/error",
                    "/login", "/signup", "/api/auth/**",
                    "/css/**", "/js/**", "/images/**", "/favicon.ico",
                    "/health", "/actuator/health", "/h2-console/**",
                    "/review", "/review/**",
                    "/report", "/report/**",
                    "/resume", "/resume/**",
                    "/job-match", "/job-match/**",
                    "/ai-tools/**", "/recruiter-outreach/**", "/outreach/**", "/roadmap/**", "/referral-finder/**", "/referrals/**", "/referral/**",
                    "/lms", "/lms/", "/lms/course/**",
                    "/api/**"
                ).permitAll()

                // Admin Only Features
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // Student Portal & User Dashboard (Strictly Requires Login)
                .requestMatchers("/student/**", "/user/**").hasAnyRole("STUDENT", "USER", "ADMIN")

                // All other requests must be authenticated
                .anyRequest().authenticated()
            )
            .formLogin(login -> login
                .loginPage("/login")
                .successHandler(new CustomAuthenticationSuccessHandler())
                .permitAll()
            )
            .rememberMe(remember -> remember
                .key("careerthon-secure-remember-me-key-2026")
                .tokenValiditySeconds(86400 * 30) // 30 days persistent login
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
