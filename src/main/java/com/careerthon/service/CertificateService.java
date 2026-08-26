package com.careerthon.service;

import com.careerthon.model.Certificate;
import com.careerthon.model.Course;
import com.careerthon.model.Internship;
import com.careerthon.model.User;
import com.careerthon.repository.CertificateRepository;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class CertificateService {

    private final CertificateRepository certificateRepository;

    // Define a directory to store generated PDFs in the current workspace or temp
    private static final String PDF_DIR = "target/classes/static/certificates/";

    public CertificateService(CertificateRepository certificateRepository) {
        this.certificateRepository = certificateRepository;
        File dir = new File(PDF_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public Certificate generateCourseCertificate(User student, Course course) {
        String certId = UUID.randomUUID().toString();
        Certificate cert = new Certificate();
        cert.setStudent(student);
        cert.setCertificateNumber(certId);
        cert.setCertificateType("COURSE_COMPLETION");
        cert.setCourseName(course.getTitle());
        cert.setQrDataUrl("https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=Careerthon.AI-cert-" + certId);

        String fileName = certId + ".pdf";
        cert.setPdfUrl("/certificates/" + fileName);

        generatePdf(cert, student.getFullName(), "has successfully completed the course", course.getTitle(), fileName);

        return certificateRepository.save(cert);
    }

    public Certificate generateInternshipCertificate(User student, Internship internship) {
        String certId = UUID.randomUUID().toString();
        Certificate cert = new Certificate();
        cert.setStudent(student);
        cert.setCertificateNumber(certId);
        cert.setCertificateType("INTERNSHIP_COMPLETION");
        cert.setInternshipName(internship.getTitle());
        cert.setQrDataUrl("https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=Careerthon.AI-cert-" + certId);

        String fileName = certId + ".pdf";
        cert.setPdfUrl("/certificates/" + fileName);

        generatePdf(cert, student.getFullName(), "has successfully completed the internship program", internship.getTitle(), fileName);

        return certificateRepository.save(cert);
    }

    public Certificate generateOfferLetter(User student, Internship internship) {
        String certId = UUID.randomUUID().toString();
        Certificate cert = new Certificate();
        cert.setStudent(student);
        cert.setCertificateNumber(certId);
        cert.setCertificateType("OFFER_LETTER");
        cert.setInternshipName(internship.getTitle());
        cert.setQrDataUrl("https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=Careerthon.AI-cert-" + certId);

        String fileName = certId + ".pdf";
        cert.setPdfUrl("/certificates/" + fileName);

        generatePdf(cert, student.getFullName(), "is officially offered the role for internship program", internship.getTitle(), fileName);

        return certificateRepository.save(cert);
    }

    public Certificate generateLetterOfRecommendation(User student, Internship internship) {
        String certId = UUID.randomUUID().toString();
        Certificate cert = new Certificate();
        cert.setStudent(student);
        cert.setCertificateNumber(certId);
        cert.setCertificateType("LOR");
        cert.setInternshipName(internship.getTitle());
        cert.setQrDataUrl("https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=Careerthon.AI-cert-" + certId);

        String fileName = certId + ".pdf";
        cert.setPdfUrl("/certificates/" + fileName);

        generatePdf(cert, student.getFullName(), "is highly recommended for their exceptional work in", internship.getTitle(), fileName);

        return certificateRepository.save(cert);
    }
    private void generatePdf(Certificate cert, String studentName, String actionText, String itemName, String fileName) {
        try {
            PdfWriter writer = new PdfWriter(new FileOutputStream(PDF_DIR + fileName));
            PdfDocument pdf = new PdfDocument(writer);
            // Landscape orientation
            pdf.setDefaultPageSize(PageSize.A4.rotate());
            Document document = new Document(pdf);

            document.add(new Paragraph("Careerthon.AI")
                    .setFontSize(40)
                    .setBold()
                    .setFontColor(ColorConstants.BLUE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(30));

            document.add(new Paragraph("CERTIFICATE OF COMPLETION")
                    .setFontSize(25)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(10));

            document.add(new LineSeparator(new SolidLine(1f)).setMarginTop(20).setMarginBottom(20));

            document.add(new Paragraph("This is to certify that")
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph(studentName)
                    .setFontSize(35)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(10)
                    .setMarginBottom(10));

            document.add(new Paragraph(actionText)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph(itemName)
                    .setFontSize(22)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(10)
                    .setMarginBottom(20));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
            document.add(new Paragraph("Date: " + cert.getIssuedAt().format(formatter))
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("Certificate ID: " + cert.getCertificateNumber())
                    .setFontSize(12)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER));

            // Add QR Code
            try {
                ImageData data = ImageDataFactory.create(java.net.URI.create(cert.getQrDataUrl()).toURL());
                Image qrImage = new Image(data);
                qrImage.setWidth(100);
                qrImage.setHeight(100);
                qrImage.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);
                qrImage.setMarginTop(20);
                document.add(qrImage);
            } catch (Exception e) {
                System.err.println("Could not generate QR code image: " + e.getMessage());
            }

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generating PDF certificate: " + e.getMessage());
        }
    }
}
