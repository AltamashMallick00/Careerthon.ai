package com.careerthon.config;

import com.careerthon.repository.ProfileReviewRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import org.springframework.lang.NonNull;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Random;

/**
 * Seeds 5,000+ demo data entries (3,000 LinkedIn profile reviews + 2,000 resume reviews)
 * into the H2 in-memory database at startup. Uses JDBC batch inserts for speed.
 * Only seeds if fewer than 100 profile reviews already exist.
 */
@Component
@Order(2) // Run after DataInitializer (default order)
public class DemoDataSeeder implements CommandLineRunner {

    private final ProfileReviewRepository profileReviewRepository;
    private final JdbcTemplate jdbcTemplate;
    private final Random random = new Random(42); // Fixed seed for reproducibility

    // ── Name pools ──────────────────────────────────────────────────────────────
    private static final String[] FIRST_NAMES = {
        "Aarav", "Vivaan", "Aditya", "Vihaan", "Arjun", "Sai", "Reyansh", "Ayaan", "Krishna", "Ishaan",
        "Ananya", "Diya", "Saanvi", "Aanya", "Isha", "Myra", "Sara", "Priya", "Riya", "Kavya",
        "Rahul", "Rohit", "Vikram", "Amit", "Suresh", "Rajesh", "Deepak", "Manish", "Sanjay", "Nikhil",
        "Neha", "Pooja", "Shreya", "Anjali", "Divya", "Sneha", "Meera", "Tanvi", "Nisha", "Swati",
        "James", "Robert", "Michael", "William", "David", "Richard", "Joseph", "Thomas", "Charles", "Daniel",
        "Mary", "Patricia", "Jennifer", "Linda", "Barbara", "Elizabeth", "Susan", "Jessica", "Sarah", "Karen",
        "Mohammed", "Ali", "Omar", "Yusuf", "Hassan", "Ibrahim", "Fatima", "Aisha", "Zainab", "Maryam",
        "Wei", "Jing", "Min", "Li", "Xiao", "Chen", "Yuki", "Haruto", "Sakura", "Hina",
        "Aryan", "Kabir", "Dhruv", "Karan", "Rohan", "Varun", "Kunal", "Gaurav", "Akash", "Harsh",
        "Simran", "Kiara", "Anika", "Tanya", "Ritika", "Pallavi", "Shalini", "Komal", "Rashmi", "Deepika",
        "Pranav", "Shivam", "Tushar", "Abhinav", "Vishal", "Siddharth", "Mayank", "Aman", "Naveen", "Pankaj",
        "Radhika", "Bhavna", "Sunita", "Geeta", "Mona", "Rekha", "Seema", "Lata", "Vandana", "Mamta",
        "Alex", "Jordan", "Taylor", "Morgan", "Casey", "Riley", "Avery", "Quinn", "Reese", "Skyler",
        "Arnav", "Dev", "Ishan", "Jay", "Krish", "Laksh", "Mihir", "Nakul", "Om", "Parth",
        "Aashvi", "Bhumi", "Charvi", "Drishti", "Esha", "Falak", "Gauri", "Heer", "Ira", "Janvi",
        "Ravi", "Sunil", "Vinod", "Ashok", "Prakash", "Manoj", "Ramesh", "Mohan", "Arun", "Vijay",
        "Priti", "Rani", "Suman", "Uma", "Veena", "Yamini", "Zara", "Aditi", "Bhavya", "Chitra",
        "Lucas", "Noah", "Ethan", "Mason", "Logan", "Oliver", "Aiden", "Elijah", "Liam", "Benjamin",
        "Emma", "Olivia", "Ava", "Sophia", "Isabella", "Mia", "Charlotte", "Amelia", "Harper", "Evelyn",
        "Abhay", "Chirag", "Dinesh", "Ganesh", "Hemant", "Jagdish", "Keshav", "Lalit", "Mukesh", "Naresh"
    };

