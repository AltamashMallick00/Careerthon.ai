package com.careerthon.service;

import com.careerthon.model.ProfileReview;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
@SuppressWarnings("null")
public class EmailService {

    private final JavaMailSender mailSender;

    @Autowired
    public EmailService(@Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public boolean sendReport(String toEmail, String reportUrl, String userName) {
        if (mailSender == null) {
            System.err.println("⚠️ Email sending skipped — JavaMailSender not configured (MAIL_PASSWORD env var missing).");
            return false;
        }
        try {
            SimpleMailMessage m = new SimpleMailMessage();
            m.setFrom("Careerthon.AI <no-reply@Careerthon.AI.onrender.com>");
            m.setTo(toEmail);
            m.setSubject("Your LinkedIn Profile Review is Ready! ✨");
            m.setText("Hi " + userName + ",\n\n" +
                      "Great news! Your LinkedIn profile review is now complete. " +
                      "You've taken the first step towards a more powerful professional presence.\n\n" +
                      "You can view your detailed report and actionable insights here:\n" +
                      reportUrl + "\n\n" +
                      "If you have any questions, feel free to reach out to us.\n\n" +
                      "Best regards,\n" +
                      "The Careerthon.AI Team ⚡");
            mailSender.send(m);
            System.out.println("✅ Real email successfully sent to: " + toEmail);
            return true;
        } catch (Exception e) {
            System.err.println("❌ Failed to send real email to " + toEmail + ": " + e.getMessage());
            return false;
        }
    }

    public boolean sendReportWithPdf(String toEmail, ProfileReview review) {
        byte[] pdfBytes = generatePdfReport(review);

        if (mailSender == null) {
            System.err.println("⚠️ Email sending with PDF skipped — JavaMailSender not configured (MAIL_PASSWORD env var missing).");
            System.out.println("   [Simulated Send] To: " + toEmail);
            System.out.println("   [Simulated Send] Subject: Your Careerthon.AI Profile Analysis Report is Ready! 📄✨");
            System.out.println("   [Simulated Send] PDF size: " + (pdfBytes != null ? pdfBytes.length : 0) + " bytes successfully generated.");
            return true;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            // Enable multipart mode for adding PDF attachment
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom("Careerthon.AI <no-reply@Careerthon.AI.onrender.com>");
            helper.setTo(toEmail);
            helper.setSubject("Your Careerthon.AI Profile Analysis Report is Ready! 📄✨");
            
            String emailText = "Dear " + review.getUserName() + ",\n\n" +
                               "Great news! Your LinkedIn profile has been fully audited by our AI Intelligence Engine.\n\n" +
                               "We have prepared a comprehensive, highly personalized review report for you. " +
                               "In addition to viewing your live dashboard, we have attached a fully detailed PDF report " +
                               "containing all your personalized keyword density alignments, ATS compliance analysis, and " +
                               "actionable roadmap recommendations to this email.\n\n" +
                               "Key Metrics Summary:\n" +
                               "- Overall Performance Score: " + review.getOverallScore() + "% (" + review.getScoreLabel() + ")\n" +
                               "- Targeted Core Roles: " + review.getSuggestedRoles() + "\n\n" +
                               "Please find your secure PDF audit report attached to this message.\n\n" +
                               "To your career success,\n" +
                               "The Careerthon.AI Team ⚡";

            helper.setText(emailText);

            if (pdfBytes != null) {
                helper.addAttachment("Careerthon.AI_LinkedIn_Audit_" + review.getUserName().replace(" ", "_") + ".pdf",
                        new ByteArrayResource(pdfBytes));
            }

            mailSender.send(mimeMessage);
            System.out.println("✅ Real email with iText PDF attachment successfully sent to: " + toEmail);
            return true;
        } catch (Exception e) {
            System.err.println("❌ Failed to send email with iText PDF to " + toEmail + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean sendRecruiterInvite(String toEmail, String candidateName, String recruiterName, String recruiterEmail, String subject, String messageText) {
        if (mailSender == null) {
            System.err.println("⚠️ Recruiter invite skipped — JavaMailSender not configured (Simulating Success to console).");
            System.out.println("   [Simulated Send] To: " + toEmail + " (" + candidateName + ")");
            System.out.println("   [Simulated Send] ReplyTo: " + recruiterEmail);
            System.out.println("   [Simulated Send] Subject: " + subject);
            System.out.println("   [Simulated Send] Message: " + messageText);
            return true;
        }
        try {
            SimpleMailMessage m = new SimpleMailMessage();
            m.setFrom("Careerthon.AI Outreach <no-reply@Careerthon.AI.onrender.com>");
            m.setReplyTo(recruiterEmail);
            m.setTo(toEmail);
            m.setSubject(subject);
            m.setText("Dear " + candidateName + ",\n\n" +
                      messageText + "\n\n" +
                      "Feel free to reply directly to this email to coordinate next steps.\n\n" +
                      "Best regards,\n" +
                      recruiterName + " via Careerthon.AI ⚡\n" +
                      recruiterEmail);
            mailSender.send(m);
            System.out.println("✅ Recruiter email successfully sent to: " + toEmail);
            return true;
        } catch (Exception e) {
            System.err.println("❌ Failed to send recruiter email to " + toEmail + ": " + e.getMessage());
            return false;
        }
    }

    private byte[] generatePdfReport(ProfileReview review) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Color Palette
            DeviceRgb primaryColor = new DeviceRgb(37, 99, 235); // Royal Blue #2563EB
            DeviceRgb darkColor = new DeviceRgb(15, 23, 42); // Slate 900
            DeviceRgb successColor = new DeviceRgb(16, 185, 129); // Emerald 500

            // Header Section
            Paragraph title = new Paragraph("Careerthon.AI PROFILE REVIEW")
                    .setFontSize(22)
                    .setBold()
                    .setFontColor(primaryColor)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(title);

            Paragraph subtitle = new Paragraph("LinkedIn Algorithmic Audit & Key ATS Alignment Indicators")
                    .setFontSize(11)
                    .setItalic()
                    .setFontColor(darkColor)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(15);
            document.add(subtitle);

            // Separator
            SolidLine line = new SolidLine(1f);
            line.setColor(primaryColor);
            LineSeparator ls = new LineSeparator(line);
            document.add(ls);

            // Candidate Summary Card
            document.add(new Paragraph("\n"));
            Table summaryTable = new Table(2);
            summaryTable.useAllAvailableWidth();

            summaryTable.addCell(new Cell().add(new Paragraph("Candidate Name:").setBold()).setBorder(Border.NO_BORDER));
            summaryTable.addCell(new Cell().add(new Paragraph(review.getUserName() != null ? review.getUserName() : "Valued Candidate")).setBorder(Border.NO_BORDER));

            summaryTable.addCell(new Cell().add(new Paragraph("Overall ATS Score:").setBold()).setBorder(Border.NO_BORDER));
            summaryTable.addCell(new Cell().add(new Paragraph(review.getOverallScore() + "% (" + review.getScoreLabel() + ")").setBold().setFontColor(successColor)).setBorder(Border.NO_BORDER));

            summaryTable.addCell(new Cell().add(new Paragraph("LinkedIn Profile URL:").setBold()).setBorder(Border.NO_BORDER));
            summaryTable.addCell(new Cell().add(new Paragraph(review.getLinkedinUrl())).setBorder(Border.NO_BORDER));

            summaryTable.addCell(new Cell().add(new Paragraph("Target Core Roles:").setBold()).setBorder(Border.NO_BORDER));
            summaryTable.addCell(new Cell().add(new Paragraph(review.getSuggestedRoles() != null ? review.getSuggestedRoles() : "Not Specified")).setBorder(Border.NO_BORDER));

            document.add(summaryTable);
            document.add(new Paragraph("\n"));
            document.add(ls);

            // Audit Sections
            addSection(document, "1. Headline Optimization", review.getHeadlineRecommendation(), primaryColor);
            addSection(document, "2. About Summary Analysis", review.getAboutRecommendation(), primaryColor);
            addSection(document, "3. Core Skills Gap & Keywords", review.getSkillsRecommendation(), primaryColor);
            addSection(document, "4. Work Experience & Accomplishments", review.getExperienceRecommendation(), primaryColor);
            addSection(document, "5. ATS Compatibility & Search Index", review.getAtsRecommendation(), primaryColor);
            addSection(document, "6. Visibility & Recruiter Optimization", review.getVisibilityRecommendation(), primaryColor);
            addSection(document, "7. Industry Benchmark Analysis", review.getIndustryBenchmark(), primaryColor);
            addSection(document, "8. Top Actionable Insights", review.getActionableInsights(), successColor);

            // Footer info
            document.add(new Paragraph("\n\n"));
            document.add(ls);
            Paragraph pdfFooter = new Paragraph("Generated by Careerthon.AI Profile Intelligence Systems © 2026. All Rights Reserved.")
                    .setFontSize(8)
                    .setFontColor(new DeviceRgb(148, 163, 184))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(15);
            document.add(pdfFooter);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            System.err.println("❌ Error generating iText PDF report: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private void addSection(Document doc, String title, String body, DeviceRgb titleColor) {
        if (body == null || body.trim().isEmpty()) return;
        doc.add(new Paragraph("\n" + title)
                .setFontSize(12)
                .setBold()
                .setFontColor(titleColor)
                .setMarginTop(8)
                .setMarginBottom(3));
        doc.add(new Paragraph(body)
                .setFontSize(9)
                .setFontColor(new DeviceRgb(71, 85, 105))
                .setMarginBottom(8));
    }
}
