# CAMPUS DIGITAL FORENSICS SYSTEM (CDF)
> **Enterprise Desktop Incident Management, Digital Forensics & Evidence Chain-of-Custody System**

Developed with **Java (JDK 21)**, **Java Swing (Dark Navy & Electric Blue Cyber Theme)**, **MySQL 8.0 Relational DBMS**, and **JDBC**.

---

## 1. Project Overview

The **Campus Digital Forensics System (CDF)** is an academic and enterprise-grade DBMS desktop application engineered to document, investigate, and manage digital security incidents across a higher education institution.

### Key Capabilities:
- **Comprehensive Lifecycle**: Manages users, hardware assets, incidents, telemetry logs, digital evidence, chain-of-custody transfers, investigator assignments, statistical analytics, and investigative correlation.
- **Academic Simulation**: Realistic simulated campus cyber incidents (Mid-term exam paper leaks, data center ARP spoofing, lab ransomware outbreaks, SSH brute-forcing, student credential stuffing).
- **Relational Integrity**: Strict foreign key constraints, atomic transactions, SHA-256 cryptographic verification, audit triggers, and database views.
- **Cybersecurity Dark UI**: High-contrast dark navy `#0D1B2A` with electric cyan accents `#00E5FF`, card-based metrics, responsive sidebar navigation, and custom anti-aliased charts.

---

## 2. Technology Stack & Prerequisites

| Technology | Specification | Purpose |
|---|---|---|
| **Java** | **JDK 21+** (Tested on Java 21 / 27) | Core Application Runtime & Swing Desktop Engine |
| **GUI Framework** | **Java Swing** | Desktop Graphical User Interface (JFrame, CardLayout, JTable) |
| **Relational DBMS** | **MySQL 8.0+** | Relational Database, ACID Transactions, Views, Stored Procedures |
| **Connectivity** | **JDBC (MySQL Connector/J 8.3.0)** | Prepared Statements, Connection Pool & Error Handling |
| **Build Tool** | **Apache Maven** & **Batch Scripts** | Dependency Management & Lifecycle Compilation |
| **IDE** | **VS Code** | Development Environment |

---

## 3. Nine Core Modules

### 1. User Management
- View, register, update, and search campus community users.
- Role-based authorization: `STUDENT`, `FACULTY`, `STAFF`, `INVESTIGATOR`, `ADMIN`.
- Account status management (`ACTIVE`, `SUSPENDED`, `INACTIVE`) with deletion protection safeguards for active sessions.

### 2. Device Management
- Register institutional and personal devices (Laptops, Desktops, Smartphones, Servers, IoT).
- Automatic ownership association with campus user profiles.
- Enforces strict unique hardware serial numbers (`serial_number UNIQUE`).
- Tracks MAC addresses, assigned IPs, operating systems, and quarantine status.

