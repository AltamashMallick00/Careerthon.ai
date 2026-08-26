package com.careerthon.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lms_notifications")
public class LmsNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // Null means broadcast to everyone
    private User targetUser;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    private String type; // COURSE_UPDATE, ASSIGNMENT_REMINDER, INTERNSHIP_UPDATE, CERTIFICATE

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private boolean isRead = false;

    public LmsNotification() {}

    public LmsNotification(User targetUser, String title, String message, String type) {
        this.targetUser = targetUser;
        this.title = title;
        this.message = message;
        this.type = type;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getTargetUser() { return targetUser; }
    public void setTargetUser(User targetUser) { this.targetUser = targetUser; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { this.isRead = read; }
}
