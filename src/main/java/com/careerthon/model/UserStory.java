package com.careerthon.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user_stories")
public class UserStory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String role;

    @Column(length = 2000)
    private String story;

    private String avatarInitials;
    private String avatarColor;
    private String avatarUrl;

    public UserStory() {}

    public UserStory(String name, String role, String story, String avatarInitials, String avatarColor, String avatarUrl) {
        this.name = name;
        this.role = role;
        this.story = story;
        this.avatarInitials = avatarInitials;
        this.avatarColor = avatarColor;
        this.avatarUrl = avatarUrl;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStory() { return story; }
    public void setStory(String story) { this.story = story; }

    public String getAvatarInitials() { return avatarInitials; }
    public void setAvatarInitials(String avatarInitials) { this.avatarInitials = avatarInitials; }

    public String getAvatarColor() { return avatarColor; }
    public void setAvatarColor(String avatarColor) { this.avatarColor = avatarColor; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}