    private static final String[] LAST_NAMES = {
        "Mishra", "Sharma", "Gupta", "Patel", "Singh", "Kumar", "Verma", "Jain", "Agarwal", "Mehta",
        "Shah", "Reddy", "Rao", "Nair", "Menon", "Iyer", "Pillai", "Bose", "Das", "Ghosh",
        "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez",
        "Wilson", "Anderson", "Taylor", "Thomas", "Hernandez", "Moore", "Martin", "Jackson", "Thompson", "White",
        "Khan", "Ahmed", "Ali", "Hassan", "Hussain", "Rahman", "Malik", "Ansari", "Sheikh", "Syed",
        "Wang", "Li", "Zhang", "Chen", "Liu", "Yang", "Huang", "Wu", "Zhou", "Xu",
        "Tanaka", "Suzuki", "Watanabe", "Takahashi", "Sato", "Nakamura", "Kobayashi", "Yamamoto", "Ito", "Kim",
        "Chauhan", "Thakur", "Pandey", "Tiwari", "Dwivedi", "Shukla", "Tripathi", "Saxena", "Rastogi", "Chandra",
        "Kapoor", "Malhotra", "Chopra", "Bhatia", "Arora", "Khanna", "Sethi", "Bansal", "Goel", "Mittal",
        "Mukherjee", "Chatterjee", "Banerjee", "Dutta", "Sen", "Roy", "Sarkar", "Biswas", "Mondal", "Barua",
        "Patil", "Kulkarni", "Deshmukh", "Joshi", "Deshpande", "Sathe", "Gokhale", "Apte", "Bhatt", "Trivedi",
        "Srivastava", "Bajpai", "Awasthi", "Nigam", "Mathur", "Bhatnagar", "Tandon", "Kaul", "Dhar", "Walia"
    };

    private static final String[] TITLES = {
        "Software Engineer", "Senior Software Engineer", "Full Stack Developer", "Frontend Developer",
        "Backend Developer", "Data Scientist", "Data Analyst", "Machine Learning Engineer",
        "Product Manager", "Project Manager", "Technical Lead", "Engineering Manager",
        "DevOps Engineer", "Cloud Architect", "Solutions Architect", "System Administrator",
        "UI/UX Designer", "Graphic Designer", "Web Developer", "Mobile Developer",
        "Business Analyst", "Quality Assurance Engineer", "Test Engineer", "Automation Engineer",
        "Cybersecurity Analyst", "Network Engineer", "Database Administrator", "Site Reliability Engineer",
        "AI Research Scientist", "Blockchain Developer", "IoT Engineer", "Embedded Systems Engineer",
        "Technical Writer", "Scrum Master", "Agile Coach", "Digital Marketing Manager",
        "SEO Specialist", "Content Strategist", "Growth Hacker", "Sales Engineer",
        "Consultant", "Freelance Developer", "Startup Founder", "CTO",
        "VP of Engineering", "Principal Engineer", "Staff Engineer", "Distinguished Engineer",
        "Junior Developer", "Intern - Software Engineering", "Graduate Trainee", "Associate Developer",
        "React Developer", "Angular Developer", "Python Developer", "Java Developer",
        "Node.js Developer", "Go Developer", "Rust Engineer", "Kotlin Developer"
    };

    private static final String[] SUGGESTED_ROLES = {
        "Software Engineer, Full Stack Developer, Backend Engineer",
        "Data Scientist, ML Engineer, AI Researcher",
        "Product Manager, Technical PM, Program Manager",
        "DevOps Engineer, SRE, Cloud Engineer",
        "Frontend Developer, UI Engineer, React Developer",
        "Technical Lead, Engineering Manager, Architect",
        "Business Analyst, Data Analyst, Strategy Consultant",
        "UX Designer, Product Designer, Interaction Designer",
        "QA Engineer, Test Automation Engineer, SDET",
        "Mobile Developer, iOS Developer, Android Developer",
        "Security Engineer, Penetration Tester, SOC Analyst",
        "Database Engineer, Data Engineer, ETL Developer",
        "Technical Writer, Documentation Engineer, Developer Advocate",
        "Solutions Architect, Enterprise Architect, Cloud Architect",
        "Scrum Master, Agile Coach, Delivery Manager"
    };

