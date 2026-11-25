package dao;

import config.DBConnection;
import models.CaseAssignment;
import models.InvestigationActivity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class InvestigatorDAO {
    private static final List<CaseAssignment> mockAssignments = new CopyOnWriteArrayList<>();
    private static final List<InvestigationActivity> mockActivities = new CopyOnWriteArrayList<>();

    static {
        initMockData();
    }

    private static void initMockData() {
        mockAssignments.add(new CaseAssignment(1, 1, "CDF-2026-001", "Mid-Term Exam Paper Leak via Exam Cell", 3, "Marcus Jackson", "m.jackson@campus.edu", 1, "Dr. Rajesh Sharma", Timestamp.valueOf("2026-10-12 09:00:00"), "IN_PROGRESS", true, "Lead forensic analyst on Exam Cell penetration case"));
        mockAssignments.add(new CaseAssignment(2, 1, "CDF-2026-001", "Mid-Term Exam Paper Leak via Exam Cell", 4, "Sarah Jenkins", "s.jenkins@campus.edu", 1, "Dr. Rajesh Sharma", Timestamp.valueOf("2026-10-12 09:15:00"), "IN_PROGRESS", false, "Co-investigator handling physical evidence acquisition"));
        mockAssignments.add(new CaseAssignment(3, 2, "CDF-2026-002", "Data Center Core Switch ARP Poisoning Attack", 5, "Arjun Kapoor", "a.kapoor@campus.edu", 2, "Elena Vance", Timestamp.valueOf("2026-10-15 15:00:00"), "IN_PROGRESS", true, "Packet capture and network traffic analysis lead"));
        mockAssignments.add(new CaseAssignment(4, 3, "CDF-2026-003", "Ransomware Infection in Mechanical Dept Lab", 3, "Marcus Jackson", "m.jackson@campus.edu", 1, "Dr. Rajesh Sharma", Timestamp.valueOf("2026-09-28 11:30:00"), "COMPLETED", true, "Ransomware isolated, decrypter tested and lab restored"));
        mockAssignments.add(new CaseAssignment(5, 4, "CDF-2026-004", "Mass Phishing Campaign Impersonating Dean Office", 4, "Sarah Jenkins", "s.jenkins@campus.edu", 1, "Dr. Rajesh Sharma", Timestamp.valueOf("2026-09-10 10:00:00"), "COMPLETED", true, "Domain takedown coordinated and compromised creds reset"));
        mockAssignments.add(new CaseAssignment(6, 5, "CDF-2026-005", "Hostel C Off-Hours Port Scanning and SSH Brute Force", 5, "Arjun Kapoor", "a.kapoor@campus.edu", 2, "Elena Vance", Timestamp.valueOf("2026-10-18 08:00:00"), "IN_PROGRESS", true, "Investigating source of hostel SSH attacks"));
        mockAssignments.add(new CaseAssignment(7, 8, "CDF-2026-008", "Suspected Academic Research Data Exfiltration", 3, "Marcus Jackson", "m.jackson@campus.edu", 1, "Dr. Rajesh Sharma", Timestamp.valueOf("2026-10-25 09:00:00"), "ASSIGNED", true, "Assigned to triage large data transfer breach"));

        mockActivities.add(new InvestigationActivity(1, 1, "CDF-2026-001", 3, "Marcus Jackson", "EVIDENCE_SEIZURE", "Seizure of Laptop and Volatile Memory", "Executed immediate seizure of student laptop in hostel following Dean authorization. Acquired RAM before reboot.", Timestamp.valueOf("2026-10-13 09:30:00")));
        mockActivities.add(new InvestigationActivity(2, 1, "CDF-2026-001", 3, "Marcus Jackson", "HEX_ANALYSIS", "File Signature and Deleted MFT Record Scan", "Recovered deleted PDF matching MD5 hash of official CS401 exam paper from unallocated clusters.", Timestamp.valueOf("2026-10-14 11:20:00")));
        mockActivities.add(new InvestigationActivity(3, 1, "CDF-2026-001", 4, "Sarah Jenkins", "INTERVIEW", "Subject Interview with Vikram Aditya", "Conducted formal interview with student regarding card swipe anomaly at Exam Cell door at 02:05 AM.", Timestamp.valueOf("2026-10-14 15:30:00")));
        mockActivities.add(new InvestigationActivity(4, 2, "CDF-2026-002", 5, "Arjun Kapoor", "LOG_CORRELATION", "Correlated MAC Table flapping with Rogue IP", "Identified MAC 7C:10:C9:81:4A:22 advertising gateway IP 10.20.45.1 across 4 switch trunk ports.", Timestamp.valueOf("2026-10-16 09:45:00")));
        mockActivities.add(new InvestigationActivity(5, 3, "CDF-2026-003", 3, "Marcus Jackson", "TIMELINE_RECONSTRUCTION", "Patient Zero Infection Vector Analysis", "Identified phishing link opened in faculty web browser initiating Powershell payload download.", Timestamp.valueOf("2026-09-29 14:00:00")));
        mockActivities.add(new InvestigationActivity(6, 4, "CDF-2026-004", 4, "Sarah Jenkins", "REPORT_DRAFTING", "Comprehensive Post-Incident Phishing Dossier", "Compiled IOC report and submitted recommendations for campus-wide FIDO2 MFA enforcement.", Timestamp.valueOf("2026-09-12 16:00:00")));
        mockActivities.add(new InvestigationActivity(7, 5, "CDF-2026-005", 5, "Arjun Kapoor", "LOG_CORRELATION", "Correlation of Auth Log Failures with Port Swipes", "Confirmed over 9,000 attempts against root account originated from Student David Kim room port.", Timestamp.valueOf("2026-10-19 11:15:00")));
        mockActivities.add(new InvestigationActivity(8, 1, "CDF-2026-001", 3, "Marcus Jackson", "PEER_REVIEW", "Peer Review of Autopsy Artifact Findings", "Reviewed SQLite forensic extraction results with Administrator Dr. Sharma for evidentiary integrity.", Timestamp.valueOf("2026-10-16 17:00:00")));
    }

    public List<CaseAssignment> getAllAssignments() {
        String sql = "SELECT ca.*, c.case_number, c.title AS case_title, " +
                     "u_inv.full_name AS investigator_name, u_inv.email AS investigator_email, " +
                     "u_by.full_name AS assigned_by_name " +
                     "FROM case_assignments ca " +
                     "INNER JOIN cases c ON ca.case_id = c.case_id " +
                     "INNER JOIN campus_users u_inv ON ca.investigator_id = u_inv.user_id " +
                     "LEFT JOIN campus_users u_by ON ca.assigned_by = u_by.user_id " +
                     "ORDER BY ca.assigned_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            List<CaseAssignment> list = new ArrayList<>();
            while (rs.next()) list.add(extractAssignment(rs));
            return list;
        } catch (SQLException e) {
            return new ArrayList<>(mockAssignments);
        }
    }

    public boolean assignInvestigator(CaseAssignment ca) {
        String sql = "INSERT INTO case_assignments (case_id, investigator_id, assigned_by, assignment_status, lead_investigator, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, ca.getCaseId());
            stmt.setInt(2, ca.getInvestigatorId());
            if (ca.getAssignedBy() != null) stmt.setInt(3, ca.getAssignedBy()); else stmt.setNull(3, Types.INTEGER);
            stmt.setString(4, ca.getAssignmentStatus());
            stmt.setBoolean(5, ca.isLeadInvestigator());
            stmt.setString(6, ca.getNotes());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) ca.setAssignmentId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            ca.setAssignmentId(mockAssignments.size() + 1);
            ca.setAssignedDate(new Timestamp(System.currentTimeMillis()));
            mockAssignments.add(0, ca);
            return true;
        }
        return false;
    }

    public boolean updateAssignmentStatus(int assignmentId, String newStatus) {
        String sql = "UPDATE case_assignments SET assignment_status = ? WHERE assignment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, assignmentId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            for (CaseAssignment ca : mockAssignments) {
                if (ca.getAssignmentId() == assignmentId) {
                    ca.setAssignmentStatus(newStatus);
                    return true;
                }
            }
        }
        return false;
    }

    public List<InvestigationActivity> getAllActivities() {
        String sql = "SELECT ia.*, c.case_number, u.full_name AS investigator_name " +
                     "FROM investigation_activities ia " +
                     "INNER JOIN cases c ON ia.case_id = c.case_id " +
                     "INNER JOIN campus_users u ON ia.investigator_id = u.user_id " +
                     "ORDER BY ia.activity_timestamp DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            List<InvestigationActivity> list = new ArrayList<>();
            while (rs.next()) list.add(extractActivity(rs));
            return list;
        } catch (SQLException e) {
            return new ArrayList<>(mockActivities);
        }
    }

    public boolean addActivity(InvestigationActivity act) {
        String sql = "INSERT INTO investigation_activities (case_id, investigator_id, activity_type, activity_summary, detailed_notes) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, act.getCaseId());
            stmt.setInt(2, act.getInvestigatorId());
            stmt.setString(3, act.getActivityType());
            stmt.setString(4, act.getActivitySummary());
            stmt.setString(5, act.getDetailedNotes());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) act.setActivityId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            act.setActivityId(mockActivities.size() + 1);
            act.setActivityTimestamp(new Timestamp(System.currentTimeMillis()));
            mockActivities.add(0, act);
            return true;
        }
        return false;
    }

    private CaseAssignment extractAssignment(ResultSet rs) throws SQLException {
        return new CaseAssignment(
            rs.getInt("assignment_id"),
            rs.getInt("case_id"),
            rs.getString("case_number"),
            rs.getString("case_title"),
            rs.getInt("investigator_id"),
            rs.getString("investigator_name"),
            rs.getString("investigator_email"),
            (Integer) rs.getObject("assigned_by"),
            rs.getString("assigned_by_name"),
            rs.getTimestamp("assigned_date"),
            rs.getString("assignment_status"),
            rs.getBoolean("lead_investigator"),
            rs.getString("notes")
        );
    }

    private InvestigationActivity extractActivity(ResultSet rs) throws SQLException {
        return new InvestigationActivity(
            rs.getInt("activity_id"),
            rs.getInt("case_id"),
            rs.getString("case_number"),
            rs.getInt("investigator_id"),
            rs.getString("investigator_name"),
            rs.getString("activity_type"),
            rs.getString("activity_summary"),
            rs.getString("detailed_notes"),
            rs.getTimestamp("activity_timestamp")
        );
    }
}
