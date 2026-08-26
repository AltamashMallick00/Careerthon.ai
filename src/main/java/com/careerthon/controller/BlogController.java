package com.careerthon.controller;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class BlogController {

    private final List<BlogPost> posts = new ArrayList<>();

    public BlogController() {
        // Seed 3 high-quality LinkedIn tips articles
        posts.add(new BlogPost(
            "how-to-optimize-linkedin-headline-recruiter-reach",
            "How to Optimize Your LinkedIn Headline for Recruiter Reach in 2026",
            "LinkedIn Headline Optimization: Beat Recruiter Search Algorithms",
            "Master the exact headline framework used by top professionals to rank on page 1 of recruiter searches and secure organic interview requests in 2026.",
            "Priyanshu Shekhar",
            "May 28, 2026",
            "5 min read",
            "Master the exact headline framework used by top professionals to rank on page 1 of recruiter searches and secure organic interview requests.",
            "<p>Your LinkedIn headline is the single most important real estate on your entire profile when it comes to recruiter discoverability. It is indexed heavily by search algorithms and acts as your direct advertisement when appearing in search results.</p>" +
            "<h3>The Recruiter Search Algorithm Revealed</h3>" +
            "<p>When recruiters use LinkedIn Recruiter, they input specific search strings consisting of Job Titles and Skills. If these exact keywords are in your headline, you are boosted to the top tier of results. A generic headline like 'Seeking new opportunities' makes you completely invisible to bots.</p>" +
            "<h3>The 3-Part Formula for High-Converting Headlines</h3>" +
            "<p>1. <strong>Primary Role / Core Stack</strong> — State your primary function clearly (e.g. Senior Java Full Stack Engineer).</p>" +
            "<p>2. <strong>Key Impact / Domain</strong> — Highlighting scale or business value (e.g. Scaling Distributed Microservices & FinTech Architectures).</p>" +
            "<p>3. <strong>Signature Skills / Tech Stack</strong> — Include target keywords (e.g. Spring Boot | AWS | Microservices | System Design).</p>" +
            "<h3>Example Winning Headline</h3>" +
            "<p><em>Senior Software Engineer @ FinTech | Spring Boot, AWS, Kafka, Microservices | Scaled Payment Systems to 10M+ Users</em></p>" +
            "<p>This layout checks all the boxes: it is rich in keywords for the search bots, it tells hiring managers exactly what you do, and it provides a metrics-driven proof of competence right upfront.</p>",
            "/images/blog_headline.jpg"
        ));

        posts.add(new BlogPost(
            "top-10-ats-friendly-resume-keywords",
            "Top 10 ATS-Friendly Keywords to Beat the Resume Bots",
            "10 ATS-Friendly Keywords to Beat Recruitment Bots",
            "Beat the recruitment algorithms. Discover the exact keyword vectors and terms that parsed resumes need to score highly on ATS platforms.",
            "Priyanshu Shekhar",
            "May 26, 2026",
            "4 min read",
            "Beat the recruitment algorithms. Discover the exact keyword vectors and terms that parsed resumes need to score highly on ATS platforms.",
            "<p>Applicant Tracking Systems (ATS) scan millions of resumes daily, filtering out up to 75% of applicants before a human recruiter ever sees them. To beat the bots, your resume must speak their exact language.</p>" +
            "<h3>Top 10 High-Value Algorithmic Keywords</h3>" +
            "<p>Depending on your technical stack, ensure these structural keyword vectors are represented in your resume:</p>" +
            "<ol>" +
            "<li><strong>System Architecture:</strong> Signals core competence in designing secure and robust structural frameworks.</li>" +
            "<li><strong>Microservices Design:</strong> Extremely high demand in modern cloud-native enterprises.</li>" +
            "<li><strong>Agile Methodologies:</strong> Confirms alignment with rapid sprint delivery patterns.</li>" +
            "<li><strong>Continuous Integration / CI-CD:</strong> Shows ownership of delivery pipeline automation.</li>" +
            "<li><strong>Cross-Functional Collaboration:</strong> Validates leadership and communication competence.</li>" +
            "<li><strong>Cloud Orchestration (AWS, Azure, GCP):</strong> High priority for cloud architecture.</li>" +
            "<li><strong>Process Automation:</strong> Shows you write code to save engineering hours.</li>" +
            "<li><strong>Scalability & Optimization:</strong> Quantifies your engineering effectiveness.</li>" +
            "<li><strong>Security Compliance:</strong> Critical keyword for modern SaaS startups.</li>" +
            "<li><strong>Data Modeling:</strong> Highlights architectural capability rather than basic scripting.</li>" +
            "</ol>" +
            "<h3>Action-Driven Keyword Formatting</h3>" +
            "<p>Never just list these as standalone terms. Weave them naturally into your bullet points alongside metrics: 'Designed microservices architecture using Spring Boot, securing 40% latency reductions.'</p>",
            "/images/blog_ats.jpg"
        ));

        posts.add(new BlogPost(
            "writing-an-irresistible-about-summary-on-linkedin",
            "Writing an Irresistible LinkedIn 'About' Summary: A Step-by-Step Guide",
            "How to Write an Irresistible LinkedIn About Summary",
            "A step-by-step narrative framework for your LinkedIn 'About' summary to capture recruiter attention, present high keyword density, and tell a premium story.",
            "Priyanshu Shekhar",
            "May 24, 2026",
            "6 min read",
            "A step-by-step narrative framework for your LinkedIn 'About' summary to capture recruiter attention, present high keyword density, and tell a premium story.",
            "<p>The LinkedIn 'About' section is your only space for a raw, high-impact professional narrative. It bridges the gap between a dry list of tasks and a compelling executive story.</p>" +
            "<h3>The Three Pillars of an Elite About Section</h3>" +
            "<p>An executive 'About' section should cover three areas: who you are, what you have built, and what technical competencies you bring to the table.</p>" +
            "<h3>Step-by-Step Narrative Framework</h3>" +
            "<h4>Step 1: The Hook</h4>" +
            "<p>Open with a strong statement highlighting your core value and years of experience. E.g., 'Senior Software Engineer with 8+ years specializing in enterprise distributed systems.'</p>" +
            "<h4>Step 2: Core Achievements</h4>" +
            "<p>Provide 2-3 bullet points demonstrating business impact. 'Scaled legacy monolithic databases to distributed clusters, saving $80k in annually managed hosting fees.'</p>" +
            "<h4>Step 3: The Keyword Cloud</h4>" +
            "<p>End with a clean skills directory to satisfy the search algorithms: <em>Core Competencies: Java, Spring Boot, AWS, Kubernetes, SQL, Docker, CI/CD, Microservices</em>.</p>",
            "/images/blog_about.jpg"
        ));
    }

    @GetMapping("/blog")
    public String listBlogPosts(Model model) {
        model.addAttribute("posts", posts);
        return "blog/list";
    }

    @GetMapping("/blog/{slug}")
    public String viewBlogPost(@PathVariable String slug, Model model) {
        Optional<BlogPost> postOpt = posts.stream().filter(p -> p.getSlug().equals(slug)).findFirst();
        if (postOpt.isEmpty()) {
            return "redirect:/blog";
        }
        model.addAttribute("post", postOpt.get());
        return "blog/detail";
    }

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String getSitemap() {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        // Static Pages
        addUrl(sb, "https://Careerthon.AI.onrender.com/", "daily", "1.0");
        addUrl(sb, "https://Careerthon.AI.onrender.com/about", "monthly", "0.8");
        addUrl(sb, "https://Careerthon.AI.onrender.com/careers", "weekly", "0.8");
        addUrl(sb, "https://Careerthon.AI.onrender.com/job-match", "monthly", "0.9");
        addUrl(sb, "https://Careerthon.AI.onrender.com/blog", "daily", "0.9");

        // Blog Posts
        for (BlogPost post : posts) {
            addUrl(sb, "https://Careerthon.AI.onrender.com/blog/" + post.getSlug(), "weekly", "0.7");
        }

        sb.append("</urlset>");
        return sb.toString();
    }

    private void addUrl(StringBuilder sb, String loc, String freq, String priority) {
        sb.append("    <url>\n");
        sb.append("        <loc>").append(loc).append("</loc>\n");
        sb.append("        <changefreq>").append(freq).append("</changefreq>\n");
        sb.append("        <priority>").append(priority).append("</priority>\n");
        sb.append("    </url>\n");
    }

    // BlogPost Inner Class
    public static class BlogPost {
        private String slug;
        private String title;
        private String metaTitle;
        private String metaDesc;
        private String author;
        private String date;
        private String readTime;
        private String summary;
        private String content;
        private String imageUrl;

        public BlogPost(String slug, String title, String metaTitle, String metaDesc, String author, String date, String readTime, String summary, String content, String imageUrl) {
            this.slug = slug;
            this.title = title;
            this.metaTitle = metaTitle;
            this.metaDesc = metaDesc;
            this.author = author;
            this.date = date;
            this.readTime = readTime;
            this.summary = summary;
            this.content = content;
            this.imageUrl = imageUrl;
        }

        public String getSlug() { return slug; }
        public String getTitle() { return title; }
        public String getMetaTitle() { return metaTitle; }
        public String getMetaDesc() { return metaDesc; }
        public String getAuthor() { return author; }
        public String getDate() { return date; }
        public String getReadTime() { return readTime; }
        public String getSummary() { return summary; }
        public String getContent() { return content; }
        public String getImageUrl() { return imageUrl; }
    }
}
