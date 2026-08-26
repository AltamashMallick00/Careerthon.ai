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
                .requestMatchers("/", "/index.html", "/about", "/careers", "/login", "/signup", "/error", "/css/**", "/js/**", "/images/**", "/favicon.ico", "/job-match", "/api/job-match", "/blog/**", "/sitemap.xml", "/features", "/review/**", "/resume/**", "/report/**", "/ai-tools/**", "/ai-tools/recruiter-outreach/**", "/ai-tools/roadmap/**", "/ai-tools/referral-finder/**").permitAll()
                .requestMatchers("/api/auth/**", "/h2-console/**", "/health", "/actuator/health").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/student/**").hasAnyRole("STUDENT", "USER", "ADMIN")
                .requestMatchers("/lms/**").hasAnyRole("STUDENT", "USER", "ADMIN")
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
