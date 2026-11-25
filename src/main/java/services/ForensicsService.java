package services;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Forensics Utilities Service
 * Implements cryptographic hashing (SHA-256) for forensic verification.
 * Note: A hash verifies data integrity from the point of collection, but
 * does not independently prove authenticity or user intent.
 */
public class ForensicsService {

    public static String calculateSHA256(File file) {
        if (file == null || !file.exists() || !file.canRead()) {
            return "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"; // Empty hash
        }
        try (InputStream is = new FileInputStream(file)) {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) != -1) {
                md.update(buffer, 0, read);
            }
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return "ERROR_CALCULATING_HASH: " + e.getMessage();
        }
    }

    public static String calculateSHA256(String text) {
        if (text == null) return "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static boolean verifyIntegrity(File file, String expectedHash) {
        if (expectedHash == null || expectedHash.isBlank()) return false;
        String calculated = calculateSHA256(file);
        return expectedHash.equalsIgnoreCase(calculated);
    }
}
