package com.careerthon.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "app_resume_reviews")
public class ResumeReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    private String userName;
    private String userEmail;

    @Column(nullable = false)
    private int atsScore;

    @Column(length = 2000)
    private String suggestedRoles;

    @Column(length = 5000)
    private String improvementSuggestions;

    private LocalDateTime uploadedAt = LocalDateTime.now();
    
    @Column(length = 2000)
    private String adminSuggestions;

    @Lob
    @Column(name = "file_data", length = 10000000) // Up to 10MB
    private byte[] fileData;

    public ResumeReview() {}

    public ResumeReview(String fileName, String userName, String userEmail, int atsScore, String suggestedRoles, String improvementSuggestions, byte[] fileData) {
        this.fileName = fileName;
        this.userName = userName;
        this.userEmail = userEmail;
        this.atsScore = atsScore;
        this.suggestedRoles = suggestedRoles;
        this.improvementSuggestions = improvementSuggestions;
        this.fileData = fileData;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public int getAtsScore() { return atsScore; }
    public void setAtsScore(int atsScore) { this.atsScore = atsScore; }

    public String getSuggestedRoles() { return suggestedRoles; }
    public void setSuggestedRoles(String suggestedRoles) { this.suggestedRoles = suggestedRoles; }

    public String getImprovementSuggestions() { return improvementSuggestions; }
    public void setImprovementSuggestions(String improvementSuggestions) { this.improvementSuggestions = improvementSuggestions; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
    
    public String getAdminSuggestions() { return adminSuggestions; }
    public void setAdminSuggestions(String adminSuggestions) { this.adminSuggestions = adminSuggestions; }
    
    public byte[] getFileData() { return fileData; }
    public void setFileData(byte[] fileData) { this.fileData = fileData; }
    
    public String getScoreColor() {
        if (atsScore >= 80) return "#10b981";
        if (atsScore >= 60) return "#0A66C2";
        if (atsScore >= 40) return "#f59e0b";
        return "#ef4444";
    }
}
