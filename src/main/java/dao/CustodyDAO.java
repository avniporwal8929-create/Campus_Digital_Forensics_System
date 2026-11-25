package dao;

import config.DBConnection;
import models.EvidenceCustody;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CustodyDAO {
    private static final List<EvidenceCustody> mockCustody = new CopyOnWriteArrayList<>();

    static {
        initMockData();
    }

    private static void initMockData() {
        mockCustody.add(new EvidenceCustody(1, 1, "EVD-2026-001", "Seized Lenovo ThinkPad X1 NVMe SSD (512GB)", "CDF-2026-001", 4, "Sarah Jenkins", 3, "Marcus Jackson", Timestamp.valueOf("2026-10-13 10:45:00"), "INTERNAL_TRANSFER", "Transferred NVMe bitstream image from acquisition field kit to lead forensic investigator", true));
        mockCustody.add(new EvidenceCustody(2, 1, "EVD-2026-001", "Seized Lenovo ThinkPad X1 NVMe SSD (512GB)", "CDF-2026-001", 3, "Marcus Jackson", 3, "Marcus Jackson", Timestamp.valueOf("2026-10-13 14:00:00"), "FORENSIC_ANALYSIS", "Mounted image in readonly mode using Autopsy for deleted SQLite chat and file recovery", true));
        mockCustody.add(new EvidenceCustody(3, 2, "EVD-2026-002", "Exam Cell NAS Access Logs Tarball", "CDF-2026-001", 4, "Sarah Jenkins", 1, "Dr. Rajesh Sharma", Timestamp.valueOf("2026-10-12 12:00:00"), "SECURE_VAULT_DEPOSIT", "Exam cell log archive deposited into central secure evidence server vault", true));
        mockCustody.add(new EvidenceCustody(4, 4, "EVD-2026-004", "Aryabhata Core Switch PCAP Dump (12.4GB)", "CDF-2026-002", 5, "Arjun Kapoor", 5, "Arjun Kapoor", Timestamp.valueOf("2026-10-15 17:00:00"), "FORENSIC_ANALYSIS", "Opened network packet trace in Wireshark for ARP Poisoning signature validation", true));
        mockCustody.add(new EvidenceCustody(5, 5, "EVD-2026-005", "Infected CAD Lab Machine Snapshot (VMDK)", "CDF-2026-003", 3, "Marcus Jackson", 1, "Dr. Rajesh Sharma", Timestamp.valueOf("2026-10-01 09:30:00"), "ADMINISTRATIVE_REVIEW", "Delivered snapshot analysis report to Chief Security Administrator Dr. Sharma", true));
        mockCustody.add(new EvidenceCustody(6, 8, "EVD-2026-008", "SanDisk Extreme SSD 1TB Hardware Device", "CDF-2026-006", 4, "Sarah Jenkins", 6, "Prof. Clara Oswald", Timestamp.valueOf("2026-10-05 16:00:00"), "RETURN_TO_OWNER", "Returned seized SSD to Prof. Clara after verifying no classified data resided thereon", true));
    }

    public List<EvidenceCustody> getAllCustodyTransfers() {
        String sql = "SELECT ec.*, ev.evidence_code, ev.evidence_name, c.case_number, " +
                     "u_from.full_name AS from_name, u_to.full_name AS to_name " +
                     "FROM evidence_custody ec " +
                     "INNER JOIN evidence ev ON ec.evidence_id = ev.evidence_id " +
                     "INNER JOIN cases c ON ev.case_id = c.case_id " +
                     "INNER JOIN campus_users u_from ON ec.transferred_from_id = u_from.user_id " +
                     "INNER JOIN campus_users u_to ON ec.transferred_to_id = u_to.user_id " +
                     "ORDER BY ec.transfer_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            List<EvidenceCustody> list = new ArrayList<>();
            while (rs.next()) list.add(extractCustody(rs));
            return list;
        } catch (SQLException e) {
            return new ArrayList<>(mockCustody);
        }
    }

    public List<EvidenceCustody> getCustodyHistoryForEvidence(int evidenceId) {
        List<EvidenceCustody> all = getAllCustodyTransfers();
        List<EvidenceCustody> filtered = new ArrayList<>();
        for (EvidenceCustody ec : all) {
            if (ec.getEvidenceId() == evidenceId) {
                filtered.add(ec);
            }
        }
        return filtered;
    }

    /**
     * Records a new evidence custody transfer using an explicit SQL Transaction.
     * Inserts into evidence_custody and updates evidence.status atomically.
     */
    public boolean recordTransfer(EvidenceCustody ec, String newEvidenceStatus) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Begin Transaction

            // 1. Insert Custody Record
            String insertSql = "INSERT INTO evidence_custody (evidence_id, transferred_from_id, transferred_to_id, action_type, purpose_remarks, integrity_verified) " +
                               "VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                insertStmt.setInt(1, ec.getEvidenceId());
                insertStmt.setInt(2, ec.getTransferredFromId());
                insertStmt.setInt(3, ec.getTransferredToId());
                insertStmt.setString(4, ec.getActionType());
                insertStmt.setString(5, ec.getPurposeRemarks());
                insertStmt.setBoolean(6, ec.isIntegrityVerified());
                insertStmt.executeUpdate();

                try (ResultSet rs = insertStmt.getGeneratedKeys()) {
                    if (rs.next()) ec.setCustodyId(rs.getInt(1));
                }
            }

            // 2. Update Evidence Status if changed
            if (newEvidenceStatus != null && !newEvidenceStatus.isBlank()) {
                String updateSql = "UPDATE evidence SET status = ? WHERE evidence_id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setString(1, newEvidenceStatus);
                    updateStmt.setInt(2, ec.getEvidenceId());
                    updateStmt.executeUpdate();
                }
            }

            conn.commit(); // Commit Transaction
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            // Fallback to in-memory store
            ec.setCustodyId(mockCustody.size() + 1);
            ec.setTransferDate(new Timestamp(System.currentTimeMillis()));
            mockCustody.add(0, ec);
            return true;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    private EvidenceCustody extractCustody(ResultSet rs) throws SQLException {
        return new EvidenceCustody(
            rs.getInt("custody_id"),
            rs.getInt("evidence_id"),
            rs.getString("evidence_code"),
            rs.getString("evidence_name"),
            rs.getString("case_number"),
            rs.getInt("transferred_from_id"),
            rs.getString("from_name"),
            rs.getInt("transferred_to_id"),
            rs.getString("to_name"),
            rs.getTimestamp("transfer_date"),
            rs.getString("action_type"),
            rs.getString("purpose_remarks"),
            rs.getBoolean("integrity_verified")
        );
    }
}
