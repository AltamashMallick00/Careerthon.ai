package com.careerthon.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ScoreBreakdown {

    private int profilePhoto;
    private int coverPhoto;
    private int headline;
    private int aboutSection;
    private int experience;
    private int education;
    private int skills;
    private int atsScore;
    private int keywordDensity;
    private int visibilityScore;
    private int recruiterMatch;
    @Column(name = "score_industry_benchmark")
    private int industryBenchmark;
    @Column(name = "score_licenses")
    private int licensesAndCertifications;
    private int recommendations;
    private int activityEngagement;

    public ScoreBreakdown() {}

    public ScoreBreakdown(int profilePhoto, int coverPhoto, int headline, int aboutSection,
                          int experience, int education, int skills, int atsScore,
                          int keywordDensity, int visibilityScore, int recruiterMatch,
                          int industryBenchmark, int licensesAndCertifications,
                          int recommendations, int activityEngagement) {
        this.profilePhoto = profilePhoto;
        this.coverPhoto = coverPhoto;
        this.headline = headline;
        this.aboutSection = aboutSection;
        this.experience = experience;
        this.education = education;
        this.skills = skills;
        this.atsScore = atsScore;
        this.keywordDensity = keywordDensity;
        this.visibilityScore = visibilityScore;
        this.recruiterMatch = recruiterMatch;
        this.industryBenchmark = industryBenchmark;
        this.licensesAndCertifications = licensesAndCertifications;
        this.recommendations = recommendations;
        this.activityEngagement = activityEngagement;
    }

    // Getters and Setters
    public int getProfilePhoto() { return profilePhoto; }
    public void setProfilePhoto(int profilePhoto) { this.profilePhoto = profilePhoto; }

    public int getCoverPhoto() { return coverPhoto; }
    public void setCoverPhoto(int coverPhoto) { this.coverPhoto = coverPhoto; }

    public int getHeadline() { return headline; }
    public void setHeadline(int headline) { this.headline = headline; }

    public int getAboutSection() { return aboutSection; }
    public void setAboutSection(int aboutSection) { this.aboutSection = aboutSection; }

    public int getExperience() { return experience; }
    public void setExperience(int experience) { this.experience = experience; }

    public int getEducation() { return education; }
    public void setEducation(int education) { this.education = education; }

    public int getSkills() { return skills; }
    public void setSkills(int skills) { this.skills = skills; }

    public int getAtsScore() { return atsScore; }
    public void setAtsScore(int atsScore) { this.atsScore = atsScore; }

    public int getKeywordDensity() { return keywordDensity; }
    public void setKeywordDensity(int keywordDensity) { this.keywordDensity = keywordDensity; }

    public int getVisibilityScore() { return visibilityScore; }
    public void setVisibilityScore(int visibilityScore) { this.visibilityScore = visibilityScore; }

    public int getRecruiterMatch() { return recruiterMatch; }
    public void setRecruiterMatch(int recruiterMatch) { this.recruiterMatch = recruiterMatch; }

    public int getIndustryBenchmark() { return industryBenchmark; }
    public void setIndustryBenchmark(int industryBenchmark) { this.industryBenchmark = industryBenchmark; }

    public int getLicensesAndCertifications() { return licensesAndCertifications; }
    public void setLicensesAndCertifications(int licensesAndCertifications) { this.licensesAndCertifications = licensesAndCertifications; }

    public int getRecommendations() { return recommendations; }
    public void setRecommendations(int recommendations) { this.recommendations = recommendations; }

    public int getActivityEngagement() { return activityEngagement; }
    public void setActivityEngagement(int activityEngagement) { this.activityEngagement = activityEngagement; }

    public String getColorFor(int score) {
        if (score >= 8) return "#10b981";
        if (score >= 5) return "#f59e0b";
        return "#ef4444";
    }
}
