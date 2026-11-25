package dao;

import config.DBConnection;
import models.Device;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DeviceDAO {
    private static final List<Device> mockDevices = new CopyOnWriteArrayList<>();

    static {
        initMockData();
    }

    private static void initMockData() {
        mockDevices.add(new Device(1, 10, "Vikram Aditya", "vikram.a22@campus.edu", "Vikram ThinkPad X1", "LAPTOP", "SN-LEN-984210", "00:1A:2B:3C:4D:5E", "Ubuntu Linux 22.04", "10.20.45.112", new Timestamp(System.currentTimeMillis()), "SEIZED", "Confiscated during exam tampering investigation"));
        mockDevices.add(new Device(2, 6, "Prof. Clara Oswald", "clara.o@campus.edu", "Prof Clara MacBook Pro", "LAPTOP", "SN-APL-774192", "A4:83:E7:2B:10:99", "macOS Sonoma 14.2", "10.20.10.45", new Timestamp(System.currentTimeMillis()), "ACTIVE", "Faculty research device with encrypted APFS volume"));
        mockDevices.add(new Device(3, 9, "Mohan Lal", "mohan.l@campus.edu", "IT Core Sysadmin Desktop", "DESKTOP", "SN-DEL-109482", "F0:2F:74:18:99:A1", "Windows 11 Pro", "10.10.1.20", new Timestamp(System.currentTimeMillis()), "ACTIVE", "Campus network monitoring workstation in DC-01"));
        mockDevices.add(new Device(4, 12, "David Kim", "david.k21@campus.edu", "David Rog Strix Gaming", "LAPTOP", "SN-ASU-663819", "7C:10:C9:81:4A:22", "Kali Linux 2024.1", "10.20.45.198", new Timestamp(System.currentTimeMillis()), "QUARANTINED", "Detected executing unauthorized mass port scan and brute-force"));
        mockDevices.add(new Device(5, 7, "Prof. Alan Morris", "a.morris@campus.edu", "Prof Morris Dell Latitude", "LAPTOP", "SN-DEL-448190", "58:6D:8F:90:1B:33", "Windows 11 Pro", "10.20.10.88", new Timestamp(System.currentTimeMillis()), "ACTIVE", "Departmental laptop with department share access"));
        mockDevices.add(new Device(6, 11, "Ananya Deshmukh", "ananya.d23@campus.edu", "Ananya Galaxy Book", "LAPTOP", "SN-SAM-330192", "90:B6:86:14:87:D5", "Windows 11 Home", "10.20.45.140", new Timestamp(System.currentTimeMillis()), "ACTIVE", "Student lab workstation connection"));
        mockDevices.add(new Device(7, 3, "Marcus Jackson", "m.jackson@campus.edu", "Forensics Workstation Falcon", "DESKTOP", "SN-CST-882001", "02:00:4C:4F:4F:01", "CAINE OS 13.0 Forensic", "10.10.5.10", new Timestamp(System.currentTimeMillis()), "ACTIVE", "Write-blocked acquisition and memory forensic platform"));
        mockDevices.add(new Device(8, 4, "Sarah Jenkins", "s.jenkins@campus.edu", "Forensics Portable Lab", "LAPTOP", "SN-HP-559123", "E4:A7:A0:51:77:88", "SANS SIFT Workstation", "10.10.5.15", new Timestamp(System.currentTimeMillis()), "ACTIVE", "Field response unit with Tableau write blockers"));
        mockDevices.add(new Device(9, 10, "Vikram Aditya", "vikram.a22@campus.edu", "OnePlus 11 5G Phone", "SMARTPHONE", "SN-ONE-817263", "B2:18:4E:99:43:AA", "Android 14", "10.30.12.80", new Timestamp(System.currentTimeMillis()), "ACTIVE", "Secondary mobile device used for campus WiFi MFA"));
        mockDevices.add(new Device(10, 1, "Dr. Rajesh Sharma", "admin@campus.edu", "Admin Enterprise Workstation", "DESKTOP", "SN-HP-990112", "18:66:DA:22:90:FF", "Windows 11 Enterprise", "10.10.1.5", new Timestamp(System.currentTimeMillis()), "ACTIVE", "Chief forensics administrator terminal"));
    }

    public List<Device> getAllDevices() {
        String sql = "SELECT d.*, u.full_name AS owner_name, u.email AS owner_email " +
                     "FROM devices d INNER JOIN campus_users u ON d.user_id = u.user_id " +
                     "ORDER BY d.device_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            List<Device> list = new ArrayList<>();
            while (rs.next()) {
                list.add(extractDevice(rs));
            }
            return list;
        } catch (SQLException e) {
            System.err.println("DeviceDAO fallback to mock data: " + e.getMessage());
            return new ArrayList<>(mockDevices);
        }
    }

    public boolean addDevice(Device device) {
        String sql = "INSERT INTO devices (user_id, device_name, device_type, serial_number, mac_address, operating_system, ip_address, status, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, device.getUserId());
            stmt.setString(2, device.getDeviceName());
            stmt.setString(3, device.getDeviceType());
            stmt.setString(4, device.getSerialNumber());
            stmt.setString(5, device.getMacAddress());
            stmt.setString(6, device.getOperatingSystem());
            stmt.setString(7, device.getIpAddress());
            stmt.setString(8, device.getStatus());
            stmt.setString(9, device.getNotes());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) device.setDeviceId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            device.setDeviceId(mockDevices.size() + 1);
            device.setRegistrationDate(new Timestamp(System.currentTimeMillis()));
            mockDevices.add(device);
            return true;
        }
        return false;
    }

    public boolean updateDevice(Device device) {
        String sql = "UPDATE devices SET user_id = ?, device_name = ?, device_type = ?, serial_number = ?, mac_address = ?, operating_system = ?, ip_address = ?, status = ?, notes = ? " +
                     "WHERE device_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, device.getUserId());
            stmt.setString(2, device.getDeviceName());
            stmt.setString(3, device.getDeviceType());
            stmt.setString(4, device.getSerialNumber());
            stmt.setString(5, device.getMacAddress());
            stmt.setString(6, device.getOperatingSystem());
            stmt.setString(7, device.getIpAddress());
            stmt.setString(8, device.getStatus());
            stmt.setString(9, device.getNotes());
            stmt.setInt(10, device.getDeviceId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            for (int i = 0; i < mockDevices.size(); i++) {
                if (mockDevices.get(i).getDeviceId() == device.getDeviceId()) {
                    mockDevices.set(i, device);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean deleteDevice(int deviceId) {
        String sql = "DELETE FROM devices WHERE device_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, deviceId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return mockDevices.removeIf(d -> d.getDeviceId() == deviceId);
        }
    }

    public boolean isSerialExists(String serial, int excludeDeviceId) {
        String sql = "SELECT COUNT(*) FROM devices WHERE serial_number = ? AND device_id != ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, serial);
            stmt.setInt(2, excludeDeviceId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            for (Device d : mockDevices) {
                if (d.getSerialNumber().equalsIgnoreCase(serial) && d.getDeviceId() != excludeDeviceId) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<Device> searchDevices(String query) {
        if (query == null || query.isBlank()) return getAllDevices();
        String sql = "SELECT d.*, u.full_name AS owner_name, u.email AS owner_email " +
                     "FROM devices d INNER JOIN campus_users u ON d.user_id = u.user_id " +
                     "WHERE d.device_name LIKE ? OR d.serial_number LIKE ? OR d.mac_address LIKE ? OR d.ip_address LIKE ? OR u.full_name LIKE ? OR d.device_type LIKE ?";
        String wild = "%" + query.trim() + "%";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 1; i <= 6; i++) stmt.setString(i, wild);
            try (ResultSet rs = stmt.executeQuery()) {
                List<Device> list = new ArrayList<>();
                while (rs.next()) list.add(extractDevice(rs));
                return list;
            }
        } catch (SQLException e) {
            List<Device> filtered = new ArrayList<>();
            String lower = query.toLowerCase();
            for (Device d : mockDevices) {
                if (d.getDeviceName().toLowerCase().contains(lower) ||
                    d.getSerialNumber().toLowerCase().contains(lower) ||
                    d.getMacAddress().toLowerCase().contains(lower) ||
                    (d.getIpAddress() != null && d.getIpAddress().contains(lower)) ||
                    (d.getOwnerName() != null && d.getOwnerName().toLowerCase().contains(lower))) {
                    filtered.add(d);
                }
            }
            return filtered;
        }
    }

    private Device extractDevice(ResultSet rs) throws SQLException {
        return new Device(
            rs.getInt("device_id"),
            rs.getInt("user_id"),
            rs.getString("owner_name"),
            rs.getString("owner_email"),
            rs.getString("device_name"),
            rs.getString("device_type"),
            rs.getString("serial_number"),
            rs.getString("mac_address"),
            rs.getString("operating_system"),
            rs.getString("ip_address"),
            rs.getTimestamp("registration_date"),
            rs.getString("status"),
            rs.getString("notes")
        );
    }
}
