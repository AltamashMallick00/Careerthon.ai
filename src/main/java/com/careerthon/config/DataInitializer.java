package com.careerthon.config;

import com.careerthon.model.User;
import com.careerthon.model.UserStory;
import com.careerthon.model.Job;
import com.careerthon.model.Course;
import com.careerthon.model.Lecture;
import com.careerthon.repository.UserRepository;
import com.careerthon.repository.UserStoryRepository;
import com.careerthon.repository.JobRepository;
import com.careerthon.repository.CourseRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(1)
@SuppressWarnings("null")
public class DataInitializer implements CommandLineRunner {

    private final UserStoryRepository userStoryRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JobRepository jobRepository;
    private final CourseRepository courseRepository;

    public DataInitializer(UserStoryRepository userStoryRepository, UserRepository userRepository,
            PasswordEncoder passwordEncoder, JobRepository jobRepository, CourseRepository courseRepository) {
        this.userStoryRepository = userStoryRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jobRepository = jobRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    public void run(String... args) {
        // Synchronize Team Members & Global Professional Testimonials (TCS, IBM, Ingram Micro)
        userStoryRepository.deleteAll();

        List<UserStory> allStories = List.of(
                // ── Core Team Members ──
                new UserStory(
                        "Priyanshu Shekhar",
                        "Full Stack Developer & Project Lead",
                        "As the developer and architect of Careerthon.AI, I built this platform to democratize LinkedIn profile optimization. Using Spring Boot and modern web technologies, I created an end-to-end SaaS solution that provides actionable insights to job seekers.",
                        "PS", "#0A66C2", "/images/priyanshu_shekhar.jpg"),
                new UserStory(
                        "Md Afroz Hassan",
                        "Data Engineer & Analytics Architect",
                        "Engineered high-throughput ETL data pipelines, real-time telemetry streaming, and automated analytics models for Careerthon.AI. Architected scalable data processing workflows to benchmark candidate profile scoring algorithms against industry data points.",
                        "AH", "#0284c7", "/images/afroz_hassan.jpg"),
                new UserStory(
                        "Altamash Mallick",
                        "Backend Engineer & Data Analyst",
                        "I contributed to the profile analysis engine and data modeling for Careerthon.AI. The scoring algorithm uses weighted analysis across 15 profile dimensions, benchmarked against industry standards.",
                        "AM", "#059669", "/images/altamash_mallick.jpg"),

                // ── Global Testimonials: TCS, IBM, Ingram Micro (No Photos, Clean Initials Badges) ──
                new UserStory(
                        "Rohan Sen",
                        "Senior Consultant @ Tata Consultancy Services (TCS)",
                        "This tool gave my profile the competitive edge it needed. The comprehensive ATS analysis was precise, helping me highlight key delivery metrics and architectural depth for global enterprise accounts.",
                        "RS", "#0A66C2", null),
                new UserStory(
                        "Arjun Varma",
                        "Cloud Solutions Architect @ IBM",
                        "As a technical lead, I see hundreds of profiles daily. Careerthon.AI formats and aligns candidate technical vectors with exact enterprise recruiter search keywords. An absolute game-changer!",
                        "AV", "#0F62FE", null),
                new UserStory(
                        "Sneha Kulkarni",
                        "Lead Talent Acquisition Partner @ Ingram Micro",
                        "After applying the keyword density optimizations and ATS alignment from Careerthon.AI, our candidates saw a major surge in organic recruiter discoverability and interview requests.",
                        "SK", "#059669", null),
                new UserStory(
                        "Vikram Malhotra",
                        "Full Stack Technical Lead @ Tata Consultancy Services (TCS)",
                        "The genuine LinkedIn PDF scanner accurately identified several critical gaps in my experience summaries, making my profile stand out to tier-1 enterprise recruiters.",
                        "VM", "#7C3AED", null),
                new UserStory(
                        "Ananya Roy",
                        "Enterprise Systems Specialist @ IBM",
                        "The keyword optimization and ATS benchmarking completely transformed my profile summary into a high-converting executive pitch. Landed top interviews within two weeks.",
                        "AR", "#D97706", null),
                new UserStory(
                        "Pooja Deshmukh",
                        "Senior Cloud Engineer @ Ingram Micro",
                        "Careerthon.AI's deep profile scan provides actionable clarity on how enterprise ATS platforms index technical capabilities. Extremely valuable for every tech professional.",
                        "PD", "#E11D48", null)
        );

        userStoryRepository.saveAll(allStories);
        System.out.println("✅ Seeded updated team members and TCS, IBM, Ingram Micro testimonials.");

        // Ensure Primary Admin Priyanshu123 exists
        userRepository.findByUsername("Priyanshu123").ifPresentOrElse(
                adminUser -> {
                    adminUser.setRoles("ROLE_ADMIN,ROLE_STUDENT,ROLE_USER");
                    adminUser.setPassword(passwordEncoder.encode("Hello1234"));
                    adminUser.setFullName("Priyanshu Shekhar");
                    userRepository.save(adminUser);
                },
                () -> {
                    User admin = new User("Priyanshu123", passwordEncoder.encode("Hello1234"), "ROLE_ADMIN,ROLE_STUDENT,ROLE_USER", "Priyanshu Shekhar");
                    userRepository.save(admin);
                });

        // Ensure secondary admin user also uses password Hello1234
        userRepository.findByUsername("admin").ifPresentOrElse(
                adminUser -> {
                    adminUser.setRoles("ROLE_ADMIN,ROLE_STUDENT,ROLE_USER");
                    adminUser.setPassword(passwordEncoder.encode("Hello1234"));
                    userRepository.save(adminUser);
                },
                () -> {
                    User admin = new User("admin", passwordEncoder.encode("Hello1234"), "ROLE_ADMIN,ROLE_STUDENT,ROLE_USER", "Administrator");
                    userRepository.save(admin);
                });

        // Ensure initial Job Openings are seeded
        if (jobRepository.count() == 0) {
            jobRepository.saveAll(List.of(
                new Job("AI NLP Architect", "Full-Time", "Engineering | Fully Remote",
                    "Architect next-generation deep learning parsing algorithms capable of extracting intelligence from complex executive resumes."),
                new Job("Senior Profile Advisor", "Part-Time / Contract", "Customer Success | Remote / Hybrid",
                    "Direct executive clients in identifying digital gaps, providing strategic keyword alignment roadmaps, and hosting private consultations."),
                new Job("Spring Boot Platform Engineer", "Full-Time", "Engineering | Fully Remote",
                    "Scale high-performance enterprise REST API frameworks, secure H2/PostgreSQL database instances, and maintain Thymeleaf UI pipelines.")
            ));
            System.out.println("✅ Seeded initial job openings.");
        }

        // ── LMS: Seed Java Full Stack Course ──────────────────────────
        if (courseRepository.count() == 0) {
            Course jfs = new Course(
                "Java Full Stack Development",
                "Master Java Full Stack development from scratch. This comprehensive course covers Core Java, OOP, HTML/CSS/JavaScript, SQL databases, Spring Boot, REST APIs, JPA/Hibernate, React.js, and deployment — everything you need to become a job-ready full stack Java developer.",
                "Multiple Instructors",
                null,
                "Beginner to Advanced",
                "Java Full Stack",
                "45+ Hours",
                25
            );

            // ── Module 1: Java Fundamentals ──
            jfs.addLecture(new Lecture("Java Tutorial for Beginners — Full Course",
                "Complete Java programming tutorial covering installation, syntax, variables, data types, operators, control flow, and methods.",
                "eIrMbAQSU34", "Module 1: Java Fundamentals", 1, 1, "2:30:00"));
            jfs.addLecture(new Lecture("Java Programming Full Course — 12 Hours",
                "In-depth Java course covering basics to advanced concepts including exception handling and file I/O.",
                "xk4_1vDrzzo", "Module 1: Java Fundamentals", 1, 2, "12:00:00"));
            jfs.addLecture(new Lecture("Java Variables & Data Types Explained",
                "Deep dive into Java's type system — primitives, reference types, type casting, and best practices.",
                "so1iUppGtF4", "Module 1: Java Fundamentals", 1, 3, "18:35"));
            jfs.addLecture(new Lecture("Java Arrays & Collections Framework",
                "Master arrays, ArrayList, HashMap, LinkedList, and the Java Collections framework.",
                "1nRj4ALuw7A", "Module 1: Java Fundamentals", 1, 4, "45:20"));

            // ── Module 2: OOP & Advanced Java ──
            jfs.addLecture(new Lecture("Object-Oriented Programming in Java",
                "Classes, objects, inheritance, polymorphism, encapsulation, and abstraction — the four pillars of OOP.",
                "pTB0EiLXUC8", "Module 2: OOP & Advanced Java", 2, 1, "1:55:00"));
            jfs.addLecture(new Lecture("Java Interfaces & Abstract Classes",
                "When and how to use interfaces vs abstract classes, default methods, and design patterns.",
                "GhslBwBRRMo", "Module 2: OOP & Advanced Java", 2, 2, "28:40"));
            jfs.addLecture(new Lecture("Java 8 Lambda Expressions & Streams",
                "Functional programming in Java with lambda expressions, streams API, map, filter, reduce.",
                "gpIUfj3KaOc", "Module 2: OOP & Advanced Java", 2, 3, "1:15:00"));
            jfs.addLecture(new Lecture("Exception Handling & Multithreading",
                "Try-catch-finally, custom exceptions, threads, Runnable, ExecutorService, and concurrent programming.",
                "r59xYe3IMYk", "Module 2: OOP & Advanced Java", 2, 4, "58:00"));

            // ── Module 3: Frontend — HTML, CSS, JavaScript ──
            jfs.addLecture(new Lecture("HTML & CSS Full Course — Build a Website",
                "HTML5 and CSS3 from scratch. Responsive layouts with Flexbox and Grid.",
                "G3e-cpL7ofc", "Module 3: Frontend — HTML, CSS, JS", 3, 1, "6:30:00"));
            jfs.addLecture(new Lecture("JavaScript Full Course for Beginners",
                "JS fundamentals — variables, functions, DOM manipulation, events, async/await, fetch API, ES6+.",
                "PkZNo7MFNFg", "Module 3: Frontend — HTML, CSS, JS", 3, 2, "3:40:00"));
            jfs.addLecture(new Lecture("CSS Flexbox & Grid — Complete Guide",
                "Modern CSS layouts with Flexbox and CSS Grid. Build responsive designs for all screen sizes.",
                "phWxA89Dy94", "Module 3: Frontend — HTML, CSS, JS", 3, 3, "45:00"));

            // ── Module 4: SQL & Databases ──
            jfs.addLecture(new Lecture("SQL Tutorial — Full Database Course",
                "Complete SQL: SELECT, INSERT, UPDATE, DELETE, JOINs, subqueries, indexing, and database design.",
                "HXV3zeQKqGY", "Module 4: SQL & Databases", 4, 1, "4:20:00"));
            jfs.addLecture(new Lecture("MySQL Full Course",
                "MySQL installation, table creation, relationships, normalization, stored procedures.",
                "7S_tz1z_5bA", "Module 4: SQL & Databases", 4, 2, "3:10:00"));
            jfs.addLecture(new Lecture("JDBC — Connecting Java to Databases",
                "Java Database Connectivity — DriverManager, Connection, PreparedStatement, ResultSet.",
                "e8g-7WnCaBo", "Module 4: SQL & Databases", 4, 3, "52:00"));

            // ── Module 5: Spring Boot & Backend ──
            jfs.addLecture(new Lecture("Spring Boot Tutorial for Beginners",
                "Spring Boot: project setup, auto-configuration, dependency injection, REST controllers.",
                "9SGDpanrc8U", "Module 5: Spring Boot & Backend", 5, 1, "2:45:00"));
            jfs.addLecture(new Lecture("Spring Boot REST API — CRUD Application",
                "Build a production REST API: @RestController, ResponseEntity, validation, error handling.",
                "0B_0gBal3OA", "Module 5: Spring Boot & Backend", 5, 2, "1:20:00"));
            jfs.addLecture(new Lecture("Spring Data JPA & Hibernate Tutorial",
                "ORM with Spring Data JPA — entity mapping, repositories, JPQL, pagination, relationships.",
                "8SGI_XS5OPw", "Module 5: Spring Boot & Backend", 5, 3, "1:50:00"));
            jfs.addLecture(new Lecture("Spring Security — Auth & Authorization",
                "Secure auth with Spring Security: form login, JWT tokens, role-based authorization.",
                "her_7pa0vrg", "Module 5: Spring Boot & Backend", 5, 4, "1:30:00"));
            jfs.addLecture(new Lecture("Spring Boot Microservices Tutorial",
                "Microservices architecture: service discovery, API gateway, inter-service communication.",
                "lh1oQHCVRt4", "Module 5: Spring Boot & Backend", 5, 5, "2:10:00"));

            // ── Module 6: React & Full Stack Project ──
            jfs.addLecture(new Lecture("React JS Full Course for Beginners",
                "React from scratch — components, JSX, state, props, hooks, routing, dynamic UIs.",
                "bMknfKXIFA8", "Module 6: React & Full Stack Project", 6, 1, "6:00:00"));
            jfs.addLecture(new Lecture("Spring Boot + React Full Stack CRUD",
                "Complete full stack app: Spring Boot backend API + React frontend. Axios, CORS, CRUD.",
                "O_XL9oQ1_To", "Module 6: React & Full Stack Project", 6, 2, "2:30:00"));
            jfs.addLecture(new Lecture("Full Stack Project — Employee Management",
                "End-to-end project: Employee Management System with Spring Boot, JPA, MySQL, React.",
                "YuAkWiMB95I", "Module 6: React & Full Stack Project", 6, 3, "3:15:00"));

            // ── Module 7: DevOps & Deployment ──
            jfs.addLecture(new Lecture("Git & GitHub Full Course",
                "Version control: git init, commit, branching, merging, pull requests, GitHub workflow.",
                "RGOj5yH7evk", "Module 7: DevOps & Deployment", 7, 1, "1:10:00"));
            jfs.addLecture(new Lecture("Docker Tutorial for Beginners",
                "Docker: containers, images, Dockerfile, Docker Compose, deploying Spring Boot apps.",
                "fqMOX6JJhGo", "Module 7: DevOps & Deployment", 7, 2, "2:10:00"));

            courseRepository.save(jfs);
            System.out.println("✅ Seeded Java Full Stack course with " + jfs.getLectures().size() + " lectures.");
        }
    }
}