### 3. Incident Management
- Register and triage campus cybersecurity events.
- Classification types: Unauthorized Access, Data Exfiltration, Malware Outbreaks, Exam Tampering, Network Intrusions, Credential Stuffing, Phishing Campaigns, Policy Violations.
- Severity levels: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`.
- Investigation status: `OPEN`, `UNDER_INVESTIGATION`, `RESOLVED`, `CLOSED`.

### 4. Event & Access Log Telemetry
- **System & Network Logs**: Records simulated SSH logins, port scans, malware triggers, file downloads, and privilege escalations.
- **Physical RFID Card Logs**: Tracks building/room swipe events, granting entry or denying off-hours/unauthorized access.
- Filter by user, source IP, device, and event classification.

### 5. Digital Evidence Locker
- Register seized digital artifacts (Bitstream hard drive images, memory dumps, PCAP traces, log archives).
- Automatic SHA-256 cryptographic hash computation for bit-level integrity verification.
- Integrated on-screen file hash calculator tool for analyzing disk files.
- Clear legal evidentiary disclaimer noting that hashes guarantee bitstream integrity, while chain of custody proves authenticity.

### 6. Chain of Custody Management
- Chronological, tamper-evident record of all evidence handoffs and analytical procedures.
- Captures transferring investigator, receiving party, timestamp, purpose, and integrity status.
- **ACID Transaction**: Atomic execution in `CustodyDAO` ensures custody logs and evidence status updates commit or roll back together.
- Immutable custody history preservation rule (append-only).

### 7. Investigator Management
- View authorized forensic investigators and manage active workloads.
- Assign lead investigators and analysts to incident cases.
- Record detailed investigation activities (Memory analysis, disk carving, subject interviews, timeline reconstruction, peer review).
- Update assignment lifecycle status (`ASSIGNED`, `IN_PROGRESS`, `COMPLETED`).

### 8. Reports & Analytics
- Executive KPI Metric cards: Total Users, Managed Assets, Total Incidents, Open & Critical Cases, Seized Evidence.
- **Custom Anti-Aliased Swing Visual Charts**:
  - Incident Severity Breakdown (Bar visualization)
  - Investigation Status Distribution
  - Evidence Artifact Distribution
  - Investigator Caseload Allocation
- Time-series aggregation demonstrating SQL `GROUP BY DATE_FORMAT(...)`.

### 9. Investigation Correlation Matrix
- Examine complex relationships between Users, Devices, Security Incidents, Telemetry Logs, Locations, and Evidence.
- Search by user name, device serial, case number, or facility room.
- Multi-table SQL `INNER JOIN` operations presenting matching artifacts with **clear forensic rationale explaining WHY records are linked**.
- Evidentiary safeguard: correlation serves as investigative assistance, not automated attribution.

---

## 4. Default Demonstration Credentials

| Username | Password | Role | Department | Description |
|---|---|---|---|---|
| `admin` | `admin123` | **ADMIN** | Cybersecurity Division | Chief Security Administrator |
| `sec_lead` | `admin123` | **ADMIN** | Security Operations | SOC Incident Commander |
| `inv_marcus`| `password123` | **INVESTIGATOR** | Digital Forensics Unit | Lead Forensics Examiner |
| `inv_sarah` | `password123` | **INVESTIGATOR** | Digital Forensics Unit | Forensic Acquisition Specialist |
| `inv_arjun` | `password123` | **INVESTIGATOR** | Incident Response Team | Network Traffic Analyst |
| `prof_clara`| `password123` | **FACULTY** | Computer Science & Eng | Faculty Member |
| `stu_vikram`| `password123` | **STUDENT** | Computer Science & Eng | Student (Associated with Case 1) |

*Note: All passwords are cryptographic SHA-256 hex hashes in the database.*

---

## 5. Database Setup Instructions (MySQL 8.0)

### Step 1: Ensure MySQL Server is Running
Open an elevated terminal (Run as Administrator) or start MySQL via Windows Services:
```powershell
net start MySQL80
```
Or open **MySQL Workbench 8.0** and ensure your local server instance is started.

### Step 2: Initialize Database Schema & Realistic Sample Data
Run the initialization script using the MySQL CLI:
```powershell
mysql -u root -p < database_schema.sql
```
*(Enter your MySQL root password when prompted).*

This script:
1. Creates the database `campus_digital_forensics`.
2. Creates all 13 relational tables with primary keys, foreign keys, unique constraints, and indexes.
3. Creates database views (`v_active_investigations`, `v_evidence_custody_chain`, `v_investigator_workload`).
4. Creates triggers for automatic audit logging.
5. Creates stored procedures (`sp_transfer_evidence_custody`, `sp_assign_investigator`).
6. Populates realistic simulated campus data (12 users, 10 devices, 6 locations, 8 cases, 15 event logs, 10 access swipes, 9 evidence items, 6 custody transfers).

### Step 3: Configure Database Connection
Edit `src/main/resources/database.properties` or create `config.properties` in the project root:
```properties
db.host=localhost
db.port=3306
db.name=campus_digital_forensics
db.user=root
db.password=your_mysql_password
db.allowFallbackDemo=true
```
*Tip: You can also configure connection parameters and test passwords directly within the GUI via the **"⚙ Database"** button in the top navigation bar or login window.*

---

## 6. How to Compile and Run the Application

### Option A: Using the Provided Automated Scripts (Recommended)
1. **Compile**:
   ```cmd
   build.bat
   ```
2. **Run**:
   ```cmd
   run.bat
   ```

### Option B: Using Maven
1. **Compile and execute via Maven**:
   ```cmd
   .\mvnw.cmd compile exec:java
   ```
   *(Or if Maven is in your system PATH: `mvn compile exec:java`)*

2. **Package standalone executable JAR**:
   ```cmd
   .\mvnw.cmd clean package
   ```

### Option C: Running the Pre-compiled Executable JAR
```powershell
java -cp "CampusDigitalForensics.jar;lib\mysql-connector-j-8.3.0.jar;src\main\resources" app.Main
```

### Option D: Running the System Integrity & Regression Tests
To verify all 9 modules and DAOs programmatically:
```powershell
java -cp "bin;lib\mysql-connector-j-8.3.0.jar" app.SystemSmokeTest
```

---

## 7. Project Architecture & Directory Structure

```
Campus_Digital_Forensics_System/
│
├── database_schema.sql          # Complete MySQL 8.0 schema & sample data script
├── DBMS_CONCEPTS.sql            # SQL queries demonstrating 13 core DBMS concepts
├── DATABASE_ARCHITECTURE.md     # Detailed ER diagram, normalization & table specs
├── README.md                    # Project documentation & execution guide
├── pom.xml                      # Maven project configuration (Java 21 + MySQL Connector/J)
├── build.bat                    # Windows automated compile script
├── run.bat                      # Windows application launcher
├── mvnw.cmd                     # Maven wrapper script
├── config.properties.example    # Configuration properties template
│
├── lib/
│   └── mysql-connector-j-8.3.0.jar # MySQL JDBC Type 4 Connector
│
├── src/main/java/
│   ├── app/
│   │   ├── Main.java            # Desktop application entry point
│   │   └── SystemSmokeTest.java # 10-point automated regression test suite
│   ├── config/
│   │   ├── AppConfig.java       # Configuration manager & env overrides
│   │   └── DBConnection.java    # Dedicated JDBC connection manager & diagnostics
│   ├── models/
│   │   ├── User.java
│   │   ├── Device.java
│   │   ├── CampusLocation.java
│   │   ├── CaseIncident.java
│   │   ├── EventLog.java
│   │   ├── AccessLog.java
│   │   ├── Evidence.java
│   │   ├── EvidenceCustody.java
│   │   ├── CaseAssignment.java
│   │   ├── InvestigationActivity.java
│   │   ├── CaseOutcome.java
│   │   ├── CorrelationResult.java
│   │   └── AnalyticsSummary.java
│   ├── dao/
│   │   ├── UserDAO.java
│   │   ├── DeviceDAO.java
│   │   ├── IncidentDAO.java
│   │   ├── LogDAO.java
│   │   ├── EvidenceDAO.java
│   │   ├── CustodyDAO.java
│   │   ├── InvestigatorDAO.java
│   │   ├── ReportDAO.java
│   │   └── CorrelationDAO.java
│   ├── services/
│   │   ├── AuthService.java     # SHA-256 authentication & RBAC
│   │   ├── ForensicsService.java# SHA-256 file/data hashing utility
│   │   └── AuditService.java    # System audit logger
│   └── ui/
│       ├── Theme.java           # Dark navy & electric blue styling system
│       ├── LoginFrame.java      # Authentication frame
│       ├── DashboardFrame.java  # Main application window & module router
│       ├── DashboardPanel.java  # Operations overview
│       ├── components/
│       │   ├── HeaderPanel.java # Top navigation, DB status, user info
│       │   ├── SidebarPanel.java# Persistent navigation drawer
│       │   ├── StatCard.java    # Metric card widget
│       │   ├── SimpleChartPanel.java # Anti-aliased 2D chart renderer
│       │   └── DbConfigDialog.java   # Runtime MySQL configuration dialog
│       ├── users/UserPanel.java
│       ├── devices/DevicePanel.java
│       ├── incidents/IncidentPanel.java
│       ├── events/EventLogPanel.java
│       ├── evidence/EvidencePanel.java
│       ├── custody/CustodyPanel.java
│       ├── investigators/InvestigatorPanel.java
│       ├── reports/ReportsPanel.java
│       └── correlation/CorrelationPanel.java
│
└── src/main/resources/
    ├── database.properties      # Connection properties
    └── database_schema.sql      # Packaged SQL initialization script
