package com.careerthon;

import com.careerthon.util.DocumentTextExtractor;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

public class DocumentTextExtractorTest {

    @Test
    void testExtractPlainText() {
        String content = "Priyanshu Shekhar - Senior Java Full Stack Developer with Spring Boot and AWS experience.";
        MockMultipartFile file = new MockMultipartFile("resume.txt", "resume.txt", "text/plain", content.getBytes(StandardCharsets.UTF_8));
        String extracted = DocumentTextExtractor.extractText(file);
        assertTrue(extracted.contains("Priyanshu Shekhar"));
        assertTrue(extracted.contains("Spring Boot"));
    }

    @Test
    void testExtractDocxText() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry = new ZipEntry("word/document.xml");
            zos.putNextEntry(entry);
            String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:body><w:p><w:r><w:t>Priyanshu Shekhar</w:t></w:r><w:r><w:t> - Full Stack Lead</w:t></w:r></w:p><w:p><w:r><w:t>Skills: Java, Python, Spring Boot, Docker, Redis, Kubernetes</w:t></w:r></w:p></w:body></w:document>";
            zos.write(xml.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }

        MockMultipartFile file = new MockMultipartFile("resume.docx", "resume.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", baos.toByteArray());
        String extracted = DocumentTextExtractor.extractText(file);
        assertTrue(extracted.contains("Priyanshu Shekhar"));
        assertTrue(extracted.contains("Spring Boot"));
        assertTrue(extracted.contains("Kubernetes"));
    }
}
