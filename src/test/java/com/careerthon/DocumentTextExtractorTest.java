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
    }

    @Test
    void testExtractLargePdfWithNestedParentheses_NoStackOverflow() {
        // Construct a large PDF text content with 10,000 parentheses and escaped characters
        StringBuilder sb = new StringBuilder();
        sb.append("%PDF-1.4\n1 0 obj\n<< /Length 20000 >>\nstream\nBT\n/F1 12 Tf\n");
        for (int i = 0; i < 5000; i++) {
            sb.append("(Senior Software Engineer (Java \\(Spring Boot\\) & Cloud \\\\ AWS) [Metric: +45% latency reduction]) Tj\n");
            sb.append("<48656C6C6F20576F726C642054657374> Tj\n");
        }
        sb.append("ET\nendstream\nendobj\n%%EOF");

        MockMultipartFile file = new MockMultipartFile("complex_resume.pdf", "complex_resume.pdf", "application/pdf", sb.toString().getBytes(StandardCharsets.ISO_8859_1));
        assertDoesNotThrow(() -> {
            String extracted = DocumentTextExtractor.extractText(file);
            assertNotNull(extracted);
            assertTrue(extracted.contains("Senior Software Engineer"));
            assertTrue(extracted.contains("Spring Boot"));
            assertTrue(extracted.contains("Hello World Test"));
        });
    }

    @Test
    void testExtractCompressedPdfStream() throws Exception {
        byte[] uncompressed = "BT /F1 12 Tf (Priyanshu Shekhar) Tj (Lead Backend Engineer) Tj ET".getBytes(StandardCharsets.ISO_8859_1);
        java.util.zip.Deflater deflater = new java.util.zip.Deflater();
        deflater.setInput(uncompressed);
        deflater.finish();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        while (!deflater.finished()) {
            int count = deflater.deflate(buf);
            baos.write(buf, 0, count);
        }
        deflater.end();
        byte[] compressedData = baos.toByteArray();

        ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();
        pdfStream.write("%PDF-1.4\n1 0 obj\n<< /Filter /FlateDecode /Length 100 >>\nstream\n".getBytes(StandardCharsets.ISO_8859_1));
        pdfStream.write(compressedData);
        pdfStream.write("\nendstream\nendobj\n%%EOF".getBytes(StandardCharsets.ISO_8859_1));

        MockMultipartFile file = new MockMultipartFile("compressed_resume.pdf", "compressed_resume.pdf", "application/pdf", pdfStream.toByteArray());
        String extracted = DocumentTextExtractor.extractText(file);
        assertTrue(extracted.contains("Priyanshu Shekhar"));
        assertTrue(extracted.contains("Lead Backend Engineer"));
    }
}
