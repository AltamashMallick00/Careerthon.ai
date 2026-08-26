package com.careerthon.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lms_assignment_submissions")
public class LmsAssignmentSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private LmsAssignment assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User student;

    private String sourceCodeUrl;
    private String pdfUrl;
    private String projectFileUrl;

    @Column(nullable = false)
    private LocalDateTime submittedAt = LocalDateTime.now();

    @Column(nullable = false)
    private String status = "SUBMITTED"; // SUBMITTED, GRADED

    private Integer marks;
    private String feedback;

    public LmsAssignmentSubmission() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LmsAssignment getAssignment() { return assignment; }
    public void setAssignment(LmsAssignment assignment) { this.assignment = assignment; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public String getSourceCodeUrl() { return sourceCodeUrl; }
    public void setSourceCodeUrl(String sourceCodeUrl) { this.sourceCodeUrl = sourceCodeUrl; }

    public String getPdfUrl() { return pdfUrl; }
    public void setPdfUrl(String pdfUrl) { this.pdfUrl = pdfUrl; }

    public String getProjectFileUrl() { return projectFileUrl; }
    public void setProjectFileUrl(String projectFileUrl) { this.projectFileUrl = projectFileUrl; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getMarks() { return marks; }
    public void setMarks(Integer marks) { this.marks = marks; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
}
