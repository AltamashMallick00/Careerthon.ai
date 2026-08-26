package com.careerthon.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "app_profile_reviews")
public class ProfileReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String linkedinUrl;

    private String userName;
    private String userTitle;
    private String suggestedRoles;

    @Column(nullable = false)
    private int overallScore;

    @Enumerated(EnumType.STRING)
    private ReviewStatus status = ReviewStatus.PENDING;

    @Embedded
    private ScoreBreakdown scoreBreakdown;

    @Column(length = 5000)
    private String headlineRecommendation;

    @Column(length = 5000)
    private String aboutRecommendation;

    @Column(length = 5000)
    private String skillsRecommendation;

    @Column(length = 5000)
    private String experienceRecommendation;

    @Column(length = 5000)
    private String visibilityRecommendation;

    @Column(length = 5000)
    private String atsRecommendation;

    @Column(length = 5000)
    private String keywordRecommendation;

    @Column(length = 5000)
    private String recruiterRecommendation;

    @Column(length = 5000)
    private String industryBenchmark;

    @Column(length = 5000)
    private String actionableInsights;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime completedAt;

    private boolean emailSent = false;
    private String emailAddress;
    
    @Column(length = 2000)
    private String adminSuggestions;

    public enum ReviewStatus {
        PENDING, ANALYZING, COMPLETED, FAILED
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLinkedinUrl() { return linkedinUrl; }
    public void setLinkedinUrl(String linkedinUrl) { this.linkedinUrl = linkedinUrl; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserTitle() { return userTitle; }
    public void setUserTitle(String userTitle) { this.userTitle = userTitle; }

    public String getSuggestedRoles() { return suggestedRoles; }
    public void setSuggestedRoles(String suggestedRoles) { this.suggestedRoles = suggestedRoles; }

    public int getOverallScore() { return overallScore; }
    public void setOverallScore(int overallScore) { this.overallScore = overallScore; }

    public ReviewStatus getStatus() { return status; }
    public void setStatus(ReviewStatus status) { this.status = status; }

    public ScoreBreakdown getScoreBreakdown() { return scoreBreakdown; }
    public void setScoreBreakdown(ScoreBreakdown scoreBreakdown) { this.scoreBreakdown = scoreBreakdown; }

    public String getHeadlineRecommendation() { return headlineRecommendation; }
    public void setHeadlineRecommendation(String headlineRecommendation) { this.headlineRecommendation = headlineRecommendation; }

    public String getAboutRecommendation() { return aboutRecommendation; }
    public void setAboutRecommendation(String aboutRecommendation) { this.aboutRecommendation = aboutRecommendation; }

    public String getSkillsRecommendation() { return skillsRecommendation; }
    public void setSkillsRecommendation(String skillsRecommendation) { this.skillsRecommendation = skillsRecommendation; }

    public String getExperienceRecommendation() { return experienceRecommendation; }
    public void setExperienceRecommendation(String experienceRecommendation) { this.experienceRecommendation = experienceRecommendation; }

    public String getVisibilityRecommendation() { return visibilityRecommendation; }
    public void setVisibilityRecommendation(String visibilityRecommendation) { this.visibilityRecommendation = visibilityRecommendation; }

    public String getAtsRecommendation() { return atsRecommendation; }
    public void setAtsRecommendation(String atsRecommendation) { this.atsRecommendation = atsRecommendation; }

    public String getKeywordRecommendation() { return keywordRecommendation; }
    public void setKeywordRecommendation(String keywordRecommendation) { this.keywordRecommendation = keywordRecommendation; }

    public String getRecruiterRecommendation() { return recruiterRecommendation; }
    public void setRecruiterRecommendation(String recruiterRecommendation) { this.recruiterRecommendation = recruiterRecommendation; }

    public String getIndustryBenchmark() { return industryBenchmark; }
    public void setIndustryBenchmark(String industryBenchmark) { this.industryBenchmark = industryBenchmark; }

    public String getActionableInsights() { return actionableInsights; }
    public void setActionableInsights(String actionableInsights) { this.actionableInsights = actionableInsights; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public boolean isEmailSent() { return emailSent; }
    public void setEmailSent(boolean emailSent) { this.emailSent = emailSent; }

    public String getEmailAddress() { return emailAddress; }
    public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }

    public String getAdminSuggestions() { return adminSuggestions; }
    public void setAdminSuggestions(String adminSuggestions) { this.adminSuggestions = adminSuggestions; }

    public String getScoreLabel() {
        if (overallScore >= 80) return "Excellent";
        if (overallScore >= 60) return "Good";
        if (overallScore >= 40) return "Average";
        return "Needs Improvement";
    }

    public String getScoreColor() {
        if (overallScore >= 80) return "#10b981";
        if (overallScore >= 60) return "#0A66C2";
        if (overallScore >= 40) return "#f59e0b";
        return "#ef4444";
    }
}
