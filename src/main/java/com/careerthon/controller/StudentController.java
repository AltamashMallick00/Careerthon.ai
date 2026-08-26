package com.careerthon.controller;

import com.careerthon.model.*;
import com.careerthon.repository.*;
import com.careerthon.service.CertificateService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/student")
@SuppressWarnings("null")
public class StudentController {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseProgressRepository courseProgressRepository;
    private final CourseRepository courseRepository;
    private final LectureRepository lectureRepository;
    private final CertificateRepository certificateRepository;
    private final CertificateService certificateService;
    private final LmsAssignmentRepository assignmentRepository;
    private final InternshipRepository internshipRepository;

    public StudentController(UserRepository userRepository, StudentProfileRepository studentProfileRepository,
                             EnrollmentRepository enrollmentRepository, CourseProgressRepository courseProgressRepository,
                             CourseRepository courseRepository, LectureRepository lectureRepository,
                             CertificateRepository certificateRepository, CertificateService certificateService,
                             LmsAssignmentRepository assignmentRepository, InternshipRepository internshipRepository) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.courseProgressRepository = courseProgressRepository;
        this.courseRepository = courseRepository;
        this.lectureRepository = lectureRepository;
        this.certificateRepository = certificateRepository;
        this.certificateService = certificateService;
        this.assignmentRepository = assignmentRepository;
        this.internshipRepository = internshipRepository;
    }

    private User getAuthenticatedUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername()).orElse(null);
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = getAuthenticatedUser(userDetails);
        if (user == null) return "redirect:/login";

        StudentProfile profile = studentProfileRepository.findByUserId(user.getId()).orElse(null);
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(user.getId());
        List<Course> allCourses = courseRepository.findAll();
        List<Course> enrolledCourses = enrollments.stream().map(Enrollment::getCourse).collect(Collectors.toList());
        List<Course> availableCourses = allCourses.stream()
                .filter(c -> !enrolledCourses.contains(c))
                .collect(Collectors.toList());

        model.addAttribute("user", user);
        model.addAttribute("profile", profile);
        model.addAttribute("enrollments", enrollments);
        model.addAttribute("availableCourses", availableCourses);

        return "student/dashboard";
    }

    @PostMapping("/enroll/{courseId}")
    public String selfEnroll(@PathVariable Long courseId, @AuthenticationPrincipal UserDetails userDetails, RedirectAttributes redirectAttributes) {
        User user = getAuthenticatedUser(userDetails);
        if (user == null) return "redirect:/login";
        
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course != null && !enrollmentRepository.existsByStudentIdAndCourseId(user.getId(), courseId)) {
            enrollmentRepository.save(new Enrollment(user, course));
            redirectAttributes.addFlashAttribute("message", "Successfully enrolled in " + course.getTitle());
        }
        return "redirect:/student/dashboard";
    }

    @GetMapping("/course/{id}")
    public String viewCourse(@PathVariable Long id, 
                             @RequestParam(required = false) Long lectureId,
                             @AuthenticationPrincipal UserDetails userDetails, 
                             Model model) {
        User user = getAuthenticatedUser(userDetails);
        if (user == null) return "redirect:/login";

        // Check if enrolled
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(user.getId(), id).orElse(null);
        if (enrollment == null) {
            return "redirect:/student/dashboard"; // Not enrolled
        }

        Course course = courseRepository.findById(id).orElse(null);
        if (course == null) return "redirect:/student/dashboard";

        List<Lecture> lectures = lectureRepository.findByCourseIdOrderByModuleOrderAscLectureOrderAsc(id);
        
        // Mark lecture as completed if a lectureId was passed
        if (lectureId != null) {
            if (!courseProgressRepository.existsByStudentIdAndCourseIdAndLectureId(user.getId(), course.getId(), lectureId)) {
                Lecture currentLecture = lectureRepository.findById(lectureId).orElse(null);
                if (currentLecture != null) {
                    courseProgressRepository.save(new CourseProgress(user, course, currentLecture));
                }
            }
        }

        // Group lectures by module name
        Map<String, List<Lecture>> modules = lectures.stream()
                .collect(Collectors.groupingBy(Lecture::getModuleName, LinkedHashMap::new, Collectors.toList()));

        // Active lecture
        Lecture activeLecture = null;
        if (lectureId != null) {
            activeLecture = lectures.stream().filter(l -> l.getId().equals(lectureId)).findFirst().orElse(null);
        }
        if (activeLecture == null && !lectures.isEmpty()) {
            activeLecture = lectures.get(0);
        }

        // Progress calculation
        long completedLecturesCount = courseProgressRepository.countByStudentIdAndCourseId(user.getId(), course.getId());
        int progressPercentage = lectures.isEmpty() ? 0 : (int) ((completedLecturesCount * 100) / lectures.size());

        // Check if completed to generate certificate
        if (progressPercentage == 100 && !enrollment.isCompleted()) {
            enrollment.setCompleted(true);
            enrollmentRepository.save(enrollment);
            certificateService.generateCourseCertificate(user, course);
        }

        // Fetch completed lecture IDs for UI checkmarks
        List<Long> completedLectureIds = courseProgressRepository.findByStudentIdAndCourseId(user.getId(), course.getId())
                .stream().map(cp -> cp.getLecture().getId()).collect(Collectors.toList());

        model.addAttribute("course", course);
        model.addAttribute("modules", modules);
        model.addAttribute("activeLecture", activeLecture);
        model.addAttribute("progressPercentage", progressPercentage);
        model.addAttribute("completedLectureIds", completedLectureIds);

        return "student/course_view";
    }

    @GetMapping("/certificates")
    public String certificates(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = getAuthenticatedUser(userDetails);
        if (user == null) return "redirect:/login";

        List<Certificate> certificates = certificateRepository.findByStudentId(user.getId());
        model.addAttribute("certificates", certificates);

        return "student/certificates";
    }

    @GetMapping("/assignments")
    public String assignments(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = getAuthenticatedUser(userDetails);
        if (user == null) return "redirect:/login";

        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(user.getId());
        List<Long> courseIds = enrollments.stream().map(e -> e.getCourse().getId()).collect(Collectors.toList());
        List<LmsAssignment> assignments = assignmentRepository.findAll().stream()
                .filter(a -> courseIds.contains(a.getCourse().getId()))
                .collect(Collectors.toList());

        model.addAttribute("assignments", assignments);
        return "student/assignments";
    }

    @GetMapping("/internships")
    public String internships(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = getAuthenticatedUser(userDetails);
        if (user == null) return "redirect:/login";

        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(user.getId());
        List<Long> courseIds = enrollments.stream().map(e -> e.getCourse().getId()).collect(Collectors.toList());
        List<Internship> internships = internshipRepository.findAll().stream()
                .filter(i -> courseIds.contains(i.getCourse().getId()))
                .collect(Collectors.toList());

        model.addAttribute("internships", internships);
        return "student/internships";
    }
}
