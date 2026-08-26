package com.careerthon.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "internship_submissions")
public class InternshipSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private InternshipTask task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User student;

    private String fileUrl; // Project zip or document
    private String liveLink; // Deployed project link if applicable

    @Column(nullable = false)
    private LocalDateTime submittedAt = LocalDateTime.now();

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED

    @Column(length = 1000)
    private String remarks;

    public InternshipSubmission() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public InternshipTask getTask() { return task; }
    public void setTask(InternshipTask task) { this.task = task; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getLiveLink() { return liveLink; }
    public void setLiveLink(String liveLink) { this.liveLink = liveLink; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
