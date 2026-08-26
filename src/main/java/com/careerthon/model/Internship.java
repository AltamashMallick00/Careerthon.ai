package com.careerthon.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "internships")
public class Internship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private String title;

    @Column(length = 3000)
    private String description;

    private String guidelinesPdfUrl;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "internship", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InternshipTask> tasks = new ArrayList<>();

    public Internship() {}

    public Internship(Course course, String title, String description, String guidelinesPdfUrl) {
        this.course = course;
        this.title = title;
        this.description = description;
        this.guidelinesPdfUrl = guidelinesPdfUrl;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getGuidelinesPdfUrl() { return guidelinesPdfUrl; }
    public void setGuidelinesPdfUrl(String guidelinesPdfUrl) { this.guidelinesPdfUrl = guidelinesPdfUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<InternshipTask> getTasks() { return tasks; }
    public void setTasks(List<InternshipTask> tasks) { this.tasks = tasks; }
}
