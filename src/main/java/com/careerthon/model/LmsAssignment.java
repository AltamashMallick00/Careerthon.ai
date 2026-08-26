package com.careerthon.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lms_assignments")
public class LmsAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private String title;

    @Column(length = 3000)
    private String description;

    private String evaluationCriteriaPdfUrl;

    @Column(nullable = false)
    private LocalDateTime dueDate;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LmsAssignmentSubmission> submissions = new ArrayList<>();

    public LmsAssignment() {}

    public LmsAssignment(Course course, String title, String description, String evaluationCriteriaPdfUrl, LocalDateTime dueDate) {
        this.course = course;
        this.title = title;
        this.description = description;
        this.evaluationCriteriaPdfUrl = evaluationCriteriaPdfUrl;
        this.dueDate = dueDate;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getEvaluationCriteriaPdfUrl() { return evaluationCriteriaPdfUrl; }
    public void setEvaluationCriteriaPdfUrl(String evaluationCriteriaPdfUrl) { this.evaluationCriteriaPdfUrl = evaluationCriteriaPdfUrl; }

    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<LmsAssignmentSubmission> getSubmissions() { return submissions; }
    public void setSubmissions(List<LmsAssignmentSubmission> submissions) { this.submissions = submissions; }
}