    private static final String[] EMAIL_DOMAINS = {
        "gmail.com", "yahoo.com", "outlook.com", "hotmail.com", "protonmail.com",
        "icloud.com", "mail.com", "zoho.com", "yandex.com", "aol.com",
        "rediffmail.com", "live.com", "msn.com", "inbox.com", "fastmail.com"
    };

    private static final String[] RESUME_IMPROVEMENTS = {
        "Add more quantifiable achievements to strengthen impact statements. Include metrics like revenue generated or team size managed.",
        "Improve keyword optimization for ATS systems. Add industry-specific terminology and technical skills mentioned in target job descriptions.",
        "Restructure experience section with action verbs. Use STAR method for bullet points to demonstrate measurable outcomes.",
        "Add a professional summary section highlighting 3-5 key strengths. This gives recruiters a quick overview of your value proposition.",
        "Include relevant certifications and training. Professional development shows commitment to growth and staying current.",
        "Optimize formatting for ATS parsing. Use standard section headers and avoid tables, graphics, or unusual fonts.",
        "Expand the skills section with both technical and soft skills. Group them by category for better readability.",
        "Add links to portfolio, GitHub, or LinkedIn profile. Online presence strengthens credibility and showcases work.",
        "Tailor resume keywords to match the job description. Mirror the language used in postings you're targeting.",
        "Remove outdated experience beyond 10-15 years. Focus on recent, relevant roles that align with career goals.",
        "Strengthen education section with relevant coursework, projects, and GPA if above 3.5. Academic achievements matter for early career.",
        "Add volunteer work and extracurricular activities. These demonstrate leadership, teamwork, and community involvement.",
        "Use consistent formatting throughout. Ensure dates, bullet styles, and fonts are uniform across all sections.",
        "Include a technology stack section for technical roles. List languages, frameworks, tools, and platforms you're proficient in.",
        "Add professional affiliations and memberships. Industry group participation shows engagement with your professional community."
    };

    private static final int PROFILE_COUNT = 3000;
    private static final int RESUME_COUNT = 2000;
    private static final int BATCH_SIZE = 500;

    public DemoDataSeeder(ProfileReviewRepository profileReviewRepository, JdbcTemplate jdbcTemplate) {
        this.profileReviewRepository = profileReviewRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        if (profileReviewRepository.count() >= 100) {
            System.out.println("⏭️  Demo data already seeded, skipping.");
            return;
        }

        long start = System.currentTimeMillis();
        System.out.println("🌱 Seeding 5,000+ demo entries...");

        seedProfileReviews();
        seedResumeReviews();

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("✅ Seeded " + (PROFILE_COUNT + RESUME_COUNT) + " demo entries in " + elapsed + "ms");
    }

