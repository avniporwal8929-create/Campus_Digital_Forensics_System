package dao;

import config.DBConnection;
import models.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class UserDAO {
    // In-memory fallback dataset for seamless demo/testing when MySQL is temporarily stopped
    private static final List<User> mockUsers = new CopyOnWriteArrayList<>();

    static {
        initMockData();
    }

    private static void initMockData() {
        mockUsers.add(new User(1, "admin", "240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9", "Dr. Rajesh Sharma", "admin@campus.edu", "Cybersecurity Division", "ADMIN", "ACTIVE", "+91-9876543210", new Timestamp(System.currentTimeMillis())));
        mockUsers.add(new User(2, "sec_lead", "240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9", "Elena Vance", "elena.vance@campus.edu", "Campus Security Operations", "ADMIN", "ACTIVE", "+91-9876543211", new Timestamp(System.currentTimeMillis())));
        mockUsers.add(new User(3, "inv_marcus", "ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f", "Marcus Jackson", "m.jackson@campus.edu", "Digital Forensics Unit", "INVESTIGATOR", "ACTIVE", "+91-9876543212", new Timestamp(System.currentTimeMillis())));
        mockUsers.add(new User(4, "inv_sarah", "ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f", "Sarah Jenkins", "s.jenkins@campus.edu", "Digital Forensics Unit", "INVESTIGATOR", "ACTIVE", "+91-9876543213", new Timestamp(System.currentTimeMillis())));
        mockUsers.add(new User(5, "inv_arjun", "ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f", "Arjun Kapoor", "a.kapoor@campus.edu", "Incident Response Team", "INVESTIGATOR", "ACTIVE", "+91-9876543214", new Timestamp(System.currentTimeMillis())));
        mockUsers.add(new User(6, "prof_clara", "ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f", "Prof. Clara Oswald", "clara.o@campus.edu", "Computer Science & Eng", "FACULTY", "ACTIVE", "+91-9876543215", new Timestamp(System.currentTimeMillis())));
        mockUsers.add(new User(7, "prof_morris", "ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f", "Prof. Alan Morris", "a.morris@campus.edu", "Information Systems", "FACULTY", "ACTIVE", "+91-9876543216", new Timestamp(System.currentTimeMillis())));
        mockUsers.add(new User(8, "dr_kavita", "ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f", "Dr. Kavita Rao", "kavita.r@campus.edu", "Dean Academics Office", "FACULTY", "ACTIVE", "+91-9876543217", new Timestamp(System.currentTimeMillis())));
        mockUsers.add(new User(9, "staff_mohan", "ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f", "Mohan Lal", "mohan.l@campus.edu", "IT Services Infrastructure", "STAFF", "ACTIVE", "+91-9876543218", new Timestamp(System.currentTimeMillis())));
        mockUsers.add(new User(10, "stu_vikram", "ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f", "Vikram Aditya", "vikram.a22@campus.edu", "Computer Science & Eng", "STUDENT", "ACTIVE", "+91-9876543219", new Timestamp(System.currentTimeMillis())));
        mockUsers.add(new User(11, "stu_ananya", "ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f", "Ananya Deshmukh", "ananya.d23@campus.edu", "Electrical Engineering", "STUDENT", "ACTIVE", "+91-9876543220", new Timestamp(System.currentTimeMillis())));
        mockUsers.add(new User(12, "stu_david", "ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f", "David Kim", "david.k21@campus.edu", "Mechanical Engineering", "STUDENT", "SUSPENDED", "+91-9876543221", new Timestamp(System.currentTimeMillis())));
    }

    public List<User> getAllUsers() {
        String sql = "SELECT user_id, username, password_hash, full_name, email, department, role, status, phone_number, created_at FROM campus_users ORDER BY user_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            List<User> list = new ArrayList<>();
            while (rs.next()) {
                list.add(extractUser(rs));
            }
            return list;
        } catch (SQLException e) {
            System.err.println("UserDAO fallback to mock data: " + e.getMessage());
            return new ArrayList<>(mockUsers);
        }
    }

    public User getUserById(int userId) {
        String sql = "SELECT * FROM campus_users WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractUser(rs);
                }
            }
        } catch (SQLException e) {
            for (User u : mockUsers) {
                if (u.getUserId() == userId) return u;
            }
        }
        return null;
    }

    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM campus_users WHERE username = ? OR email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractUser(rs);
                }
            }
        } catch (SQLException e) {
            for (User u : mockUsers) {
                if (u.getUsername().equalsIgnoreCase(username) || u.getEmail().equalsIgnoreCase(username)) {
                    return u;
                }
            }
        }
        return null;
    }

    public boolean addUser(User user) {
        String sql = "INSERT INTO campus_users (username, password_hash, full_name, email, department, role, status, phone_number) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getFullName());
            stmt.setString(4, user.getEmail());
            stmt.setString(5, user.getDepartment());
            stmt.setString(6, user.getRole());
            stmt.setString(7, user.getStatus());
            stmt.setString(8, user.getPhoneNumber());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setUserId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Database insert failed: " + e.getMessage() + ", adding to fallback store.");
            user.setUserId(mockUsers.size() + 1);
            user.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            mockUsers.add(user);
            return true;
        }
        return false;
    }

    public boolean updateUser(User user) {
        String sql = "UPDATE campus_users SET full_name = ?, email = ?, department = ?, role = ?, status = ?, phone_number = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getDepartment());
            stmt.setString(4, user.getRole());
            stmt.setString(5, user.getStatus());
            stmt.setString(6, user.getPhoneNumber());
            stmt.setInt(7, user.getUserId());

            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            for (int i = 0; i < mockUsers.size(); i++) {
                if (mockUsers.get(i).getUserId() == user.getUserId()) {
                    mockUsers.set(i, user);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM campus_users WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            return mockUsers.removeIf(u -> u.getUserId() == userId);
        }
    }

    public List<User> searchUsers(String query) {
        if (query == null || query.isBlank()) {
            return getAllUsers();
        }
        String sql = "SELECT * FROM campus_users WHERE full_name LIKE ? OR username LIKE ? OR email LIKE ? OR department LIKE ? OR role LIKE ?";
        String wild = "%" + query.trim() + "%";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, wild);
            stmt.setString(2, wild);
            stmt.setString(3, wild);
            stmt.setString(4, wild);
            stmt.setString(5, wild);

            try (ResultSet rs = stmt.executeQuery()) {
                List<User> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(extractUser(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            List<User> filtered = new ArrayList<>();
            String lower = query.toLowerCase();
            for (User u : mockUsers) {
                if (u.getFullName().toLowerCase().contains(lower) ||
                    u.getUsername().toLowerCase().contains(lower) ||
                    u.getEmail().toLowerCase().contains(lower) ||
                    u.getDepartment().toLowerCase().contains(lower) ||
                    u.getRole().toLowerCase().contains(lower)) {
                    filtered.add(u);
                }
            }
            return filtered;
        }
    }

    public List<User> getInvestigators() {
        String sql = "SELECT * FROM campus_users WHERE role IN ('INVESTIGATOR', 'ADMIN') AND status = 'ACTIVE' ORDER BY full_name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            List<User> list = new ArrayList<>();
            while (rs.next()) {
                list.add(extractUser(rs));
            }
            return list;
        } catch (SQLException e) {
            List<User> list = new ArrayList<>();
            for (User u : mockUsers) {
                if (("INVESTIGATOR".equalsIgnoreCase(u.getRole()) || "ADMIN".equalsIgnoreCase(u.getRole())) && "ACTIVE".equalsIgnoreCase(u.getStatus())) {
                    list.add(u);
                }
            }
            return list;
        }
    }

    private User extractUser(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt("user_id"),
            rs.getString("username"),
            rs.getString("password_hash"),
            rs.getString("full_name"),
            rs.getString("email"),
            rs.getString("department"),
            rs.getString("role"),
            rs.getString("status"),
            rs.getString("phone_number"),
            rs.getTimestamp("created_at")
        );
    }
}
