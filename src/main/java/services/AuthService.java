package services;

import dao.UserDAO;
import models.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Authentication and Session Management Service
 * Handles password hashing (SHA-256), login validation, and RBAC authorization.
 */
public class AuthService {
    private static AuthService instance;
    private final UserDAO userDAO;
    private User currentUser;

    private AuthService() {
        this.userDAO = new UserDAO();
    }

    public static synchronized AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    /**
     * Compute SHA-256 hash in hex representation.
     */
    public static String hashPassword(String password) {
        if (password == null) return "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 Algorithm not found", e);
        }
    }

    /**
     * Authenticate user with username/email and plaintext password.
     */
    public boolean login(String usernameOrEmail, String password) {
        if (usernameOrEmail == null || usernameOrEmail.isBlank() || password == null) {
            return false;
        }

        User user = userDAO.getUserByUsername(usernameOrEmail.trim());
        if (user == null) {
            return false;
        }

        // Check account status
        if ("SUSPENDED".equalsIgnoreCase(user.getStatus()) || "INACTIVE".equalsIgnoreCase(user.getStatus())) {
            return false;
        }

        String inputHash = hashPassword(password);
        if (user.getPasswordHash().equalsIgnoreCase(inputHash)) {
            this.currentUser = user;
            return true;
        }

        // Support default plain match if legacy dev testing
        if (password.equals(user.getPasswordHash())) {
            this.currentUser = user;
            return true;
        }

        return false;
    }

    public void logout() {
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        return currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole());
    }

    public boolean isInvestigatorOrAdmin() {
        if (currentUser == null) return false;
        String role = currentUser.getRole();
        return "ADMIN".equalsIgnoreCase(role) || "INVESTIGATOR".equalsIgnoreCase(role);
    }
}
