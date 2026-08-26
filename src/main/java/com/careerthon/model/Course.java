package com.careerthon.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 3000)
    private String description;

    @Column(nullable = false)
    private String instructor;

    private String thumbnailUrl;

    @Column(nullable = false)
    private String difficulty; // Beginner, Intermediate, Advanced

    @Column(nullable = false)
    private String category; // e.g. "Java Full Stack"

    private String totalDuration; // e.g. "45+ Hours"

    private int totalLectures;

    @Column(length = 2000)
    private String prerequisites;

    @Column(length = 2000)
    private String learningOutcomes;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("moduleOrder ASC, lectureOrder ASC")
    private List<Lecture> lectures = new ArrayList<>();

    public Course() {}

    public Course(String title, String description, String instructor, String thumbnailUrl,
                  String difficulty, String category, String totalDuration, int totalLectures) {
        this.title = title;
        this.description = description;
        this.instructor = instructor;
        this.thumbnailUrl = thumbnailUrl;
        this.difficulty = difficulty;
        this.category = category;
        this.totalDuration = totalDuration;
        this.totalLectures = totalLectures;
    }

    public Course(String title, String description, String instructor, String thumbnailUrl,
                  String difficulty, String category, String totalDuration, int totalLectures,
                  String prerequisites, String learningOutcomes) {
        this.title = title;
        this.description = description;
        this.instructor = instructor;
        this.thumbnailUrl = thumbnailUrl;
        this.difficulty = difficulty;
        this.category = category;
        this.totalDuration = totalDuration;
        this.totalLectures = totalLectures;
        this.prerequisites = prerequisites;
        this.learningOutcomes = learningOutcomes;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getInstructor() { return instructor; }
    public void setInstructor(String instructor) { this.instructor = instructor; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getTotalDuration() { return totalDuration; }
    public void setTotalDuration(String totalDuration) { this.totalDuration = totalDuration; }

    public int getTotalLectures() { return totalLectures; }
    public void setTotalLectures(int totalLectures) { this.totalLectures = totalLectures; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getPrerequisites() { return prerequisites; }
    public void setPrerequisites(String prerequisites) { this.prerequisites = prerequisites; }

    public String getLearningOutcomes() { return learningOutcomes; }
    public void setLearningOutcomes(String learningOutcomes) { this.learningOutcomes = learningOutcomes; }

    public List<Lecture> getLectures() { return lectures; }
    public void setLectures(List<Lecture> lectures) { this.lectures = lectures; }

    public void addLecture(Lecture lecture) {
        lectures.add(lecture);
        lecture.setCourse(this);
    }
}