```

---

## 8. Summary of DBMS Concepts Demonstrated

1. **Primary Keys & Candidate Keys**: Auto-incrementing primary keys and candidate unique keys (`username`, `email`, `serial_number`, `case_number`, `evidence_code`).
2. **Foreign Keys & Referential Integrity**: Explicit foreign key definitions with cascading deletes (`ON DELETE CASCADE`) and nullification (`ON DELETE SET NULL`).
3. **Cardinality**:
   - One-to-One: `cases` $\leftrightarrow$ `case_outcomes`.
   - One-to-Many: `campus_users` $\rightarrow$ `devices`, `cases` $\rightarrow$ `evidence`.
   - Many-to-Many: `cases` $\leftrightarrow$ `campus_users` (via `case_users`).
4. **Data Normalization**: 1NF, 2NF, 3NF, and BCNF compliant relational schema.
5. **Prepared Statements**: Parameterized queries preventing SQL injection across all DAOs.
6. **Inner & Left Joins**: Relational multi-table joins for asset resolution and audit history.
7. **Aggregations & Grouping**: `COUNT(*)`, `SUM()`, `GROUP BY`, and `HAVING` clauses for KPI generation.
8. **Subqueries**: Nested selection queries finding devices involved in critical security incidents.
9. **Database Views**: Predefined reporting views (`v_active_investigations`, `v_evidence_custody_chain`).
10. **Stored Procedures**: `sp_transfer_evidence_custody` and `sp_assign_investigator`.
11. **Triggers**: Automated audit trail logging for case status updates and evidence insertion.
12. **Transactions (ACID)**: Multi-statement atomic transactions (`commit`/`rollback`) during custody transfers.
13. **Indexing**: Performance indexes on foreign keys, serial numbers, emails, timestamps, and hash digests.
