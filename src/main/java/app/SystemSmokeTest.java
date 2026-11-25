package app;

import dao.*;
import models.*;
import services.AuthService;
import services.ForensicsService;

import java.sql.Timestamp;
import java.util.List;

public class SystemSmokeTest {
    public static void main(String[] args) {
        System.out.println(">>> STARTING CDF SYSTEM INTEGRITY & REGRESSION TESTS <<<");
        int passed = 0;
        int failed = 0;

        // Test 1: Authentication & Hashing
        try {
            System.out.print("[TEST 1] Authentication & SHA-256 Hashing: ");
            String hash = AuthService.hashPassword("admin123");
            if (!hash.equals("240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9")) {
                throw new AssertionError("Password hash mismatch: " + hash);
            }
            AuthService auth = AuthService.getInstance();
            boolean loginOk = auth.login("admin", "admin123");
            if (!loginOk || !auth.isAdmin()) {
                throw new AssertionError("Admin login failed");
            }
            System.out.println("PASSED (Admin authenticated)");
            passed++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            failed++;
        }

        // Test 2: User DAO CRUD & Safeguards
        try {
            System.out.print("[TEST 2] Module 1 - User Management CRUD: ");
            UserDAO userDAO = new UserDAO();
            List<User> users = userDAO.getAllUsers();
            if (users.size() < 10) throw new AssertionError("Expected at least 10 users, found: " + users.size());

            User testUser = new User();
            testUser.setUsername("test_analyst");
            testUser.setPasswordHash(AuthService.hashPassword("test1234"));
            testUser.setFullName("Test Analyst");
            testUser.setEmail("analyst.test@campus.edu");
            testUser.setDepartment("Forensics");
            testUser.setRole("INVESTIGATOR");
            testUser.setStatus("ACTIVE");
            testUser.setCreatedAt(new Timestamp(System.currentTimeMillis()));

            userDAO.addUser(testUser);
            List<User> search = userDAO.searchUsers("test_analyst");
            if (search.isEmpty()) throw new AssertionError("Added user not found in search");

            userDAO.deleteUser(testUser.getUserId());
            System.out.println("PASSED (Found " + users.size() + " users, added/searched/deleted)");
            passed++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            failed++;
        }

        // Test 3: Device DAO & Duplicate Serial Prevention
        try {
            System.out.print("[TEST 3] Module 2 - Device Registry & Serial Constraint: ");
            DeviceDAO deviceDAO = new DeviceDAO();
            List<Device> devs = deviceDAO.getAllDevices();
            if (devs.size() < 8) throw new AssertionError("Expected at least 8 devices, found: " + devs.size());

            boolean dup = deviceDAO.isSerialExists("SN-LEN-984210", -1);
            if (!dup) throw new AssertionError("Duplicate serial not detected for existing SN-LEN-984210");
            System.out.println("PASSED (Duplicate serial prevention verified)");
            passed++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            failed++;
        }

        // Test 4: Incident DAO & Status Transitions
        try {
            System.out.print("[TEST 4] Module 3 - Security Incidents & Transitions: ");
            IncidentDAO incDAO = new IncidentDAO();
            List<CaseIncident> cases = incDAO.getAllCases();
            if (cases.size() < 8) throw new AssertionError("Expected at least 8 cases, found: " + cases.size());

            incDAO.updateCaseStatus(1, "UNDER_INVESTIGATION");
            CaseIncident c1 = incDAO.getCaseById(1);
            if (!"UNDER_INVESTIGATION".equalsIgnoreCase(c1.getStatus())) throw new AssertionError("Status update failed");
            System.out.println("PASSED (Active cases: " + cases.size() + ")");
            passed++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            failed++;
        }

        // Test 5: Event & Access Logs
        try {
            System.out.print("[TEST 5] Module 4 - Event & Access Log Telemetry: ");
            LogDAO logDAO = new LogDAO();
            List<EventLog> events = logDAO.getAllEventLogs();
            List<AccessLog> access = logDAO.getAllAccessLogs();
            if (events.size() < 12) throw new AssertionError("Expected at least 12 events, found: " + events.size());
            if (access.size() < 5) throw new AssertionError("Expected at least 5 access logs, found: " + access.size());
            System.out.println("PASSED (" + events.size() + " events, " + access.size() + " card swipes)");
            passed++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            failed++;
        }

        // Test 6: Evidence & SHA-256 Hash Calculation
        try {
            System.out.print("[TEST 6] Module 5 - Evidence Locker & SHA-256 Integrity: ");
            EvidenceDAO evDAO = new EvidenceDAO();
            List<Evidence> evList = evDAO.getAllEvidence();
            if (evList.size() < 8) throw new AssertionError("Expected at least 8 evidence records, found: " + evList.size());

            String hash = ForensicsService.calculateSHA256("FORENSIC_ACQUISITION_DATA_STREAM");
            if (hash.length() != 64) throw new AssertionError("Invalid SHA-256 hash length: " + hash);
            System.out.println("PASSED (" + evList.size() + " evidence records verified)");
            passed++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            failed++;
        }

        // Test 7: Chain of Custody & Transaction
        try {
            System.out.print("[TEST 7] Module 6 - Chain of Custody & Transfer History: ");
            CustodyDAO custodyDAO = new CustodyDAO();
            List<EvidenceCustody> list = custodyDAO.getAllCustodyTransfers();
            if (list.size() < 5) throw new AssertionError("Expected at least 5 custody transfers, found: " + list.size());

            EvidenceCustody testTransfer = new EvidenceCustody(
                999, 1, "EVD-2026-001", "Lenovo ThinkPad SSD", "CDF-2026-001",
                3, "Marcus Jackson", 1, "Dr. Rajesh Sharma",
                new Timestamp(System.currentTimeMillis()), "INTERNAL_TRANSFER",
                "Delivered to central vault for secure long-term archival", true
            );
            boolean ok = custodyDAO.recordTransfer(testTransfer, "SECURED");
            if (!ok) throw new AssertionError("Custody transfer failed");
            System.out.println("PASSED (Custody timeline: " + list.size() + " entries)");
            passed++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            failed++;
        }

        // Test 8: Investigator Management
        try {
            System.out.print("[TEST 8] Module 7 - Investigator Workload & Assignments: ");
            InvestigatorDAO invDAO = new InvestigatorDAO();
            List<CaseAssignment> assignments = invDAO.getAllAssignments();
            List<InvestigationActivity> acts = invDAO.getAllActivities();
            if (assignments.isEmpty() || acts.isEmpty()) throw new AssertionError("Assignments or activities empty");
            System.out.println("PASSED (" + assignments.size() + " assignments, " + acts.size() + " activities)");
            passed++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            failed++;
        }

        // Test 9: Reports & Analytics
        try {
            System.out.print("[TEST 9] Module 8 - Statistical Analytics & Aggregation: ");
            ReportDAO reportDAO = new ReportDAO();
            AnalyticsSummary s = reportDAO.getAnalyticsSummary();
            if (s.getTotalUsers() < 10 || s.getTotalDevices() < 8 || s.getTotalIncidents() < 8) {
                throw new AssertionError("Incomplete summary counts");
            }
            if (s.getSeverityDistribution().isEmpty() || s.getStatusDistribution().isEmpty()) {
                throw new AssertionError("Empty distribution aggregations");
            }
            System.out.println("PASSED (" + s.getTotalIncidents() + " incidents aggregated across " + s.getSeverityDistribution().size() + " severities)");
            passed++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            failed++;
        }

        // Test 10: Investigation Correlation
        try {
            System.out.print("[TEST 10] Module 9 - Multi-Relational Investigation Correlation: ");
            CorrelationDAO corrDAO = new CorrelationDAO();
            List<CorrelationResult> vikram = corrDAO.correlateEntity("USER", "Vikram");
            if (vikram.isEmpty()) throw new AssertionError("No correlations found for user Vikram");

            List<CorrelationResult> case1 = corrDAO.correlateEntity("INCIDENT", "CDF-2026-001");
            if (case1.isEmpty()) throw new AssertionError("No correlations found for case CDF-2026-001");

            System.out.println("PASSED (Vikram: " + vikram.size() + " connections, Case 1: " + case1.size() + " connections)");
            passed++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            failed++;
        }

        System.out.println("==========================================================");
        System.out.printf("CDF VERIFICATION RESULTS: %d PASSED, %d FAILED%n", passed, failed);
        System.out.println("==========================================================");
        if (failed > 0) System.exit(1);
    }
}
