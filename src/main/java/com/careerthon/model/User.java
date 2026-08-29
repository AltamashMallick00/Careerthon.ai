package com.careerthon.model;

import jakarta.persistence.*;

@Entity
@Table(name = "app_users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String roles; // Store as comma-separated: ROLE_USER, ROLE_ADMIN

    private String fullName;

    private String email;

    private String phoneNumber;

    private boolean emailVerified = false;

    private String authProvider = "LOCAL"; // LOCAL, GOOGLE

    @Column(nullable = false)
    private boolean enabled = true;

    public User() {}

    public User(String username, String password, String roles, String fullName) {
        this.username = username;
        this.password = password;
        this.roles = roles;
        this.fullName = fullName;
    }

    public User(String username, String password, String roles, String fullName, String email, String phoneNumber) {
        this.username = username;
        this.password = password;
        this.roles = roles;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRoles() { return roles; }
    public void setRoles(String roles) { this.roles = roles; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }

    public String getAuthProvider() { return authProvider; }
    public void setAuthProvider(String authProvider) { this.authProvider = authProvider; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
