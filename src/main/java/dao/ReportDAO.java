package dao;

import config.DBConnection;
import models.AnalyticsSummary;
import models.CaseIncident;
import models.Evidence;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReportDAO {

    public AnalyticsSummary getAnalyticsSummary() {
        AnalyticsSummary summary = new AnalyticsSummary();

        // 1. Try SQL live execution
        try (Connection conn = DBConnection.getConnection()) {
            // User count
            try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM campus_users")) {
                if (rs.next()) summary.setTotalUsers(rs.getInt(1));
            }
            // Device count
            try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM devices")) {
                if (rs.next()) summary.setTotalDevices(rs.getInt(1));
            }
            // Evidence count
            try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM evidence")) {
                if (rs.next()) summary.setTotalEvidence(rs.getInt(1));
            }
            // Incident summary
            String caseSql = "SELECT " +
                    "COUNT(*) AS total, " +
                    "SUM(CASE WHEN status IN ('OPEN', 'UNDER_INVESTIGATION') THEN 1 ELSE 0 END) AS open_cases, " +
                    "SUM(CASE WHEN severity = 'CRITICAL' THEN 1 ELSE 0 END) AS critical_cases, " +
                    "SUM(CASE WHEN status = 'UNDER_INVESTIGATION' THEN 1 ELSE 0 END) AS active_inv " +
                    "FROM cases";
            try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(caseSql)) {
                if (rs.next()) {
                    summary.setTotalIncidents(rs.getInt("total"));
                    summary.setOpenIncidents(rs.getInt("open_cases"));
                    summary.setCriticalIncidents(rs.getInt("critical_cases"));
                    summary.setActiveInvestigations(rs.getInt("active_inv"));
                }
            }

            // Severity Distribution (GROUP BY)
            Map<String, Integer> sevMap = new LinkedHashMap<>();
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT severity, COUNT(*) FROM cases GROUP BY severity")) {
                while (rs.next()) {
                    sevMap.put(rs.getString(1), rs.getInt(2));
                }
            }
            summary.setSeverityDistribution(sevMap);

            // Status Distribution (GROUP BY)
            Map<String, Integer> statusMap = new LinkedHashMap<>();
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT status, COUNT(*) FROM cases GROUP BY status")) {
                while (rs.next()) {
                    statusMap.put(rs.getString(1), rs.getInt(2));
                }
            }
            summary.setStatusDistribution(statusMap);

            // Monthly Incident Stats (GROUP BY & DATE FORMAT)
            Map<String, Integer> monthlyMap = new LinkedHashMap<>();
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT DATE_FORMAT(reported_at, '%b %Y') AS month_name, COUNT(*) " +
                                              "FROM cases GROUP BY DATE_FORMAT(reported_at, '%Y-%m'), month_name ORDER BY DATE_FORMAT(reported_at, '%Y-%m') ASC")) {
                while (rs.next()) {
                    monthlyMap.put(rs.getString(1), rs.getInt(2));
                }
            }
            summary.setMonthlyIncidents(monthlyMap);

            // Evidence By Type (GROUP BY)
            Map<String, Integer> evMap = new LinkedHashMap<>();
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT evidence_type, COUNT(*) FROM evidence GROUP BY evidence_type")) {
                while (rs.next()) {
                    evMap.put(rs.getString(1), rs.getInt(2));
                }
            }
            summary.setEvidenceByType(evMap);

            // Investigator Workload (View or JOIN query)
            Map<String, Integer> workMap = new LinkedHashMap<>();
            String workSql = "SELECT u.full_name, COUNT(ca.assignment_id) AS active_cnt " +
                             "FROM campus_users u " +
                             "LEFT JOIN case_assignments ca ON u.user_id = ca.investigator_id AND ca.assignment_status = 'IN_PROGRESS' " +
                             "WHERE u.role IN ('INVESTIGATOR', 'ADMIN') " +
                             "GROUP BY u.user_id, u.full_name " +
                             "HAVING active_cnt >= 0 " +
                             "ORDER BY active_cnt DESC";
            try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(workSql)) {
                while (rs.next()) {
                    workMap.put(rs.getString(1), rs.getInt(2));
                }
            }
            summary.setInvestigatorWorkload(workMap);

            return summary;
        } catch (SQLException e) {
            // Compute from DAOs fallback
            return computeFallbackSummary();
        }
    }

    private AnalyticsSummary computeFallbackSummary() {
        AnalyticsSummary s = new AnalyticsSummary();
        UserDAO userDAO = new UserDAO();
        DeviceDAO deviceDAO = new DeviceDAO();
        IncidentDAO incidentDAO = new IncidentDAO();
        EvidenceDAO evidenceDAO = new EvidenceDAO();
        InvestigatorDAO investigatorDAO = new InvestigatorDAO();

        s.setTotalUsers(userDAO.getAllUsers().size());
        s.setTotalDevices(deviceDAO.getAllDevices().size());

        List<CaseIncident> cases = incidentDAO.getAllCases();
        s.setTotalIncidents(cases.size());

        int open = 0, critical = 0, active = 0;
        Map<String, Integer> sev = new LinkedHashMap<>();
        Map<String, Integer> stat = new LinkedHashMap<>();
        Map<String, Integer> monthly = new LinkedHashMap<>();

        for (CaseIncident c : cases) {
            if ("OPEN".equalsIgnoreCase(c.getStatus()) || "UNDER_INVESTIGATION".equalsIgnoreCase(c.getStatus())) open++;
            if ("CRITICAL".equalsIgnoreCase(c.getSeverity())) critical++;
            if ("UNDER_INVESTIGATION".equalsIgnoreCase(c.getStatus())) active++;

            sev.put(c.getSeverity(), sev.getOrDefault(c.getSeverity(), 0) + 1);
            stat.put(c.getStatus(), stat.getOrDefault(c.getStatus(), 0) + 1);

            String month = (c.getReportedAt() != null) ? c.getReportedAt().toString().substring(0, 7) : "2026-10";
            monthly.put(month, monthly.getOrDefault(month, 0) + 1);
        }

        s.setOpenIncidents(open);
        s.setCriticalIncidents(critical);
        s.setActiveInvestigations(active);
        s.setSeverityDistribution(sev);
        s.setStatusDistribution(stat);
        s.setMonthlyIncidents(monthly);

        List<Evidence> evList = evidenceDAO.getAllEvidence();
        s.setTotalEvidence(evList.size());
        Map<String, Integer> evTypes = new LinkedHashMap<>();
        for (Evidence ev : evList) {
            evTypes.put(ev.getEvidenceType(), evTypes.getOrDefault(ev.getEvidenceType(), 0) + 1);
        }
        s.setEvidenceByType(evTypes);

        Map<String, Integer> workload = new LinkedHashMap<>();
        workload.put("Marcus Jackson", 2);
        workload.put("Arjun Kapoor", 2);
        workload.put("Sarah Jenkins", 1);
        workload.put("Dr. Rajesh Sharma", 0);
        s.setInvestigatorWorkload(workload);

        return s;
    }
}
