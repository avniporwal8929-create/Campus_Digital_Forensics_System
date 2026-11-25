package dao;

import config.DBConnection;
import models.AccessLog;
import models.EventLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class LogDAO {
    private static final List<EventLog> mockEventLogs = new CopyOnWriteArrayList<>();
    private static final List<AccessLog> mockAccessLogs = new CopyOnWriteArrayList<>();

    static {
        initMockData();
    }

    private static void initMockData() {
        // Event Logs (15 events)
        mockEventLogs.add(new EventLog(1, 10, "Vikram Aditya", 1, "Vikram ThinkPad X1", 3, "Exam Cell", "USER_LOGIN", "10.20.45.112", "SSH session initiated to Exam Cell Local Gateway 10.20.10.15:22", Timestamp.valueOf("2026-10-12 02:14:20")));
        mockEventLogs.add(new EventLog(2, 10, "Vikram Aditya", 1, "Vikram ThinkPad X1", 3, "Exam Cell", "FILE_ACCESSED", "10.20.45.112", "File downloaded: /srv/shares/exams/2026_midsem/CS401_Final.pdf", Timestamp.valueOf("2026-10-12 02:22:15")));
        mockEventLogs.add(new EventLog(3, 10, "Vikram Aditya", 1, "Vikram ThinkPad X1", 3, "Exam Cell", "USER_LOGOUT", "10.20.45.112", "SSH session terminated cleanly after transferring 1.4 MB", Timestamp.valueOf("2026-10-12 02:24:05")));
        mockEventLogs.add(new EventLog(4, 12, "David Kim", 4, "David Rog Strix Gaming", 5, "Newton Hostel", "PORT_SCAN_DETECTED", "10.20.45.198", "Nmap SYN stealth scan executed against 10.20.10.0/24 subnet (ports 21, 22, 80, 443, 3306, 8080)", Timestamp.valueOf("2026-10-18 02:45:00")));
        mockEventLogs.add(new EventLog(5, 12, "David Kim", 4, "David Rog Strix Gaming", 5, "Newton Hostel", "AUTH_FAILURE", "10.20.45.198", "Failed SSH authentication for root user on grading server 10.10.1.20 (attempt 148)", Timestamp.valueOf("2026-10-18 03:02:11")));
        mockEventLogs.add(new EventLog(6, 12, "David Kim", 4, "David Rog Strix Gaming", 5, "Newton Hostel", "AUTH_FAILURE", "10.20.45.198", "Failed SSH authentication for admin user on grading server 10.10.1.20 (attempt 320)", Timestamp.valueOf("2026-10-18 03:08:44")));
        mockEventLogs.add(new EventLog(7, 9, "Mohan Lal", 3, "IT Core Sysadmin Desktop", 2, "Data Center", "DEVICE_CONNECTED", "10.10.1.20", "Cisco Catalyst 9300 switch management console attached via Serial RJ45", Timestamp.valueOf("2026-10-15 14:20:00")));
        mockEventLogs.add(new EventLog(8, null, "Unregistered / Unknown", 4, "David Rog Strix Gaming", 2, "Data Center", "PORT_SCAN_DETECTED", "10.20.45.198", "Gratuitous ARP replies sent claiming default gateway MAC address (MAC Spoof)", Timestamp.valueOf("2026-10-15 14:25:30")));
        mockEventLogs.add(new EventLog(9, 6, "Prof. Clara Oswald", 2, "Prof Clara MacBook Pro", 1, "AI & Forensics Lab", "DEVICE_CONNECTED", "10.20.10.45", "External SanDisk Extreme SSD 1TB plugged into research workstation port 2", Timestamp.valueOf("2026-10-02 16:30:10")));
        mockEventLogs.add(new EventLog(10, 6, "Prof. Clara Oswald", 2, "Prof Clara MacBook Pro", 1, "AI & Forensics Lab", "USER_LOGIN", "10.20.10.45", "Kerberos single-sign-on authenticated for Prof. Clara Oswald in AI Lab", Timestamp.valueOf("2026-10-02 16:31:00")));
        mockEventLogs.add(new EventLog(11, 10, "Vikram Aditya", 9, "OnePlus 11 5G Phone", 4, "Ramanujan Library", "USER_LOGIN", "10.30.12.80", "Radius 802.1X wireless login on SSID Campus-Secure via OnePlus 11", Timestamp.valueOf("2026-10-12 08:00:15")));
        mockEventLogs.add(new EventLog(12, 11, "Ananya Deshmukh", 6, "Ananya Galaxy Book", 4, "Ramanujan Library", "USER_LOGIN", "10.20.45.140", "Portal session opened from Digital Reading Hall workstation 14", Timestamp.valueOf("2026-10-24 19:45:10")));
        mockEventLogs.add(new EventLog(13, null, "Internal Sensor", null, "IDS Appliance", 2, "Data Center", "MALWARE_TRIGGER", "10.10.1.99", "Snort IDS rule alert: ET TROJAN LockBit Ransomware outbound handshake detected", Timestamp.valueOf("2026-09-28 10:45:22")));
        mockEventLogs.add(new EventLog(14, 3, "Marcus Jackson", 7, "Forensics Falcon", 1, "AI & Forensics Lab", "DEVICE_CONNECTED", "10.10.5.10", "Tableau T8u USB 3.0 Forensic Bridge connected to analysis machine", Timestamp.valueOf("2026-10-13 10:00:00")));
        mockEventLogs.add(new EventLog(15, 4, "Sarah Jenkins", 8, "Forensics Portable Lab", 1, "AI & Forensics Lab", "PRIVILEGE_ESCALATION", "10.10.5.15", "Local sudo session opened for investigator Jenkins to mount disk image read-only", Timestamp.valueOf("2026-10-13 11:30:00")));

        // Access Logs (RFID Swipes)
        mockAccessLogs.add(new AccessLog(1, 10, "Vikram Aditya", 3, "Exam Cell", "CRD-STU-99014", "DENIED_OFF_HOURS", Timestamp.valueOf("2026-10-12 01:55:10")));
        mockAccessLogs.add(new AccessLog(2, 10, "Vikram Aditya", 3, "Exam Cell", "CRD-FAC-77419", "GRANTED", Timestamp.valueOf("2026-10-12 02:05:00")));
        mockAccessLogs.add(new AccessLog(3, 6, "Prof. Clara Oswald", 1, "AI & Forensics Lab", "CRD-FAC-77419", "GRANTED", Timestamp.valueOf("2026-10-02 16:25:00")));
        mockAccessLogs.add(new AccessLog(4, 12, "David Kim", 5, "Newton Hostel", "CRD-STU-66381", "GRANTED", Timestamp.valueOf("2026-10-18 01:30:00")));
        mockAccessLogs.add(new AccessLog(5, 9, "Mohan Lal", 2, "Data Center", "CRD-STF-10948", "GRANTED", Timestamp.valueOf("2026-10-15 14:05:00")));
        mockAccessLogs.add(new AccessLog(6, 10, "Vikram Aditya", 4, "Ramanujan Library", "CRD-STU-99014", "GRANTED", Timestamp.valueOf("2026-10-12 07:50:00")));
        mockAccessLogs.add(new AccessLog(7, 3, "Marcus Jackson", 1, "AI & Forensics Lab", "CRD-INV-88200", "GRANTED", Timestamp.valueOf("2026-10-13 09:45:00")));
        mockAccessLogs.add(new AccessLog(8, 4, "Sarah Jenkins", 1, "AI & Forensics Lab", "CRD-INV-55912", "GRANTED", Timestamp.valueOf("2026-10-13 09:50:00")));
        mockAccessLogs.add(new AccessLog(9, 12, "David Kim", 2, "Data Center", "CRD-STU-66381", "DENIED_PERMISSION", Timestamp.valueOf("2026-10-15 13:58:00")));
        mockAccessLogs.add(new AccessLog(10, 8, "Dr. Kavita Rao", 3, "Exam Cell", "CRD-ADM-00192", "GRANTED", Timestamp.valueOf("2026-10-12 08:15:00")));
    }

    public List<EventLog> getAllEventLogs() {
        String sql = "SELECT el.*, u.full_name AS user_name, d.device_name, CONCAT(l.building_name, ' - ', l.room_number) AS location_name " +
                     "FROM event_logs el " +
                     "LEFT JOIN campus_users u ON el.user_id = u.user_id " +
                     "LEFT JOIN devices d ON el.device_id = d.device_id " +
                     "LEFT JOIN locations l ON el.location_id = l.location_id " +
                     "ORDER BY el.event_timestamp DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            List<EventLog> list = new ArrayList<>();
            while (rs.next()) list.add(extractEventLog(rs));
            return list;
        } catch (SQLException e) {
            return new ArrayList<>(mockEventLogs);
        }
    }

    public boolean addEventLog(EventLog log) {
        String sql = "INSERT INTO event_logs (user_id, device_id, location_id, event_type, ip_address, event_details) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (log.getUserId() != null) stmt.setInt(1, log.getUserId()); else stmt.setNull(1, Types.INTEGER);
            if (log.getDeviceId() != null) stmt.setInt(2, log.getDeviceId()); else stmt.setNull(2, Types.INTEGER);
            if (log.getLocationId() != null) stmt.setInt(3, log.getLocationId()); else stmt.setNull(3, Types.INTEGER);
            stmt.setString(4, log.getEventType());
            stmt.setString(5, log.getIpAddress());
            stmt.setString(6, log.getEventDetails());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) log.setLogId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            log.setLogId(mockEventLogs.size() + 1);
            log.setEventTimestamp(new Timestamp(System.currentTimeMillis()));
            mockEventLogs.add(0, log);
            return true;
        }
        return false;
    }

    public List<AccessLog> getAllAccessLogs() {
        String sql = "SELECT al.*, u.full_name AS user_name, CONCAT(l.building_name, ' - ', l.room_number) AS location_name " +
                     "FROM access_logs al " +
                     "INNER JOIN campus_users u ON al.user_id = u.user_id " +
                     "INNER JOIN locations l ON al.location_id = l.location_id " +
                     "ORDER BY al.access_timestamp DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            List<AccessLog> list = new ArrayList<>();
            while (rs.next()) list.add(extractAccessLog(rs));
            return list;
        } catch (SQLException e) {
            return new ArrayList<>(mockAccessLogs);
        }
    }

    public boolean addAccessLog(AccessLog log) {
        String sql = "INSERT INTO access_logs (user_id, location_id, card_number, access_result) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, log.getUserId());
            stmt.setInt(2, log.getLocationId());
            stmt.setString(3, log.getCardNumber());
            stmt.setString(4, log.getAccessResult());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) log.setAccessId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            log.setAccessId(mockAccessLogs.size() + 1);
            log.setAccessTimestamp(new Timestamp(System.currentTimeMillis()));
            mockAccessLogs.add(0, log);
            return true;
        }
        return false;
    }

    private EventLog extractEventLog(ResultSet rs) throws SQLException {
        return new EventLog(
            rs.getInt("log_id"),
            (Integer) rs.getObject("user_id"),
            rs.getString("user_name"),
            (Integer) rs.getObject("device_id"),
            rs.getString("device_name"),
            (Integer) rs.getObject("location_id"),
            rs.getString("location_name"),
            rs.getString("event_type"),
            rs.getString("ip_address"),
            rs.getString("event_details"),
            rs.getTimestamp("event_timestamp")
        );
    }

    private AccessLog extractAccessLog(ResultSet rs) throws SQLException {
        return new AccessLog(
            rs.getInt("access_id"),
            rs.getInt("user_id"),
            rs.getString("user_name"),
            rs.getInt("location_id"),
            rs.getString("location_name"),
            rs.getString("card_number"),
            rs.getString("access_result"),
            rs.getTimestamp("access_timestamp")
        );
    }
}
