package com.careerthon.config;

import com.careerthon.model.*;
import com.careerthon.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class CourseSeeder implements CommandLineRunner {

    private final CourseRepository courseRepository;
    private final InternshipRepository internshipRepository;
    private final QuizRepository quizRepository;
    private final LmsAssignmentRepository lmsAssignmentRepository;
    private final LectureRepository lectureRepository;

    public CourseSeeder(CourseRepository courseRepository,
                        InternshipRepository internshipRepository,
                        QuizRepository quizRepository,
                        LmsAssignmentRepository lmsAssignmentRepository,
                        LectureRepository lectureRepository) {
        this.courseRepository = courseRepository;
        this.internshipRepository = internshipRepository;
        this.quizRepository = quizRepository;
        this.lmsAssignmentRepository = lmsAssignmentRepository;
        this.lectureRepository = lectureRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Seed Courses
        if (courseRepository.count() <= 1) { 
            List<Course> coursesToSeed = Arrays.asList(
                    new Course("Spring Boot Microservices", "Master Spring Boot microservices architecture, Eureka, API Gateway, and distributed tracing.", "Admin", null, "Advanced", "Spring Boot", "30+ Hours", 20),
                    new Course("React JS Complete Guide", "Learn React hooks, Redux toolkit, Context API, and build modern responsive web applications.", "Admin", null, "Intermediate", "React JS", "25+ Hours", 18),
                    new Course("Angular Masterclass", "Deep dive into Angular components, directives, RxJS, state management, and routing.", "Admin", null, "Intermediate", "Angular", "28+ Hours", 22),
                    new Course("Node JS Backend Development", "Build scalable backend APIs using Node.js, event loop, streams, and asynchronous programming.", "Admin", null, "Intermediate", "Node JS", "20+ Hours", 15),
                    new Course("Express JS Framework", "Build fast and secure REST APIs with Express JS, middleware, authentication, and MongoDB.", "Admin", null, "Beginner to Advanced", "Express JS", "15+ Hours", 12),
                    new Course("Python Development Mastery", "Complete Python course covering syntax, OOP, file handling, libraries, and scripting.", "Admin", null, "Beginner", "Python Development", "35+ Hours", 25),
                    new Course("Django Web Framework", "Build robust web applications with Django ORM, templates, authentication, and REST framework.", "Admin", null, "Intermediate", "Django", "30+ Hours", 20),
                    new Course("Flask for API Development", "Learn to build lightweight, fast, and scalable REST APIs using Python Flask.", "Admin", null, "Intermediate", "Flask", "12+ Hours", 10),
                    new Course("Data Science Bootcamp", "Pandas, NumPy, Matplotlib, exploratory data analysis, and statistical modeling in Python.", "Admin", null, "Beginner to Advanced", "Data Science", "40+ Hours", 30),
                    new Course("Machine Learning A-Z", "Supervised and unsupervised learning, regression, classification, clustering, and Scikit-Learn.", "Admin", null, "Advanced", "Machine Learning", "45+ Hours", 35),
                    new Course("Deep Learning with PyTorch & TF", "Neural networks, CNNs, RNNs, computer vision, and NLP using TensorFlow and PyTorch.", "Admin", null, "Advanced", "Deep Learning", "50+ Hours", 40),
                    new Course("Artificial Intelligence Foundations", "Introduction to AI algorithms, search strategies, knowledge representation, and expert systems.", "Admin", null, "Beginner", "Artificial Intelligence", "20+ Hours", 15),
                    new Course("Generative AI & LLMs", "Understand transformers, LLMs, fine-tuning, RAG architecture, and LangChain integration.", "Admin", null, "Advanced", "Generative AI", "25+ Hours", 18),
                    new Course("Prompt Engineering Masterclass", "Learn advanced prompting techniques for ChatGPT, Claude, and Midjourney to boost productivity.", "Admin", null, "Beginner", "Prompt Engineering", "10+ Hours", 8),
                    new Course("Agentic AI Development", "Build autonomous AI agents using AutoGPT, LangChain, and API integrations.", "Admin", null, "Advanced", "Agentic AI", "15+ Hours", 12),
                    new Course("Cyber Security Fundamentals", "Network security, cryptography, vulnerability assessment, and risk management basics.", "Admin", null, "Beginner", "Cyber Security", "30+ Hours", 20),
                    new Course("Ethical Hacking & Pen Testing", "Kali Linux, Metasploit, Nmap, Wireshark, web application hacking, and security audits.", "Admin", null, "Advanced", "Ethical Hacking", "45+ Hours", 35),
                    new Course("Cloud Computing Essentials", "Concepts of IaaS, PaaS, SaaS, virtualization, and public/private cloud architectures.", "Admin", null, "Beginner", "Cloud Computing", "15+ Hours", 10),
                    new Course("AWS Solutions Architect", "EC2, S3, RDS, Lambda, VPC, IAM, and deploying scalable architectures on AWS.", "Admin", null, "Intermediate", "AWS", "40+ Hours", 28),
                    new Course("Microsoft Azure Fundamentals", "Azure App Services, Virtual Machines, Cosmos DB, and Azure DevOps integration.", "Admin", null, "Intermediate", "Azure", "35+ Hours", 25),
                    new Course("Google Cloud Platform (GCP)", "Compute Engine, Cloud Storage, BigQuery, and Kubernetes Engine on Google Cloud.", "Admin", null, "Intermediate", "Google Cloud", "30+ Hours", 22),
                    new Course("DevOps Engineering", "CI/CD pipelines, Jenkins, Git, Infrastructure as Code, Terraform, and Ansible.", "Admin", null, "Advanced", "DevOps", "40+ Hours", 30),
                    new Course("Docker Certification Guide", "Containerization, Dockerfiles, volumes, networks, and Docker Compose.", "Admin", null, "Intermediate", "Docker", "15+ Hours", 12),
                    new Course("Kubernetes (K8s) Mastery", "Pod deployment, Services, Ingress, StatefulSets, ConfigMaps, and cluster management.", "Admin", null, "Advanced", "Kubernetes", "25+ Hours", 20)
            );

            for (Course course : coursesToSeed) {
                if (courseRepository.findByCategoryIgnoreCase(course.getCategory()).isEmpty()) {
                    Course savedCourse = courseRepository.save(course);
                    seedLecturesForCourse(savedCourse);
                }
            }
            System.out.println("✅ Successfully seeded 24 new technology courses and their video lectures into the LMS database.");
        }

        // Make sure all existing courses also have lectures if they are empty
        List<Course> allCourses = courseRepository.findAll();
        for (Course course : allCourses) {
            if (lectureRepository.countByCourseId(course.getId()) == 0) {
                seedLecturesForCourse(course);
            }
        }

        // Try to get the main Java course to attach features to
        Course mainCourse = courseRepository.findAll().stream()
                .filter(c -> c.getTitle().contains("Java") || c.getTitle().contains("Boot"))
                .findFirst()
                .orElse(courseRepository.findAll().get(0));

        // Seed Internship
        if (internshipRepository.count() == 0 && mainCourse != null) {
            Internship internship = new Internship(mainCourse, 
                    "Full Stack Developer Internship", 
                    "Work on a real-world enterprise application building backend APIs with Spring Boot and an interactive frontend with React/HTML.", 
                    "https://Careerthon.AI.app/docs/internship-guidelines.pdf");
            
            InternshipTask task1 = new InternshipTask(internship, "Task 1: API Design", "Design the REST API endpoints and create Swagger documentation.", LocalDateTime.now().plusDays(7));
            InternshipTask task2 = new InternshipTask(internship, "Task 2: Database Schema", "Design the database schema using MySQL and create JPA entities.", LocalDateTime.now().plusDays(14));
            InternshipTask task3 = new InternshipTask(internship, "Task 3: Integration", "Integrate the backend with a React frontend to build a complete dashboard.", LocalDateTime.now().plusDays(21));
            
            internship.getTasks().addAll(Arrays.asList(task1, task2, task3));
            internshipRepository.save(internship);
            System.out.println("✅ Successfully seeded sample Internship.");
        }

        // Seed Quiz
        if (quizRepository.count() == 0 && mainCourse != null) {
            Quiz quiz = new Quiz(mainCourse, "Java & Spring Boot Core Concepts Quiz");
            
            QuizQuestion q1 = new QuizQuestion(quiz, "Which of the following is true about Spring Boot?", "It requires XML configuration", "It provides auto-configuration", "It does not support embedded servers", "It is only used for frontend", "B");
            QuizQuestion q2 = new QuizQuestion(quiz, "What is the primary purpose of the @RestController annotation?", "To map HTML views", "To handle REST API requests and bind responses directly to the body", "To configure database connections", "To declare security rules", "B");
            QuizQuestion q3 = new QuizQuestion(quiz, "In Java, which keyword is used to inherit a class?", "implement", "extend", "extends", "inherits", "C");
            
            quiz.getQuestions().addAll(Arrays.asList(q1, q2, q3));
            quizRepository.save(quiz);
            System.out.println("✅ Successfully seeded sample Quiz.");
        }

        // Seed Assignment
        if (lmsAssignmentRepository.count() == 0 && mainCourse != null) {
            LmsAssignment assignment = new LmsAssignment(mainCourse, 
                    "Build a RESTful Employee Management System", 
                    "Create a Spring Boot application with complete CRUD operations for an Employee entity. Use MySQL for the database and Spring Data JPA. Include validation and proper exception handling.", 
                    "https://Careerthon.AI.app/docs/assignment-criteria.pdf", 
                    LocalDateTime.now().plusDays(14));
            
            lmsAssignmentRepository.save(assignment);
            System.out.println("✅ Successfully seeded sample Assignment.");
        }
    }

    private void seedLecturesForCourse(Course course) {
        // High quality open-source YouTube video IDs
        String[] videoIds = {"PkZNo7MFNFg", "eIrMbAQSU34", "G3e-cpL7ofc", "pTB0EiLXUC8", "HXV3zeQKqGY", "_uQrJ0TkZlc", "rfscVS0vtbw", "F5mRW0jo-U4", "Z1RJmh_OqeA", "t25hL98R6gA", "0B_0gBal3OA", "EnT-ZTrvjZI", "SccSCuHhOw0", "bMknfKXIFA8", "k5E2AVpwsko"};
        
        // Add 3 modules
        for (int m = 1; m <= 3; m++) {
            String moduleName = "Module " + m + ": " + (m == 1 ? "Fundamentals of " : (m == 2 ? "Advanced Concepts in " : "Building Projects with ")) + course.getCategory();
            
            // Add 3-4 lectures per module
            for (int l = 1; l <= 4; l++) {
                String title = course.getCategory() + " Tutorial Part " + ((m-1)*4 + l);
                String videoId = videoIds[(int)(Math.random() * videoIds.length)];
                
                Lecture lecture = new Lecture(title, "Comprehensive tutorial covering core aspects of " + course.getCategory() + ".", videoId, moduleName, m, l, "45:00");
                lecture.setCourse(course);
                lectureRepository.save(lecture);
            }
        }
    }
}
