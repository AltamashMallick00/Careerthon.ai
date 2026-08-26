package com.careerthon.model;

import jakarta.persistence.*;

@Entity
@Table(name = "lectures")
public class Lecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private String youtubeVideoId; // e.g. "eIrMbAQSU34"

    @Column(nullable = false)
    private String moduleName; // e.g. "Module 1: Java Basics"

    private int moduleOrder; // for sorting modules

    private int lectureOrder; // for sorting within a module

    private String duration; // e.g. "12:35"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    public Lecture() {}

    public Lecture(String title, String description, String youtubeVideoId,
                   String moduleName, int moduleOrder, int lectureOrder, String duration) {
        this.title = title;
        this.description = description;
        this.youtubeVideoId = youtubeVideoId;
        this.moduleName = moduleName;
        this.moduleOrder = moduleOrder;
        this.lectureOrder = lectureOrder;
        this.duration = duration;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getYoutubeVideoId() { return sanitizeYoutubeId(youtubeVideoId); }
    public void setYoutubeVideoId(String youtubeVideoId) { this.youtubeVideoId = sanitizeYoutubeId(youtubeVideoId); }

    private String sanitizeYoutubeId(String videoId) {
        if (videoId == null) return null;
        videoId = videoId.trim();
        if (videoId.contains("v=")) {
            videoId = videoId.substring(videoId.indexOf("v=") + 2);
            if (videoId.contains("&")) videoId = videoId.substring(0, videoId.indexOf("&"));
        } else if (videoId.contains("youtu.be/")) {
            videoId = videoId.substring(videoId.indexOf("youtu.be/") + 9);
            if (videoId.contains("?")) videoId = videoId.substring(0, videoId.indexOf("?"));
        } else if (videoId.contains("embed/")) {
            videoId = videoId.substring(videoId.indexOf("embed/") + 6);
            if (videoId.contains("?")) videoId = videoId.substring(0, videoId.indexOf("?"));
        }
        return videoId;
    }

    public String getModuleName() { return moduleName; }
    public void setModuleName(String moduleName) { this.moduleName = moduleName; }

    public int getModuleOrder() { return moduleOrder; }
    public void setModuleOrder(int moduleOrder) { this.moduleOrder = moduleOrder; }

    public int getLectureOrder() { return lectureOrder; }
    public void setLectureOrder(int lectureOrder) { this.lectureOrder = lectureOrder; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
}
