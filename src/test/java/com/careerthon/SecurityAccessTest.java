package com.careerthon;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityAccessTest {

    @Autowired
    private MockMvc mockMvc;

    // ── 1. Public Pages (Accessible for browsing & navigation without Login) ───

    @Test
    public void testPublicPages_AllowedForAnonymous() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/about"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/careers"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/features"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/blog"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/signup"))
                .andExpect(status().isOk());

        // Users can browse and open all feature showcase pages
        mockMvc.perform(get("/review"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/job-match"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/resume"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/ai-tools/recruiter-outreach"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/ai-tools/roadmap"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/ai-tools/referral-finder"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/lms"))
                .andExpect(status().isOk());
    }

    // ── 2. Protected Dashboards & Actions (Anonymous Users Redirected to /login) ───

    @Test
    public void testProtectedActions_RedirectToLogin_WhenAnonymous() throws Exception {
        // AI Review submission requires authentication
        mockMvc.perform(post("/review/submit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        // Student Dashboard requires authentication
        mockMvc.perform(get("/student/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        // Admin Dashboard requires authentication
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    // ── 3. Authenticated Student/User Access ────────────────────────────────

    @Test
    @WithMockUser(username = "Priyanshu123", roles = {"STUDENT", "USER"})
    public void testProtectedDashboards_AllowedForAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/student/dashboard"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "teststudent", roles = {"STUDENT", "USER"})
    public void testAdminEndpoints_ForbiddenForRegularUser() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isForbidden());
    }

    // ── 4. Admin Access ───────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "Priyanshu123", roles = {"ADMIN", "STUDENT", "USER"})
    public void testAdminEndpoints_AllowedForAdmin() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk());
    }
}
