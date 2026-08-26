package com.careerthon.controller;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.ByteArrayOutputStream;

@Controller
@SuppressWarnings("null")
public class OutreachController {

    @GetMapping({"/ai-tools/recruiter-outreach", "/recruiter-outreach", "/outreach"})
    public String showOutreachForm(Model model) {
        model.addAttribute("recruiterName", "");
        model.addAttribute("companyName", "");
        model.addAttribute("targetRole", "");
        model.addAttribute("pitchTone", "Professional & Confident");
        model.addAttribute("keyStrength", "");
        return "ai/outreach";
    }

    @PostMapping("/ai-tools/recruiter-outreach/generate")
    public String generateOutreach(
            @RequestParam("recruiterName") String recruiterName,
            @RequestParam("companyName") String companyName,
            @RequestParam("targetRole") String targetRole,
            @RequestParam(value = "pitchTone", defaultValue = "Professional") String pitchTone,
            @RequestParam(value = "keyStrength", defaultValue = "") String keyStrength,
            Model model) {

        String rName = recruiterName.trim().isEmpty() ? "Hiring Manager" : recruiterName.trim();
        String cName = companyName.trim().isEmpty() ? "your team" : companyName.trim();
        String tRole = targetRole.trim().isEmpty() ? "Engineering Role" : targetRole.trim();
        String strength = keyStrength.trim().isEmpty() ? "building high-scalability systems and modern full-stack architectures" : keyStrength.trim();

        // 1. InMail (Short, 3-sentence high-impact pitch)
        String inmail = String.format(
                "Hi %s,\n\nI noticed %s is expanding its %s team and wanted to reach out directly. " +
                "With proven experience in %s, I’ve delivered measurable results that align closely with your team's goals.\n\n" +
                "I’d love to connect for a quick 5-minute chat regarding how I can add immediate value to %s. Looking forward to connecting!",
                rName, cName, tRole, strength, cName
        );

        // 2. Direct Cold Email
        String coldEmailSubject = String.format("Exploring %s Opportunities at %s | Value Pitch", tRole, cName);
        String coldEmailBody = String.format(
                "Dear %s,\n\n" +
                "I hope this email finds you well.\n\n" +
                "I have been closely following %s’s recent growth and impact in the industry. As a dedicated technical professional specializing in %s, " +
                "I am writing to express my strong interest in joining your team for %s opportunities.\n\n" +
                "Key Value Highlights:\n" +
                "• %s\n" +
                "• Proven track record of high-quality execution, clean architecture, and performance optimization.\n" +
                "• Experience collaborating in fast-paced, high-impact environments.\n\n" +
                "I have attached my resume for your review. Would you be open to a brief 10-minute introductory call next week?\n\n" +
                "Thank you for your time and consideration.\n\n" +
                "Best regards,\n[Your Name]\n[Your Contact Info]",
                rName, cName, tRole, tRole, strength
        );

        // 3. Application Follow-up Email
        String followUpSubject = String.format("Following Up: %s Application | %s", tRole, cName);
        String followUpBody = String.format(
                "Hi %s,\n\n" +
                "I recently submitted my application for the %s position at %s. Given my background in %s, " +
                "I am very excited about the possibility of contributing to your team's upcoming initiatives.\n\n" +
                "I wanted to briefly re-emphasize my key focus on %s. Please let me know if you need any additional portfolio samples or references from my end.\n\n" +
                "Thank you again for your time, and I look forward to hearing about next steps!\n\n" +
                "Best regards,\n[Your Name]",
                rName, tRole, cName, tRole, strength
        );

        model.addAttribute("recruiterName", rName);
        model.addAttribute("companyName", cName);
        model.addAttribute("targetRole", tRole);
        model.addAttribute("pitchTone", pitchTone);
        model.addAttribute("keyStrength", strength);
        model.addAttribute("inmail", inmail);
        model.addAttribute("coldEmailSubject", coldEmailSubject);
        model.addAttribute("coldEmailBody", coldEmailBody);
        model.addAttribute("followUpSubject", followUpSubject);
        model.addAttribute("followUpBody", followUpBody);

        return "ai/outreach";
    }

    @GetMapping("/ai-tools/recruiter-outreach/download-pdf")
    public ResponseEntity<byte[]> downloadPdf(
            @RequestParam(value = "recruiterName", defaultValue = "Recruiter") String recruiterName,
            @RequestParam(value = "companyName", defaultValue = "Company") String companyName,
            @RequestParam(value = "targetRole", defaultValue = "Target Role") String targetRole,
            @RequestParam(value = "inmail", defaultValue = "") String inmail,
            @RequestParam(value = "coldEmailSubject", defaultValue = "") String coldEmailSubject,
            @RequestParam(value = "coldEmailBody", defaultValue = "") String coldEmailBody) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("CAREERTHON.AI — RECRUITER OUTREACH BRIEF")
                .setBold().setFontSize(18).setFontColor(ColorConstants.BLUE).setMarginBottom(15));

        document.add(new Paragraph("Target Recruiter: " + recruiterName + " | Company: " + companyName + " | Role: " + targetRole)
                .setItalic().setFontSize(10).setMarginBottom(20));

        document.add(new Paragraph("1. LINKEDIN INMAIL PITCH").setBold().setFontSize(12));
        document.add(new Paragraph(inmail).setFontSize(10).setMarginBottom(20));

        document.add(new Paragraph("2. DIRECT COLD EMAIL PITCH").setBold().setFontSize(12));
        document.add(new Paragraph("Subject: " + coldEmailSubject).setBold().setFontSize(10));
        document.add(new Paragraph(coldEmailBody).setFontSize(10).setMarginBottom(20));

        document.close();

        byte[] pdfBytes = baos.toByteArray();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Careerthon_Outreach_Brief.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
