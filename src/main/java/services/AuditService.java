package services;

import config.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AuditService {
    private static AuditService instance;

    private AuditService() {}

    public static synchronized AuditService getInstance() {
        if (instance == null) instance = new AuditService();
        return instance;
    }

    public void logAction(String tableName, int recordId, String action, String user, String details) {
        String sql = "INSERT INTO audit_logs (table_name, record_id, action_performed, performed_by_user, change_details) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            stmt.setInt(2, recordId);
            stmt.setString(3, action);
            stmt.setString(4, (user != null) ? user : "SYSTEM");
            stmt.setString(5, details);
            stmt.executeUpdate();
        } catch (SQLException e) {
            // Log to console if offline
            System.out.printf("[AUDIT] %s on %s #%d by %s: %s%n", action, tableName, recordId, user, details);
        }
    }
}
