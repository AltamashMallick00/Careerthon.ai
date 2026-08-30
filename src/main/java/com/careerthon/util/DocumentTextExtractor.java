package com.careerthon.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.Inflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Ultra-Lightweight, Zero-Regex, Zero-Metaspace Document Text Extractor.
 * Uses 100% linear iterative state-machine scanning for PDF and DOCX.
 * Completely immune to both OutOfMemoryError (Metaspace) and StackOverflowError (Regex recursion).
 */
public class DocumentTextExtractor {

    public static String extractText(MultipartFile file) {
        if (file == null || file.isEmpty()) return "";
        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";

        try {
            byte[] bytes = file.getBytes();
            if (bytes == null || bytes.length == 0) return "";

            if (originalFilename.endsWith(".pdf") || isPdfBytes(bytes)) {
                return extractPdfText(bytes);
            } else if (originalFilename.endsWith(".docx") || isZipBytes(bytes)) {
                return extractDocxText(bytes);
            } else {
                return extractPlainText(bytes);
            }
        } catch (Exception e) {
            return "Document content from " + originalFilename;
        }
    }

    private static boolean isPdfBytes(byte[] bytes) {
        if (bytes.length < 5) return false;
        return bytes[0] == '%' && bytes[1] == 'P' && bytes[2] == 'D' && bytes[3] == 'F';
    }

    private static boolean isZipBytes(byte[] bytes) {
        if (bytes.length < 4) return false;
        return bytes[0] == 0x50 && bytes[1] == 0x4B && bytes[2] == 0x03 && bytes[3] == 0x04;
    }

    /**
     * Extracts text from PDF bytes by finding and decompressing all streams and
     * iteratively parsing PDF text operators using a linear scanner (Zero Regex).
     */
    public static String extractPdfText(byte[] pdfBytes) {
        if (pdfBytes == null || pdfBytes.length == 0) return "";
        StringBuilder fullText = new StringBuilder();

        byte[] streamMarker = new byte[]{'s', 't', 'r', 'e', 'a', 'm'};
        byte[] endstreamMarker = new byte[]{'e', 'n', 'd', 's', 't', 'r', 'e', 'a', 'm'};

        int index = 0;
        int maxIndex = pdfBytes.length - 10;

        while (index < maxIndex) {
            int streamStart = findSequence(pdfBytes, streamMarker, index);
            if (streamStart == -1) break;

            // Skip "stream" and following CR/LF
            int dataStart = streamStart + 6;
            while (dataStart < pdfBytes.length && (pdfBytes[dataStart] == '\r' || pdfBytes[dataStart] == '\n')) {
                dataStart++;
            }

            int endStream = findSequence(pdfBytes, endstreamMarker, dataStart);
            if (endStream == -1) break;

            int dataEnd = endStream;
            while (dataEnd > dataStart && (pdfBytes[dataEnd - 1] == '\r' || pdfBytes[dataEnd - 1] == '\n' || pdfBytes[dataEnd - 1] == ' ')) {
                dataEnd--;
            }

            if (dataEnd > dataStart) {
                int length = dataEnd - dataStart;
                byte[] streamData = new byte[length];
                System.arraycopy(pdfBytes, dataStart, streamData, 0, length);

                // Try decompressing with Inflater (standard PDF FlateDecode)
                byte[] decompressed = decompressStream(streamData);
                if (decompressed != null && decompressed.length > 0) {
                    String decoded = new String(decompressed, StandardCharsets.ISO_8859_1);
                    parsePdfStreamIterative(decoded, fullText);
                } else {
                    // Try parsing as uncompressed stream
                    String rawDecoded = new String(streamData, StandardCharsets.ISO_8859_1);
                    parsePdfStreamIterative(rawDecoded, fullText);
                }
            }

            index = endStream + 9;
        }

        // Fallback: If compressed stream extraction returned insufficient text,
        // scan the raw PDF bytes iteratively for text literal strings
        if (fullText.length() < 100) {
            String rawPdf = new String(pdfBytes, StandardCharsets.ISO_8859_1);
            parsePdfStreamIterative(rawPdf, fullText);
        }

        String result = cleanExtractedText(fullText.toString());
        return result.length() > 50 ? result.substring(0, Math.min(result.length(), 40000)) : fullText.toString().trim();
    }

