package com.careerthon.service;

import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class SpecExportService {

    public byte[] generateMasterSpec() throws IOException {
        try (XWPFDocument document = new XWPFDocument()) {
            
            // --- Cover Page ---
            XWPFParagraph titlePara = document.createParagraph();
            titlePara.setAlignment(ParagraphAlignment.CENTER);
            titlePara.setSpacingBefore(1000);
            XWPFRun titleRun = titlePara.createRun();
            titleRun.setText("Careerthon.AI");
            titleRun.setBold(true);
            titleRun.setFontSize(48);
            titleRun.setColor("D4A017");

            XWPFParagraph subTitlePara = document.createParagraph();
            subTitlePara.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun subTitleRun = subTitlePara.createRun();
            subTitleRun.setText("MASTER DEVELOPER PROMPT");
            subTitleRun.setBold(true);
            subTitleRun.setFontSize(24);
            subTitleRun.setColor("1A1A2E");

            XWPFParagraph versionPara = document.createParagraph();
            versionPara.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun versionRun = versionPara.createRun();
            versionRun.setText("Complete Technical Specification — All 80 Features");
            versionRun.setFontSize(14);
            versionRun.setColor("555555");

            document.createParagraph().createRun().addBreak(BreakType.PAGE);

            // --- Global Rules ---
            addHeading(document, "🎨 GLOBAL DESIGN RULES", "D4A017", 24);
            addParagraph(document, "Every new page and feature must match the existing Careerthon.AI site EXACTLY. Dark theme, gold accents, and Mike-1 widget are mandatory.");
            
            // --- Feature 1 Example (to show structure) ---
            addHeading(document, "FEATURE 1: Application Tracker (Kanban)", "1A1A2E", 18);
            addParagraph(document, "Route: /tracker | Auth: Login Required");
            addBullet(document, "6 Columns: Wishlist, Applied, Screening, Interview, Offer, Rejected");
            addBullet(document, "Drag & Drop functionality using SortableJS");
            addBullet(document, "Fields: Company, Title, Location, Salary, Priority, Notes");

            // --- Section: AI Writing Tools ---
            addHeading(document, "🤖 AI Writing Tools (1-10)", "1A1A2E", 16);
            addBullet(document, "LinkedIn Headline Generator");
            addBullet(document, "Bio/Summary Rewriter");
            addBullet(document, "Achievement Bullet Generator");
            addBullet(document, "Recommendation Letter Writer");
            addBullet(document, "Thank You Email Generator");

            // ... More sections can be added here ...
            
            addHeading(document, "✅ GLOBAL ACCEPTANCE CRITERIA", "D4A017", 20);
            addBullet(document, "Design matches existing Careerthon.AI site");
            addBullet(document, "MIKE-1 widget present on every page");
            addBullet(document, "Mobile responsive on all new pages");
            addBullet(document, "All data persists to DB per user");

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.write(out);
            return out.toByteArray();
        }
    }

    private void addHeading(XWPFDocument doc, String text, String color, int size) {
        XWPFParagraph p = doc.createParagraph();
        p.setSpacingBefore(400);
        XWPFRun r = p.createRun();
        r.setText(text);
        r.setBold(true);
        r.setFontSize(size);
        r.setColor(color);
    }

    private void addParagraph(XWPFDocument doc, String text) {
        XWPFParagraph p = doc.createParagraph();
        p.setSpacingAfter(200);
        XWPFRun r = p.createRun();
        r.setText(text);
        r.setFontSize(11);
    }

    private void addBullet(XWPFDocument doc, String text) {
        XWPFParagraph p = doc.createParagraph();
        p.setNumID(java.math.BigInteger.valueOf(1)); // Simplified numbering
        XWPFRun r = p.createRun();
        r.setText("• " + text);
        r.setFontSize(11);
    }
}