    private void seedProfileReviews() {
        String sql = "INSERT INTO app_profile_reviews (" +
            "linkedin_url, user_name, user_title, suggested_roles, overall_score, " +
            "status, created_at, completed_at, email_sent, email_address" +
            ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        for (int batch = 0; batch < PROFILE_COUNT; batch += BATCH_SIZE) {
            int end = Math.min(batch + BATCH_SIZE, PROFILE_COUNT);
            int batchStart = batch;

            jdbcTemplate.batchUpdate(sql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
                @Override
                public void setValues(@NonNull PreparedStatement ps, int i) throws java.sql.SQLException {
                    String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
                    String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
                    String fullName = firstName + " " + lastName;
                    String slug = (firstName + "-" + lastName + "-" + (10000 + random.nextInt(90000))).toLowerCase();
                    String linkedinUrl = "https://linkedin.com/in/" + slug;
                    String title = TITLES[random.nextInt(TITLES.length)];
                    String roles = SUGGESTED_ROLES[random.nextInt(SUGGESTED_ROLES.length)];
                    int score = generateWeightedScore(45, 92, 68, 12);
                    String email = (firstName.toLowerCase() + "." + lastName.toLowerCase() + 
                                   (random.nextInt(100) > 60 ? String.valueOf(random.nextInt(999)) : "")) + 
                                   "@" + EMAIL_DOMAINS[random.nextInt(EMAIL_DOMAINS.length)];

                    LocalDateTime created = randomDateInLastMonths(6);
                    LocalDateTime completed = created.plusMinutes(2 + random.nextInt(28));

                    ps.setString(1, linkedinUrl);
                    ps.setString(2, fullName);
                    ps.setString(3, title);
                    ps.setString(4, roles);
                    ps.setInt(5, score);
                    ps.setString(6, "COMPLETED");
                    ps.setTimestamp(7, Timestamp.valueOf(created));
                    ps.setTimestamp(8, Timestamp.valueOf(completed));
                    ps.setBoolean(9, random.nextInt(100) < 75); // 75% emailed
                    ps.setString(10, email);
                }

                @Override
                public int getBatchSize() {
                    return end - batchStart;
                }
            });
        }
        System.out.println("   📊 Seeded " + PROFILE_COUNT + " LinkedIn profile reviews");
    }

    private void seedResumeReviews() {
        String sql = "INSERT INTO app_resume_reviews (" +
            "file_name, user_name, user_email, ats_score, suggested_roles, " +
            "improvement_suggestions, uploaded_at" +
            ") VALUES (?, ?, ?, ?, ?, ?, ?)";

        for (int batch = 0; batch < RESUME_COUNT; batch += BATCH_SIZE) {
            // Fix: ensure we don't exceed RESUME_COUNT
            int actualEnd = Math.min(batch + BATCH_SIZE, RESUME_COUNT);
            int batchStart = batch;

            jdbcTemplate.batchUpdate(sql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
                @Override
                public void setValues(@NonNull PreparedStatement ps, int i) throws java.sql.SQLException {
                    String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
                    String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
                    String fullName = firstName + " " + lastName;
                    String fileName = firstName.toLowerCase() + "_" + lastName.toLowerCase() + "_resume.pdf";
                    String email = (firstName.toLowerCase() + "." + lastName.toLowerCase() +
                                   (random.nextInt(100) > 60 ? String.valueOf(random.nextInt(999)) : "")) +
                                   "@" + EMAIL_DOMAINS[random.nextInt(EMAIL_DOMAINS.length)];
                    int atsScore = generateWeightedScore(35, 95, 65, 15);
                    String roles = SUGGESTED_ROLES[random.nextInt(SUGGESTED_ROLES.length)];
                    String improvement = RESUME_IMPROVEMENTS[random.nextInt(RESUME_IMPROVEMENTS.length)];
                    LocalDateTime uploaded = randomDateInLastMonths(6);

                    ps.setString(1, fileName);
                    ps.setString(2, fullName);
                    ps.setString(3, email);
                    ps.setInt(4, atsScore);
                    ps.setString(5, roles);
                    ps.setString(6, improvement);
                    ps.setTimestamp(7, Timestamp.valueOf(uploaded));
                }

                @Override
                public int getBatchSize() {
                    return actualEnd - batchStart;
                }
            });
        }
        System.out.println("   📄 Seeded " + RESUME_COUNT + " resume/CV reviews");
    }

    /**
     * Generate a weighted random score centered around `mean` with given `stdDev`,
     * clamped between `min` and `max`.
     */
    private int generateWeightedScore(int min, int max, int mean, int stdDev) {
        int score = (int) Math.round(mean + random.nextGaussian() * stdDev);
        return Math.max(min, Math.min(max, score));
    }

    /**
     * Generate a random LocalDateTime within the last N months.
     */
    private LocalDateTime randomDateInLastMonths(int months) {
        LocalDateTime now = LocalDateTime.now();
        long totalMinutes = (long) months * 30 * 24 * 60;
        long minutesAgo = (long) (random.nextDouble() * totalMinutes);
        return now.minusMinutes(minutesAgo);
    }
}
