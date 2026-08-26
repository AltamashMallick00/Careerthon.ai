package com.careerthon.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "certificates")
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User student;

    @Column(nullable = false, unique = true)
    private String certificateNumber;

    @Column(nullable = false)
    private String certificateType; // COURSE_COMPLETION, INTERNSHIP_COMPLETION, OFFER_LETTER, LOR

    private String courseName;
    private String internshipName;

    private String pdfUrl;
    
    @Column(length = 1000)
    private String qrDataUrl; // URL to the generated QR code or the link it points to

    @Column(nullable = false)
    private LocalDateTime issuedAt = LocalDateTime.now();

    public Certificate() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public String getCertificateNumber() { return certificateNumber; }
    public void setCertificateNumber(String certificateNumber) { this.certificateNumber = certificateNumber; }

    public String getCertificateType() { return certificateType; }
    public void setCertificateType(String certificateType) { this.certificateType = certificateType; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getInternshipName() { return internshipName; }
    public void setInternshipName(String internshipName) { this.internshipName = internshipName; }

    public String getPdfUrl() { return pdfUrl; }
    public void setPdfUrl(String pdfUrl) { this.pdfUrl = pdfUrl; }

    public String getQrDataUrl() { return qrDataUrl; }
    public void setQrDataUrl(String qrDataUrl) { this.qrDataUrl = qrDataUrl; }

    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }
}
