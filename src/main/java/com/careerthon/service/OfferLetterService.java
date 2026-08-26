package com.careerthon.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class OfferLetterService {

    public byte[] generateOfferLetter(String candidateName, String role, String startDate, String stipend, String issueDate) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Header
            document.add(new Paragraph("Careerthon.AI")
                    .setFontSize(28)
                    .setBold()
                    .setFontColor(ColorConstants.BLUE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(5));
            
            document.add(new Paragraph("Empowering Careers, Accelerating Growth")
                    .setFontSize(10)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(30));

            // Date and Candidate Details
            document.add(new Paragraph("Date: " + issueDate).setTextAlignment(TextAlignment.RIGHT).setMarginBottom(20));
            document.add(new Paragraph("Dear " + candidateName + ",").setBold().setMarginBottom(10));

            // Body Paragraphs
            document.add(new Paragraph("We are thrilled to offer you the position of " + role + " at Careerthon.AI.")
                    .setMarginBottom(10));
            
            document.add(new Paragraph("At Careerthon.AI, we believe in fostering innovation and empowering our team members to achieve their highest potential. Your skills and background stood out to us, and we are confident that you will make a significant impact on our mission.")
                    .setMarginBottom(10));
            
            document.add(new Paragraph("Offer Details:")
                    .setBold().setMarginBottom(5));
            
            // Details Table
            Table table = new Table(UnitValue.createPercentArray(new float[]{30, 70})).useAllAvailableWidth();
            table.setMarginBottom(20);
            
            table.addCell(new Cell().add(new Paragraph("Role:")).setBorder(Border.NO_BORDER).setBold());
            table.addCell(new Cell().add(new Paragraph(role)).setBorder(Border.NO_BORDER));
            
            table.addCell(new Cell().add(new Paragraph("Start Date:")).setBorder(Border.NO_BORDER).setBold());
            table.addCell(new Cell().add(new Paragraph(startDate)).setBorder(Border.NO_BORDER));
            
            table.addCell(new Cell().add(new Paragraph("Stipend/Salary:")).setBorder(Border.NO_BORDER).setBold());
            table.addCell(new Cell().add(new Paragraph(stipend)).setBorder(Border.NO_BORDER));

            document.add(table);

            document.add(new Paragraph("Please review this offer letter and indicate your acceptance by signing and returning a copy to us prior to your start date. We look forward to welcoming you to the team!")
                    .setMarginBottom(40));

            document.add(new Paragraph("Sincerely,").setMarginBottom(20));

            // Signatures Section
            Table sigTable = new Table(UnitValue.createPercentArray(new float[]{100})).useAllAvailableWidth();
            
            Cell sig1 = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER);
            sig1.add(new Paragraph("Priyanshu Shekhar").setItalic().setFontColor(ColorConstants.BLUE).setFontSize(16).setMarginBottom(5));
            sig1.add(new Paragraph("Priyanshu Shekhar\nFounder & Lead Architect").setBold().setFontSize(10));
            
            sigTable.addCell(sig1);

            document.add(sigTable);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating offer letter PDF", e);
        }
    }
}