    private static int findSequence(byte[] source, byte[] target, int fromIndex) {
        if (source == null || target == null || fromIndex < 0) return -1;
        int limit = source.length - target.length;
        outer:
        for (int i = fromIndex; i <= limit; i++) {
            for (int j = 0; j < target.length; j++) {
                if (source[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    private static byte[] decompressStream(byte[] data) {
        try {
            Inflater inflater = new Inflater(false);
            inflater.setInput(data);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(Math.max(data.length * 3, 1024));
            byte[] buffer = new byte[4096];
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                if (count == 0 && inflater.needsInput()) break;
                outputStream.write(buffer, 0, count);
            }
            inflater.end();
            return outputStream.toByteArray();
        } catch (Exception e) {
            try {
                Inflater rawInflater = new Inflater(true);
                rawInflater.setInput(data);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream(Math.max(data.length * 3, 1024));
                byte[] buffer = new byte[4096];
                while (!rawInflater.finished()) {
                    int count = rawInflater.inflate(buffer);
                    if (count == 0 && rawInflater.needsInput()) break;
                    outputStream.write(buffer, 0, count);
                }
                rawInflater.end();
                return outputStream.toByteArray();
            } catch (Exception e2) {
                return null;
            }
        }
    }

    /**
     * Pure linear, non-recursive, state-machine token scanner for PDF stream text.
     * ZERO REGEX. Uses only 1 stack frame regardless of document size.
     */
    private static void parsePdfStreamIterative(String content, StringBuilder output) {
        if (content == null || content.isEmpty()) return;
        int len = content.length();
        int i = 0;

        while (i < len) {
            char c = content.charAt(i);

            // 1. PDF String Literal: (Text)
            if (c == '(') {
                i++;
                StringBuilder str = new StringBuilder();
                int parenDepth = 1;

                while (i < len && parenDepth > 0) {
                    char ch = content.charAt(i);
                    if (ch == '\\' && i + 1 < len) {
                        char next = content.charAt(i + 1);
                        if (next == 'n') str.append('\n');
                        else if (next == 'r') str.append('\r');
                        else if (next == 't') str.append('\t');
                        else if (next == 'b') str.append('\b');
                        else if (next == 'f') str.append('\f');
                        else if (next == '(' || next == ')' || next == '\\') str.append(next);
                        else if (next >= '0' && next <= '7') {
                            // Octal escape sequence \ddd
                            int octalVal = next - '0';
                            int octalLen = 1;
                            while (octalLen < 3 && i + 1 + octalLen < len && content.charAt(i + 1 + octalLen) >= '0' && content.charAt(i + 1 + octalLen) <= '7') {
                                octalVal = octalVal * 8 + (content.charAt(i + 1 + octalLen) - '0');
                                octalLen++;
                            }
                            str.append((char) octalVal);
                            i += octalLen; // skip octal digits
                        } else {
                            str.append(next);
                        }
                        i += 2;
                    } else if (ch == '(') {
                        parenDepth++;
                        str.append(ch);
                        i++;
                    } else if (ch == ')') {
                        parenDepth--;
                        if (parenDepth > 0) {
                            str.append(ch);
                        }
                        i++;
                    } else {
                        str.append(ch);
                        i++;
                    }
                }

                String s = str.toString().trim();
                if (s.length() > 0 && isPrintable(s)) {
                    output.append(s).append(" ");
                }
            }
            // 2. PDF Hex Encoded String: <48656C6C6F>
            else if (c == '<' && i + 1 < len && content.charAt(i + 1) != '<') {
                i++;
                int hexStart = i;
                while (i < len && content.charAt(i) != '>') {
                    i++;
                }
                if (i < len) {
                    String hex = content.substring(hexStart, i);
                    String decoded = decodeHexString(hex);
                    if (decoded.length() > 0 && isPrintable(decoded)) {
                        output.append(decoded).append(" ");
                    }
                    i++; // skip '>'
                }
            }
            else {
                i++;
            }
        }
    }

    private static String decodeHexString(String hex) {
        if (hex == null) return "";
        StringBuilder sb = new StringBuilder();
        int len = hex.length();
        int i = 0;

        while (i < len) {
            char c1 = hex.charAt(i);
            if (isHexDigit(c1)) {
                int j = i + 1;
                while (j < len && !isHexDigit(hex.charAt(j))) {
                    j++;
                }
                if (j < len) {
                    char c2 = hex.charAt(j);
                    try {
                        int code = Integer.parseInt("" + c1 + c2, 16);
                        if (code >= 32 && code <= 126) {
                            sb.append((char) code);
                        } else if (code == 10 || code == 13 || code == 9) {
                            sb.append(' ');
                        }
                    } catch (Exception ignored) {}
                    i = j + 1;
                } else {
                    break;
                }
            } else {
                i++;
            }
        }
        return sb.toString();
    }

    private static boolean isHexDigit(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    /**
     * Extracts text from DOCX using ZipInputStream to parse word/document.xml.
     * Pure linear tag parser (Zero Regex).
     */
    public static String extractDocxText(byte[] docxBytes) {
        StringBuilder sb = new StringBuilder();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(docxBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if ("word/document.xml".equalsIgnoreCase(entry.getName())) {
                    byte[] buffer = new byte[4096];
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        baos.write(buffer, 0, len);
                    }
                    String xml = baos.toString(StandardCharsets.UTF_8);
                    parseDocxXmlLinear(xml, sb);
                    break;
                }
            }
        } catch (Exception ignored) {}

        String result = cleanExtractedText(sb.toString());
        return result.length() > 50 ? result : extractPlainText(docxBytes);
    }

    private static void parseDocxXmlLinear(String xml, StringBuilder sb) {
        if (xml == null || xml.isEmpty()) return;
        int len = xml.length();
        int i = 0;

        while (i < len) {
            int tagStart = xml.indexOf("<w:t", i);
            if (tagStart == -1) break;

            int tagClose = xml.indexOf(">", tagStart);
            if (tagClose == -1) break;

            int endTag = xml.indexOf("</w:t>", tagClose);
            if (endTag == -1) break;

            String text = xml.substring(tagClose + 1, endTag);
            if (!text.isEmpty()) {
                sb.append(decodeXmlEntities(text)).append(" ");
            }
            i = endTag + 6;
        }
    }

    private static String decodeXmlEntities(String input) {
        if (input == null) return "";
        return input.replace("&amp;", "&")
                    .replace("&lt;", "<")
                    .replace("&gt;", ">")
                    .replace("&quot;", "\"")
                    .replace("&apos;", "'");
    }

    /**
     * Fallback for TXT and unknown documents.
     */
    public static String extractPlainText(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return "";
        StringBuilder sb = new StringBuilder(bytes.length);
        for (byte b : bytes) {
            int unsigned = b & 0xFF;
            if ((unsigned >= 32 && unsigned <= 126) || unsigned == 10 || unsigned == 13 || unsigned == 9) {
                sb.append((char) unsigned);
            } else if (unsigned > 127) {
                sb.append(' ');
            }
        }
        return cleanExtractedText(sb.toString());
    }

    private static boolean isPrintable(String s) {
        if (s == null || s.isEmpty()) return false;
        int printable = 0;
        int len = s.length();
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if ((c >= 32 && c <= 126) || c == '\n' || c == '\r' || c == '\t') {
                printable++;
            }
        }
        return (double) printable / len >= 0.6;
    }

    public static String cleanExtractedText(String raw) {
        if (raw == null || raw.isEmpty()) return "";
        StringBuilder sb = new StringBuilder(raw.length());
        boolean lastSpace = false;

        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c == '\r') {
                continue;
            }
            if (c == ' ' || c == '\t') {
                if (!lastSpace) {
                    sb.append(' ');
                    lastSpace = true;
                }
            } else {
                sb.append(c);
                lastSpace = false;
            }
        }
        return sb.toString().trim();
    }
}
