package com.careerthon.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "internship_tasks")
public class InternshipTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internship_id", nullable = false)
    private Internship internship;

    @Column(nullable = false)
    private String title;

    @Column(length = 3000)
    private String description;

    @Column(nullable = false)
    private LocalDateTime deadline;

    public InternshipTask() {}

    public InternshipTask(Internship internship, String title, String description, LocalDateTime deadline) {
        this.internship = internship;
        this.title = title;
        this.description = description;
        this.deadline = deadline;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Internship getInternship() { return internship; }
    public void setInternship(Internship internship) { this.internship = internship; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
}
