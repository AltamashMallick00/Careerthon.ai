package com.careerthon;

import com.careerthon.model.ProfileReview;
import com.careerthon.model.ResumeReview;
import com.careerthon.repository.ProfileReviewRepository;
import com.careerthon.repository.ResumeReviewRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ReviewAndScanIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProfileReviewRepository profileReviewRepository;

    @Autowired
    private ResumeReviewRepository resumeReviewRepository;

    @Test
    public void testLinkedInUrlScan_EndToEnd() throws Exception {
        // 1. Submit LinkedIn URL
        String redirectUrl = mockMvc.perform(post("/review/submit")
                        .param("linkedinUrl", "https://linkedin.com/in/priyanshushekhar"))
                .andExpect(status().is3xxRedirection())
                .andReturn().getResponse().getRedirectedUrl();

        assertNotNull(redirectUrl);
        assertTrue(redirectUrl.contains("/review/analyzing/"));

        String reviewIdStr = redirectUrl.substring(redirectUrl.lastIndexOf("/") + 1);
        Long reviewId = Long.parseLong(reviewIdStr);

        // 2. Trigger AI Analysis
        mockMvc.perform(get("/review/analyze/" + reviewId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.redirectUrl").value("/report/" + reviewId));

        // 3. View Report
        mockMvc.perform(get("/report/" + reviewId))
                .andExpect(status().isOk());
    }

    @Test
    public void testResumeUploadScan_EndToEnd() throws Exception {
        String content = "Priyanshu Shekhar\nSoftware Engineer\nEmail: priya@careerthon.ai\nPhone: 9876543210\n" +
                "Skills: Java, Spring Boot, React, AWS, Docker, Kubernetes, SQL\n" +
                "Experience: Led engineering of microservices, reduced API latency by 45% for 10M requests.\n" +
                "Education: B.Tech Computer Science";
        MockMultipartFile file = new MockMultipartFile("resume", "priyanshu_resume.txt", "text/plain", content.getBytes(StandardCharsets.UTF_8));

        String redirectUrl = mockMvc.perform(multipart("/review/upload")
                        .file(file)
                        .param("userName", "Priyanshu Shekhar")
                        .param("userEmail", "priya@careerthon.ai"))
                .andExpect(status().is3xxRedirection())
                .andReturn().getResponse().getRedirectedUrl();

        assertNotNull(redirectUrl);
        assertTrue(redirectUrl.contains("/resume/results/"));

        String resumeIdStr = redirectUrl.substring(redirectUrl.lastIndexOf("/") + 1);
        Long resumeId = Long.parseLong(resumeIdStr);

        // View Resume ATS Results
        mockMvc.perform(get("/resume/results/" + resumeId))
                .andExpect(status().isOk());
    }
}
