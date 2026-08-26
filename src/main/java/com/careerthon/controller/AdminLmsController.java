package com.careerthon.controller;

import com.careerthon.model.*;
import com.careerthon.repository.*;
import com.careerthon.service.YoutubeImportService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/lms")
@SuppressWarnings("null")
public class AdminLmsController {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final InternshipRepository internshipRepository;
    private final YoutubeImportService youtubeImportService;
    private final PasswordEncoder passwordEncoder;
    private final LmsAssignmentRepository lmsAssignmentRepository;
    private final QuizRepository quizRepository;

    public AdminLmsController(CourseRepository courseRepository, UserRepository userRepository,
                              StudentProfileRepository studentProfileRepository,
                              EnrollmentRepository enrollmentRepository,
                              InternshipRepository internshipRepository,
                              YoutubeImportService youtubeImportService,
                              PasswordEncoder passwordEncoder,
                              LmsAssignmentRepository lmsAssignmentRepository,
                              QuizRepository quizRepository) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.internshipRepository = internshipRepository;
        this.youtubeImportService = youtubeImportService;
        this.passwordEncoder = passwordEncoder;
        this.lmsAssignmentRepository = lmsAssignmentRepository;
        this.quizRepository = quizRepository;
    }

    @GetMapping("/dashboard")
    public String lmsDashboard(Model model) {
        model.addAttribute("totalCourses", courseRepository.count());
        model.addAttribute("totalStudents", studentProfileRepository.count());
        model.addAttribute("totalInternships", internshipRepository.count());
        model.addAttribute("totalEnrollments", enrollmentRepository.count());
        model.addAttribute("totalAssignments", lmsAssignmentRepository.count());
        model.addAttribute("totalQuizzes", quizRepository.count());
        return "admin/lms/dashboard";
    }

    // --- Courses ---
    @GetMapping("/courses")
    public String listCourses(Model model) {
        model.addAttribute("courses", courseRepository.findAllByOrderByCreatedAtDesc());
        return "admin/lms/courses";
    }

    @PostMapping("/courses/delete/{id}")
    public String deleteCourse(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        courseRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "Course deleted successfully!");
        return "redirect:/admin/lms/courses";
    }

    // --- YouTube Import ---
    @GetMapping("/import")
    public String showImportPage() {
        return "admin/lms/import";
    }

    @PostMapping("/import")
    public String importYoutubePlaylist(@RequestParam String playlistUrl, RedirectAttributes redirectAttributes) {
        try {
            Course course = youtubeImportService.importPlaylist(playlistUrl);
            redirectAttributes.addFlashAttribute("message", "Successfully imported course: " + course.getTitle());
            return "redirect:/admin/lms/courses";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Import failed: " + e.getMessage());
            return "redirect:/admin/lms/import";
        }
    }

    // --- Students ---
    @GetMapping("/students")
    public String listStudents(Model model) {
        model.addAttribute("students", studentProfileRepository.findAll());
        model.addAttribute("courses", courseRepository.findAll());
        return "admin/lms/students";
    }

    @PostMapping("/students/add")
    public String addStudent(@RequestParam String fullName, @RequestParam String username,
                             @RequestParam String password, @RequestParam String mobileNumber,
                             @RequestParam String collegeName, @RequestParam String branch,
                             @RequestParam String graduationYear, RedirectAttributes redirectAttributes) {
        if (userRepository.findByUsername(username).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Username already exists!");
            return "redirect:/admin/lms/students";
        }

        User user = new User(username, passwordEncoder.encode(password), "ROLE_STUDENT", fullName);
        user = userRepository.save(user);

        StudentProfile profile = new StudentProfile(user, mobileNumber, collegeName, branch, graduationYear);
        studentProfileRepository.save(profile);

        redirectAttributes.addFlashAttribute("message", "Student created successfully!");
        return "redirect:/admin/lms/students";
    }

    @PostMapping("/students/enroll")
    public String enrollStudent(@RequestParam Long studentId, @RequestParam Long courseId, RedirectAttributes redirectAttributes) {
        User student = userRepository.findById(studentId).orElse(null);
        Course course = courseRepository.findById(courseId).orElse(null);
        
        if (student != null && course != null) {
            if (!enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), course.getId())) {
                enrollmentRepository.save(new Enrollment(student, course));
                redirectAttributes.addFlashAttribute("message", "Student enrolled in " + course.getTitle());
            } else {
                redirectAttributes.addFlashAttribute("error", "Student is already enrolled in this course.");
            }
        }
        return "redirect:/admin/lms/students";
    }

    @PostMapping("/students/enroll/bulk")
    public String bulkEnrollStudents(@RequestParam List<Long> studentIds, @RequestParam Long courseId, RedirectAttributes redirectAttributes) {
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course != null && studentIds != null) {
            int count = 0;
            for (Long studentId : studentIds) {
                User student = userRepository.findById(studentId).orElse(null);
                if (student != null && !enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), course.getId())) {
                    enrollmentRepository.save(new Enrollment(student, course));
                    count++;
                }
            }
            redirectAttributes.addFlashAttribute("message", count + " students successfully enrolled in " + course.getTitle());
        }
        return "redirect:/admin/lms/students";
    }
}
