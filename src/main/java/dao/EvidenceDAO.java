package dao;

import config.DBConnection;
import models.Evidence;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class EvidenceDAO {
    private static final List<Evidence> mockEvidence = new CopyOnWriteArrayList<>();

    static {
        initMockData();
    }

    private static void initMockData() {
        mockEvidence.add(new Evidence(1, 1, "CDF-2026-001", "EVD-2026-001", "Seized Lenovo ThinkPad X1 NVMe SSD (512GB)", "HARD_DRIVE", "Raw bit-stream image acquired from Vikram ThinkPad using Tableau forensic bridge", "/evidence/cases/CDF-2026-001/nvme_01.raw", "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", 3, "Marcus Jackson", Timestamp.valueOf("2026-10-13 10:15:00"), "Forensic Safe Cabinet A - Shelf 2", "IN_ANALYSIS"));
        mockEvidence.add(new Evidence(2, 1, "CDF-2026-001", "EVD-2026-002", "Exam Cell NAS Access Logs Tarball", "LOG_ARCHIVE", "Apache, SSH and Samba access logs covering Oct 10 to Oct 13 2026", "/evidence/cases/CDF-2026-001/exam_cell_logs.tar.gz", "7d793037a0760186574b0282f2f435e70d7168765f974381508cc704192323ed", 4, "Sarah Jenkins", Timestamp.valueOf("2026-10-12 11:00:00"), "Forensics Server Repository /evd/002", "SECURED"));
        mockEvidence.add(new Evidence(3, 1, "CDF-2026-001", "EVD-2026-003", "RAM Memory Dump of ThinkPad Laptop", "RAM_DUMP", "Volatile memory captured using LiME kernel module prior to power off", "/evidence/cases/CDF-2026-001/ram_capture.dump", "a5835925230d303b60057f6488344e4735870ff0617037877e6114e931e61f28", 3, "Marcus Jackson", Timestamp.valueOf("2026-10-13 09:40:00"), "Forensic Vault High Priority Storage", "IN_ANALYSIS"));
        mockEvidence.add(new Evidence(4, 2, "CDF-2026-002", "EVD-2026-004", "Aryabhata Core Switch PCAP Dump (12.4GB)", "NETWORK_PCAP", "Promiscuous port mirror capture of malicious ARP responses and sniffing payload", "/evidence/cases/CDF-2026-002/core_mirror_oct15.pcap", "126a8e44b64f5f37d2823c72f7b83641a2286612ec131a4000da4545e6735292", 5, "Arjun Kapoor", Timestamp.valueOf("2026-10-15 16:30:00"), "Forensic Storage SAN Array Vol 3", "IN_ANALYSIS"));
        mockEvidence.add(new Evidence(5, 3, "CDF-2026-003", "EVD-2026-005", "Infected CAD Lab Machine Snapshot (VMDK)", "SYSTEM_SNAPSHOT", "Forensic disk copy containing LockBit ransomware sample and encrypted test vector", "/evidence/cases/CDF-2026-003/lab_pc4_snap.vmdk", "8f434346648f6b96df89dda901c5176b10e6d0ceec3e6a14bbb47d0ca73ab04c", 3, "Marcus Jackson", Timestamp.valueOf("2026-09-28 14:00:00"), "Offline Malware Isolation Safe", "ARCHIVED"));
        mockEvidence.add(new Evidence(6, 4, "CDF-2026-004", "EVD-2026-006", "Phishing Kit Source HTML and PHP Script Dump", "LOG_ARCHIVE", "Recovered malicious harvest scripts hosted on hijacked campus sub-domain", "/evidence/cases/CDF-2026-004/phish_kit_src.zip", "ca978112ca1bbdcafac231b39a23dc4da786eff8147c4e72b9807785afee48bb", 4, "Sarah Jenkins", Timestamp.valueOf("2026-09-10 12:45:00"), "Forensic Server Repository /evd/006", "ARCHIVED"));
        mockEvidence.add(new Evidence(7, 5, "CDF-2026-005", "EVD-2026-007", "Hostel C Switch Port 24 Traffic PCAP", "NETWORK_PCAP", "24-hour network capture exhibiting systematic brute-force dictionary traffic", "/evidence/cases/CDF-2026-005/hostel_c_p24.pcap", "9f83c24c9152f627324de376707ba13f74f42501a6ec6e69280e1184e60104be", 5, "Arjun Kapoor", Timestamp.valueOf("2026-10-18 10:15:00"), "Forensic Storage SAN Array Vol 2", "SECURED"));
        mockEvidence.add(new Evidence(8, 6, "CDF-2026-006", "EVD-2026-008", "SanDisk Extreme SSD 1TB Hardware Device", "USB_DRIVE", "Physical SSD seized from AI Lab connected to unauthorized computer", "/evidence/cases/CDF-2026-006/sandisk_1tb.dd", "3b10e777d3fc7e3420e79546e2a5d1641609b32d8b03373a43183556e3a77162", 4, "Sarah Jenkins", Timestamp.valueOf("2026-10-02 17:30:00"), "Evidence Locker Room B - Bin 14", "RETURNED"));
        mockEvidence.add(new Evidence(9, 8, "CDF-2026-008", "EVD-2026-009", "Border Firewall Mega.nz Flow NetFlow Logs", "LOG_ARCHIVE", "Cisco ASA NetFlow records verifying 42GB SSL exfiltration session", "/evidence/cases/CDF-2026-008/netflow_oct25.csv", "b843109a96e549d4be21ec7e2f518e1d283c784f183c5a6d38e2118f6735e294", 3, "Marcus Jackson", Timestamp.valueOf("2026-10-25 08:30:00"), "Forensics Server Repository /evd/009", "SECURED"));
    }

    public List<Evidence> getAllEvidence() {
        String sql = "SELECT e.*, c.case_number, u.full_name AS collector_name " +
                     "FROM evidence e " +
                     "INNER JOIN cases c ON e.case_id = c.case_id " +
                     "INNER JOIN campus_users u ON e.collected_by_id = u.user_id " +
                     "ORDER BY e.collection_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            List<Evidence> list = new ArrayList<>();
            while (rs.next()) list.add(extractEvidence(rs));
            return list;
        } catch (SQLException e) {
            return new ArrayList<>(mockEvidence);
        }
    }

    public boolean addEvidence(Evidence ev) {
        String sql = "INSERT INTO evidence (case_id, evidence_code, evidence_name, evidence_type, description, file_reference, sha256_hash, collected_by_id, storage_location, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, ev.getCaseId());
            stmt.setString(2, ev.getEvidenceCode());
            stmt.setString(3, ev.getEvidenceName());
            stmt.setString(4, ev.getEvidenceType());
            stmt.setString(5, ev.getDescription());
            stmt.setString(6, ev.getFileReference());
            stmt.setString(7, ev.getSha256Hash());
            stmt.setInt(8, ev.getCollectedById());
            stmt.setString(9, ev.getStorageLocation());
            stmt.setString(10, ev.getStatus());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) ev.setEvidenceId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            ev.setEvidenceId(mockEvidence.size() + 1);
            ev.setCollectionDate(new Timestamp(System.currentTimeMillis()));
            mockEvidence.add(0, ev);
            return true;
        }
        return false;
    }

    public boolean updateEvidence(Evidence ev) {
        String sql = "UPDATE evidence SET evidence_name = ?, evidence_type = ?, description = ?, file_reference = ?, sha256_hash = ?, storage_location = ?, status = ? " +
                     "WHERE evidence_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, ev.getEvidenceName());
            stmt.setString(2, ev.getEvidenceType());
            stmt.setString(3, ev.getDescription());
            stmt.setString(4, ev.getFileReference());
            stmt.setString(5, ev.getSha256Hash());
            stmt.setString(6, ev.getStorageLocation());
            stmt.setString(7, ev.getStatus());
            stmt.setInt(8, ev.getEvidenceId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            for (int i = 0; i < mockEvidence.size(); i++) {
                if (mockEvidence.get(i).getEvidenceId() == ev.getEvidenceId()) {
                    mockEvidence.set(i, ev);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean deleteEvidence(int evidenceId) {
        String sql = "DELETE FROM evidence WHERE evidence_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, evidenceId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return mockEvidence.removeIf(ev -> ev.getEvidenceId() == evidenceId);
        }
    }

    public List<Evidence> searchEvidence(String query, String typeFilter) {
        List<Evidence> all = getAllEvidence();
        List<Evidence> filtered = new ArrayList<>();
        String q = (query == null) ? "" : query.trim().toLowerCase();

        for (Evidence ev : all) {
            boolean matchesQuery = q.isEmpty() ||
                    ev.getEvidenceCode().toLowerCase().contains(q) ||
                    ev.getEvidenceName().toLowerCase().contains(q) ||
                    ev.getSha256Hash().toLowerCase().contains(q) ||
                    ev.getCaseNumber().toLowerCase().contains(q);

            boolean matchesType = (typeFilter == null || "ALL".equalsIgnoreCase(typeFilter) || typeFilter.equalsIgnoreCase(ev.getEvidenceType()));

            if (matchesQuery && matchesType) {
                filtered.add(ev);
            }
        }
        return filtered;
    }

    private Evidence extractEvidence(ResultSet rs) throws SQLException {
        return new Evidence(
            rs.getInt("evidence_id"),
            rs.getInt("case_id"),
            rs.getString("case_number"),
            rs.getString("evidence_code"),
            rs.getString("evidence_name"),
            rs.getString("evidence_type"),
            rs.getString("description"),
            rs.getString("file_reference"),
            rs.getString("sha256_hash"),
            rs.getInt("collected_by_id"),
            rs.getString("collector_name"),
            rs.getTimestamp("collection_date"),
            rs.getString("storage_location"),
            rs.getString("status")
        );
    }
}
