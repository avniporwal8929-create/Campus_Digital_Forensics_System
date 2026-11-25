package dao;

import config.DBConnection;
import models.CaseIncident;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class IncidentDAO {
    private static final List<CaseIncident> mockCases = new CopyOnWriteArrayList<>();

    static {
        initMockData();
    }

    private static void initMockData() {
        mockCases.add(new CaseIncident(1, "CDF-2026-001", "Mid-Term Exam Paper Leak via Exam Cell", "EXAM_TAMPERING", "CS401 Computer Networks question paper leaked online 4 hours before scheduled examination. Digital unauthorized intrusion detected in Exam Cell NAS.", "CRITICAL", "UNDER_INVESTIGATION", 3, "Main Academic Block - Room 105", 8, "Dr. Kavita Rao", Timestamp.valueOf("2026-10-12 08:30:00"), null));
        mockCases.add(new CaseIncident(2, "CDF-2026-002", "Data Center Core Switch ARP Poisoning Attack", "NETWORK_INTRUSION", "Widespread packet capture and man-in-the-middle ARP spoofing detected redirecting administrative subnet traffic in Aryabhata Complex.", "HIGH", "UNDER_INVESTIGATION", 2, "Aryabhata Computing Complex - Server Room DC-01", 9, "Mohan Lal", Timestamp.valueOf("2026-10-15 14:15:00"), null));
        mockCases.add(new CaseIncident(3, "CDF-2026-003", "Ransomware Infection in Mechanical Dept Lab", "MALWARE_OUTBREAK", "LockBit variant detected encrypting CAD files on 5 lab desktop machines. Prompt isolation prevented lateral propagation across campus VLANs.", "HIGH", "RESOLVED", 1, "Turing Technology Center - Lab-301", 6, "Prof. Clara Oswald", Timestamp.valueOf("2026-09-28 11:00:00"), Timestamp.valueOf("2026-10-01 10:00:00")));
        mockCases.add(new CaseIncident(4, "CDF-2026-004", "Mass Phishing Campaign Impersonating Dean Office", "PHISHING_ATTACK", "Spoofed emails sent to 450 students soliciting portal passwords under guise of scholarship verification. 18 credentials compromised.", "MEDIUM", "CLOSED", 4, "Ramanujan Library - Digital Reading Hall 2", 1, "Dr. Rajesh Sharma", Timestamp.valueOf("2026-09-10 09:20:00"), Timestamp.valueOf("2026-09-12 17:30:00")));
        mockCases.add(new CaseIncident(5, "CDF-2026-005", "Hostel C Off-Hours Port Scanning and SSH Brute Force", "UNAUTHORIZED_ACCESS", "Over 10,000 automated SSH connection attempts originating from IP 10.20.45.198 targeting campus grading server between 02:00 and 04:00 AM.", "HIGH", "UNDER_INVESTIGATION", 5, "Newton Hall of Residence - Block-C 4th Floor", 2, "Elena Vance", Timestamp.valueOf("2026-10-18 03:15:00"), null));
        mockCases.add(new CaseIncident(6, "CDF-2026-006", "Unauthorized USB Insertion in Restricted Research Lab", "POLICY_VIOLATION", "Unapproved USB mass storage device connected to AI model training cluster containing proprietary research dataset.", "MEDIUM", "RESOLVED", 1, "Turing Technology Center - Lab-301", 6, "Prof. Clara Oswald", Timestamp.valueOf("2026-10-02 16:40:00"), Timestamp.valueOf("2026-10-05 16:00:00")));
        mockCases.add(new CaseIncident(7, "CDF-2026-007", "Credential Stuffing against Student ERP Portal", "CREDENTIAL_STUFFING", "Distributed login attempts using compromised third-party credential dump across campus portal API endpoints.", "MEDIUM", "OPEN", 2, "Aryabhata Computing Complex - Server Room DC-01", 9, "Mohan Lal", Timestamp.valueOf("2026-10-24 19:50:00"), null));
        mockCases.add(new CaseIncident(8, "CDF-2026-008", "Suspected Academic Research Data Exfiltration", "DATA_EXFILTRATION", "Abnormal outbound HTTPS payload transfer of 42GB to external Mega.nz endpoint during non-instructional weekend hours.", "CRITICAL", "OPEN", 1, "Turing Technology Center - Lab-301", 7, "Prof. Alan Morris", Timestamp.valueOf("2026-10-25 01:10:00"), null));
    }

    public List<CaseIncident> getAllCases() {
        String sql = "SELECT c.*, CONCAT(l.building_name, ' - ', l.room_number) AS location_name, u.full_name AS reported_by_name " +
                     "FROM cases c " +
                     "LEFT JOIN locations l ON c.location_id = l.location_id " +
                     "LEFT JOIN campus_users u ON c.reported_by_user_id = u.user_id " +
                     "ORDER BY c.reported_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            List<CaseIncident> list = new ArrayList<>();
            while (rs.next()) list.add(extractCase(rs));
            return list;
        } catch (SQLException e) {
            System.err.println("IncidentDAO fallback to mock data: " + e.getMessage());
            return new ArrayList<>(mockCases);
        }
    }

    public CaseIncident getCaseById(int caseId) {
        for (CaseIncident c : getAllCases()) {
            if (c.getCaseId() == caseId) return c;
        }
        return null;
    }

    public boolean addCase(CaseIncident c) {
        String sql = "INSERT INTO cases (case_number, title, incident_type, description, severity, status, location_id, reported_by_user_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, c.getCaseNumber());
            stmt.setString(2, c.getTitle());
            stmt.setString(3, c.getIncidentType());
            stmt.setString(4, c.getDescription());
            stmt.setString(5, c.getSeverity());
            stmt.setString(6, c.getStatus());
            if (c.getLocationId() != null) stmt.setInt(7, c.getLocationId());
            else stmt.setNull(7, Types.INTEGER);
            if (c.getReportedByUserId() != null) stmt.setInt(8, c.getReportedByUserId());
            else stmt.setNull(8, Types.INTEGER);

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) c.setCaseId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            c.setCaseId(mockCases.size() + 1);
            c.setReportedAt(new Timestamp(System.currentTimeMillis()));
            mockCases.add(c);
            return true;
        }
        return false;
    }

    public boolean updateCase(CaseIncident c) {
        String sql = "UPDATE cases SET title = ?, incident_type = ?, description = ?, severity = ?, status = ?, location_id = ? " +
                     "WHERE case_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, c.getTitle());
            stmt.setString(2, c.getIncidentType());
            stmt.setString(3, c.getDescription());
            stmt.setString(4, c.getSeverity());
            stmt.setString(5, c.getStatus());
            if (c.getLocationId() != null) stmt.setInt(6, c.getLocationId());
            else stmt.setNull(6, Types.INTEGER);
            stmt.setInt(7, c.getCaseId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            for (int i = 0; i < mockCases.size(); i++) {
                if (mockCases.get(i).getCaseId() == c.getCaseId()) {
                    mockCases.set(i, c);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean updateCaseStatus(int caseId, String newStatus) {
        String sql = "UPDATE cases SET status = ?, resolved_at = ? WHERE case_id = ?";
        Timestamp resolvedAt = ("RESOLVED".equalsIgnoreCase(newStatus) || "CLOSED".equalsIgnoreCase(newStatus))
                ? new Timestamp(System.currentTimeMillis()) : null;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setTimestamp(2, resolvedAt);
            stmt.setInt(3, caseId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            for (CaseIncident c : mockCases) {
                if (c.getCaseId() == caseId) {
                    c.setStatus(newStatus);
                    c.setResolvedAt(resolvedAt);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean deleteCase(int caseId) {
        String sql = "DELETE FROM cases WHERE case_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, caseId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return mockCases.removeIf(c -> c.getCaseId() == caseId);
        }
    }

    public List<CaseIncident> searchCases(String query, String statusFilter, String severityFilter) {
        List<CaseIncident> all = getAllCases();
        List<CaseIncident> filtered = new ArrayList<>();
        String q = (query == null) ? "" : query.trim().toLowerCase();

        for (CaseIncident c : all) {
            boolean matchesQuery = q.isEmpty() ||
                    c.getCaseNumber().toLowerCase().contains(q) ||
                    c.getTitle().toLowerCase().contains(q) ||
                    c.getIncidentType().toLowerCase().contains(q) ||
                    c.getDescription().toLowerCase().contains(q);

            boolean matchesStatus = (statusFilter == null || "ALL".equalsIgnoreCase(statusFilter) || statusFilter.equalsIgnoreCase(c.getStatus()));
            boolean matchesSeverity = (severityFilter == null || "ALL".equalsIgnoreCase(severityFilter) || severityFilter.equalsIgnoreCase(c.getSeverity()));

            if (matchesQuery && matchesStatus && matchesSeverity) {
                filtered.add(c);
            }
        }
        return filtered;
    }

    private CaseIncident extractCase(ResultSet rs) throws SQLException {
        return new CaseIncident(
            rs.getInt("case_id"),
            rs.getString("case_number"),
            rs.getString("title"),
            rs.getString("incident_type"),
            rs.getString("description"),
            rs.getString("severity"),
            rs.getString("status"),
            (Integer) rs.getObject("location_id"),
            rs.getString("location_name"),
            (Integer) rs.getObject("reported_by_user_id"),
            rs.getString("reported_by_name"),
            rs.getTimestamp("reported_at"),
            rs.getTimestamp("resolved_at")
        );
    }
}
