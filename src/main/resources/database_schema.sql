-- =====================================================================
-- CAMPUS DIGITAL FORENSICS SYSTEM (CDF)
-- DATABASE INITIALIZATION AND SAMPLE DATA SCRIPT
-- Target DBMS: MySQL 8.0+
-- Database Name: campus_digital_forensics
-- =====================================================================

DROP DATABASE IF EXISTS campus_digital_forensics;
CREATE DATABASE campus_digital_forensics
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE campus_digital_forensics;

-- =====================================================================
-- 1. CAMPUS USERS TABLE
-- Roles: STUDENT, FACULTY, STAFF, INVESTIGATOR, ADMIN
-- Status: ACTIVE, SUSPENDED, INACTIVE
-- =====================================================================
CREATE TABLE campus_users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(64) NOT NULL COMMENT 'SHA-256 Hex Hash',
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    department VARCHAR(100) NOT NULL,
    role ENUM('STUDENT', 'FACULTY', 'STAFF', 'INVESTIGATOR', 'ADMIN') NOT NULL,
    status ENUM('ACTIVE', 'SUSPENDED', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    phone_number VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_role (role),
    INDEX idx_user_email (email),
    INDEX idx_user_dept (department)
) ENGINE=InnoDB;

-- =====================================================================
-- 2. DEVICES TABLE
-- Types: LAPTOP, DESKTOP, SMARTPHONE, TABLET, SERVER, IOT_DEVICE
-- Status: ACTIVE, SEIZED, QUARANTINED, DECOMMISSIONED
-- =====================================================================
CREATE TABLE devices (
    device_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    device_name VARCHAR(100) NOT NULL,
    device_type ENUM('LAPTOP', 'DESKTOP', 'SMARTPHONE', 'TABLET', 'SERVER', 'IOT_DEVICE') NOT NULL,
    serial_number VARCHAR(100) NOT NULL UNIQUE,
    mac_address VARCHAR(17) NOT NULL,
    operating_system VARCHAR(50) NOT NULL,
    ip_address VARCHAR(45),
    registration_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status ENUM('ACTIVE', 'SEIZED', 'QUARANTINED', 'DECOMMISSIONED') NOT NULL DEFAULT 'ACTIVE',
    notes TEXT,
    CONSTRAINT fk_devices_user FOREIGN KEY (user_id) 
        REFERENCES campus_users(user_id) ON DELETE CASCADE,
    INDEX idx_device_serial (serial_number),
    INDEX idx_device_mac (mac_address),
    INDEX idx_device_status (status)
) ENGINE=InnoDB;

-- =====================================================================
-- 3. LOCATIONS TABLE
-- Zones: ACADEMIC, LABORATORY, ADMINISTRATIVE, HOSTEL, DATA_CENTER, LIBRARY
-- =====================================================================
CREATE TABLE locations (
    location_id INT AUTO_INCREMENT PRIMARY KEY,
    building_name VARCHAR(100) NOT NULL,
    room_number VARCHAR(50) NOT NULL,
    zone_type ENUM('ACADEMIC', 'LABORATORY', 'ADMINISTRATIVE', 'HOSTEL', 'DATA_CENTER', 'LIBRARY') NOT NULL,
    access_level_required ENUM('PUBLIC', 'STUDENT', 'FACULTY', 'RESTRICTED', 'SECURITY_ONLY') NOT NULL DEFAULT 'STUDENT',
    description VARCHAR(255),
    UNIQUE KEY uq_location_room (building_name, room_number),
    INDEX idx_location_zone (zone_type)
) ENGINE=InnoDB;

-- =====================================================================
-- 4. CASES (INCIDENTS) TABLE
-- Severity: LOW, MEDIUM, HIGH, CRITICAL
-- Status: OPEN, UNDER_INVESTIGATION, RESOLVED, CLOSED
-- =====================================================================
CREATE TABLE cases (
    case_id INT AUTO_INCREMENT PRIMARY KEY,
    case_number VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    incident_type ENUM(
        'UNAUTHORIZED_ACCESS', 
        'DATA_EXFILTRATION', 
        'MALWARE_OUTBREAK', 
        'EXAM_TAMPERING', 
        'NETWORK_INTRUSION', 
        'CREDENTIAL_STUFFING', 
        'PHISHING_ATTACK', 
        'POLICY_VIOLATION'
    ) NOT NULL,
    description TEXT NOT NULL,
    severity ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') NOT NULL DEFAULT 'MEDIUM',
    status ENUM('OPEN', 'UNDER_INVESTIGATION', 'RESOLVED', 'CLOSED') NOT NULL DEFAULT 'OPEN',
    location_id INT NULL,
    reported_by_user_id INT NULL,
    reported_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_cases_location FOREIGN KEY (location_id) 
        REFERENCES locations(location_id) ON DELETE SET NULL,
    CONSTRAINT fk_cases_reporter FOREIGN KEY (reported_by_user_id) 
        REFERENCES campus_users(user_id) ON DELETE SET NULL,
    INDEX idx_case_status (status),
    INDEX idx_case_severity (severity),
    INDEX idx_case_type (incident_type),
    INDEX idx_case_reported_at (reported_at)
) ENGINE=InnoDB;

-- =====================================================================
-- 5. EVENT LOGS TABLE (Network, System & Security Events)
-- =====================================================================
CREATE TABLE event_logs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NULL,
    device_id INT NULL,
    location_id INT NULL,
    event_type ENUM(
        'USER_LOGIN', 
        'USER_LOGOUT', 
        'AUTH_FAILURE', 
        'DEVICE_CONNECTED', 
        'DEVICE_DISCONNECTED', 
        'FILE_ACCESSED', 
        'PORT_SCAN_DETECTED', 
        'MALWARE_TRIGGER', 
        'PRIVILEGE_ESCALATION'
    ) NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    event_details TEXT NOT NULL,
    event_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_event_user FOREIGN KEY (user_id) 
        REFERENCES campus_users(user_id) ON DELETE SET NULL,
    CONSTRAINT fk_event_device FOREIGN KEY (device_id) 
        REFERENCES devices(device_id) ON DELETE SET NULL,
    CONSTRAINT fk_event_location FOREIGN KEY (location_id) 
        REFERENCES locations(location_id) ON DELETE SET NULL,
    INDEX idx_event_type (event_type),
    INDEX idx_event_time (event_timestamp),
    INDEX idx_event_ip (ip_address)
) ENGINE=InnoDB;

-- =====================================================================
-- 6. ACCESS LOGS TABLE (Physical RFID / Access Card Swipes)
-- =====================================================================
CREATE TABLE access_logs (
    access_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    location_id INT NOT NULL,
    card_number VARCHAR(50) NOT NULL,
    access_result ENUM('GRANTED', 'DENIED_INVALID_CARD', 'DENIED_PERMISSION', 'DENIED_OFF_HOURS') NOT NULL,
    access_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_access_user FOREIGN KEY (user_id) 
        REFERENCES campus_users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_access_location FOREIGN KEY (location_id) 
        REFERENCES locations(location_id) ON DELETE CASCADE,
    INDEX idx_access_time (access_timestamp),
    INDEX idx_access_card (card_number),
    INDEX idx_access_result (access_result)
) ENGINE=InnoDB;

-- =====================================================================
-- 7. CASE USERS TABLE (Many-to-Many: Person Involvement)
-- Types: SUSPECT, VICTIM, WITNESS, REPORTING_PARTY, PERSON_OF_INTEREST
-- =====================================================================
CREATE TABLE case_users (
    case_user_id INT AUTO_INCREMENT PRIMARY KEY,
    case_id INT NOT NULL,
    user_id INT NOT NULL,
    association_type ENUM('SUSPECT', 'VICTIM', 'WITNESS', 'REPORTING_PARTY', 'PERSON_OF_INTEREST') NOT NULL,
    notes TEXT,
    associated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_case_user_case FOREIGN KEY (case_id) 
        REFERENCES cases(case_id) ON DELETE CASCADE,
    CONSTRAINT fk_case_user_user FOREIGN KEY (user_id) 
        REFERENCES campus_users(user_id) ON DELETE CASCADE,
    UNIQUE KEY uq_case_user_assoc (case_id, user_id, association_type)
) ENGINE=InnoDB;

-- =====================================================================
-- 8. CASE ASSIGNMENTS TABLE (Investigators assigned to cases)
-- Status: ASSIGNED, IN_PROGRESS, COMPLETED, REASSIGNED
-- =====================================================================
CREATE TABLE case_assignments (
    assignment_id INT AUTO_INCREMENT PRIMARY KEY,
    case_id INT NOT NULL,
    investigator_id INT NOT NULL,
    assigned_by INT NULL,
    assigned_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assignment_status ENUM('ASSIGNED', 'IN_PROGRESS', 'COMPLETED', 'REASSIGNED') NOT NULL DEFAULT 'ASSIGNED',
    lead_investigator BOOLEAN NOT NULL DEFAULT FALSE,
    notes TEXT,
    CONSTRAINT fk_assign_case FOREIGN KEY (case_id) 
        REFERENCES cases(case_id) ON DELETE CASCADE,
    CONSTRAINT fk_assign_investigator FOREIGN KEY (investigator_id) 
        REFERENCES campus_users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_assign_by FOREIGN KEY (assigned_by) 
        REFERENCES campus_users(user_id) ON DELETE SET NULL,
    INDEX idx_assign_status (assignment_status)
) ENGINE=InnoDB;

-- =====================================================================
-- 9. EVIDENCE TABLE
-- Types: HARD_DRIVE, USB_DRIVE, NETWORK_PCAP, LOG_ARCHIVE, RAM_DUMP,
--        SMARTPHONE_IMAGE, CARD_SWIPE_EXPORT, SYSTEM_SNAPSHOT
-- Status: SECURED, IN_ANALYSIS, IN_TRANSIT, COURT_EVIDENCE, RETURNED, ARCHIVED
-- =====================================================================
CREATE TABLE evidence (
    evidence_id INT AUTO_INCREMENT PRIMARY KEY,
    case_id INT NOT NULL,
    evidence_code VARCHAR(50) NOT NULL UNIQUE,
    evidence_name VARCHAR(150) NOT NULL,
    evidence_type ENUM(
        'HARD_DRIVE', 
        'USB_DRIVE', 
        'NETWORK_PCAP', 
        'LOG_ARCHIVE', 
        'RAM_DUMP', 
        'SMARTPHONE_IMAGE', 
        'CARD_SWIPE_EXPORT', 
        'SYSTEM_SNAPSHOT'
    ) NOT NULL,
    description TEXT,
    file_reference VARCHAR(255),
    sha256_hash VARCHAR(64) NOT NULL,
    collected_by_id INT NOT NULL,
    collection_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    storage_location VARCHAR(100) NOT NULL,
    status ENUM('SECURED', 'IN_ANALYSIS', 'IN_TRANSIT', 'COURT_EVIDENCE', 'RETURNED', 'ARCHIVED') NOT NULL DEFAULT 'SECURED',
    CONSTRAINT fk_evidence_case FOREIGN KEY (case_id) 
        REFERENCES cases(case_id) ON DELETE CASCADE,
    CONSTRAINT fk_evidence_collector FOREIGN KEY (collected_by_id) 
        REFERENCES campus_users(user_id),
    INDEX idx_evidence_code (evidence_code),
    INDEX idx_evidence_hash (sha256_hash),
    INDEX idx_evidence_status (status)
) ENGINE=InnoDB;

-- =====================================================================
-- 10. EVIDENCE CUSTODY TABLE (Chain of Custody tracking)
-- Preserves custody history instead of deleting historical records
-- =====================================================================
CREATE TABLE evidence_custody (
    custody_id INT AUTO_INCREMENT PRIMARY KEY,
    evidence_id INT NOT NULL,
    transferred_from_id INT NOT NULL,
    transferred_to_id INT NOT NULL,
    transfer_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    action_type ENUM(
        'INITIAL_RECEIPT', 
        'INTERNAL_TRANSFER', 
        'FORENSIC_ANALYSIS', 
        'SECURE_VAULT_DEPOSIT', 
        'ADMINISTRATIVE_REVIEW', 
        'RETURN_TO_OWNER'
    ) NOT NULL,
    purpose_remarks TEXT NOT NULL,
    integrity_verified BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_custody_evidence FOREIGN KEY (evidence_id) 
        REFERENCES evidence(evidence_id) ON DELETE CASCADE,
    CONSTRAINT fk_custody_from FOREIGN KEY (transferred_from_id) 
        REFERENCES campus_users(user_id),
    CONSTRAINT fk_custody_to FOREIGN KEY (transferred_to_id) 
        REFERENCES campus_users(user_id),
    INDEX idx_custody_transfer_date (transfer_date)
) ENGINE=InnoDB;

-- =====================================================================
-- 11. INVESTIGATION ACTIVITIES TABLE (Activity Logs / Timeline)
-- =====================================================================
CREATE TABLE investigation_activities (
    activity_id INT AUTO_INCREMENT PRIMARY KEY,
    case_id INT NOT NULL,
    investigator_id INT NOT NULL,
    activity_type ENUM(
        'EVIDENCE_SEIZURE', 
        'HEX_ANALYSIS', 
        'TIMELINE_RECONSTRUCTION', 
        'INTERVIEW', 
        'LOG_CORRELATION', 
        'REPORT_DRAFTING', 
        'PEER_REVIEW'
    ) NOT NULL,
    activity_summary VARCHAR(200) NOT NULL,
    detailed_notes TEXT,
    activity_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_activity_case FOREIGN KEY (case_id) 
        REFERENCES cases(case_id) ON DELETE CASCADE,
    CONSTRAINT fk_activity_investigator FOREIGN KEY (investigator_id) 
        REFERENCES campus_users(user_id),
    INDEX idx_activity_time (activity_timestamp)
) ENGINE=InnoDB;

-- =====================================================================
-- 12. CASE OUTCOMES TABLE (Final Findings & Sanctions)
-- One-to-One with cases
-- =====================================================================
CREATE TABLE case_outcomes (
    outcome_id INT AUTO_INCREMENT PRIMARY KEY,
    case_id INT NOT NULL UNIQUE,
    findings_summary TEXT NOT NULL,
    recommended_action ENUM(
        'DISCIPLINARY_HEARING', 
        'CAMPUS_EXPULSION', 
        'LAW_ENFORCEMENT_REFERRAL', 
        'NETWORK_PRIVILEGE_REVOKED', 
        'SYSTEM_PATCHED', 
        'NO_ACTION_REQUIRED'
    ) NOT NULL,
    finalized_by_id INT NOT NULL,
    resolution_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_appealable BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_outcome_case FOREIGN KEY (case_id) 
        REFERENCES cases(case_id) ON DELETE CASCADE,
    CONSTRAINT fk_outcome_finalizer FOREIGN KEY (finalized_by_id) 
        REFERENCES campus_users(user_id)
) ENGINE=InnoDB;

-- =====================================================================
-- 13. AUDIT LOGS TABLE
-- Tracks critical changes, triggers, security actions
-- =====================================================================
CREATE TABLE audit_logs (
    audit_id INT AUTO_INCREMENT PRIMARY KEY,
    table_name VARCHAR(50) NOT NULL,
    record_id INT NOT NULL,
    action_performed ENUM('INSERT', 'UPDATE', 'DELETE') NOT NULL,
    performed_by_user VARCHAR(100) NOT NULL,
    change_details TEXT,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_table (table_name),
    INDEX idx_audit_time (timestamp)
) ENGINE=InnoDB;

-- =====================================================================
-- DBMS CONCEPTS: VIEWS
-- =====================================================================

-- VIEW 1: Active Investigations Overview
CREATE OR REPLACE VIEW v_active_investigations AS
SELECT 
    c.case_id,
    c.case_number,
    c.title,
    c.incident_type,
    c.severity,
    c.status,
    c.reported_at,
    loc.building_name,
    loc.room_number,
    rep.full_name AS reported_by,
    COUNT(DISTINCT ca.assignment_id) AS assigned_investigators_count,
    COUNT(DISTINCT ev.evidence_id) AS evidence_count
FROM cases c
LEFT JOIN locations loc ON c.location_id = loc.location_id
LEFT JOIN campus_users rep ON c.reported_by_user_id = rep.user_id
LEFT JOIN case_assignments ca ON c.case_id = ca.case_id AND ca.assignment_status IN ('ASSIGNED', 'IN_PROGRESS')
LEFT JOIN evidence ev ON c.case_id = ev.case_id
GROUP BY c.case_id, c.case_number, c.title, c.incident_type, c.severity, c.status, c.reported_at, loc.building_name, loc.room_number, rep.full_name;

-- VIEW 2: Complete Evidence Chain of Custody Timeline
CREATE OR REPLACE VIEW v_evidence_custody_chain AS
SELECT 
    ec.custody_id,
    ev.evidence_id,
    ev.evidence_code,
    ev.evidence_name,
    c.case_number,
    u_from.full_name AS transferred_from_name,
    u_from.role AS transferred_from_role,
    u_to.full_name AS transferred_to_name,
    u_to.role AS transferred_to_role,
    ec.transfer_date,
    ec.action_type,
    ec.purpose_remarks,
    ec.integrity_verified
FROM evidence_custody ec
INNER JOIN evidence ev ON ec.evidence_id = ev.evidence_id
INNER JOIN cases c ON ev.case_id = c.case_id
INNER JOIN campus_users u_from ON ec.transferred_from_id = u_from.user_id
INNER JOIN campus_users u_to ON ec.transferred_to_id = u_to.user_id;

-- VIEW 3: Investigator Workload Summary (Aggregate & Group By)
CREATE OR REPLACE VIEW v_investigator_workload AS
SELECT 
    u.user_id AS investigator_id,
    u.full_name,
    u.email,
    u.department,
    COUNT(ca.assignment_id) AS total_assignments,
    SUM(CASE WHEN ca.assignment_status = 'IN_PROGRESS' THEN 1 ELSE 0 END) AS active_cases,
    SUM(CASE WHEN ca.assignment_status = 'COMPLETED' THEN 1 ELSE 0 END) AS completed_cases,
    COUNT(DISTINCT act.activity_id) AS activities_logged
FROM campus_users u
LEFT JOIN case_assignments ca ON u.user_id = ca.investigator_id
LEFT JOIN investigation_activities act ON u.user_id = act.investigator_id
WHERE u.role IN ('INVESTIGATOR', 'ADMIN')
GROUP BY u.user_id, u.full_name, u.email, u.department;

-- =====================================================================
-- DBMS CONCEPTS: TRIGGERS
-- =====================================================================
DELIMITER $$

-- Trigger 1: Automatically log status changes on cases
DROP TRIGGER IF EXISTS trg_audit_case_status_update$$
CREATE TRIGGER trg_audit_case_status_update
AFTER UPDATE ON cases
FOR EACH ROW
BEGIN
    IF OLD.status != NEW.status THEN
        INSERT INTO audit_logs (table_name, record_id, action_performed, performed_by_user, change_details)
        VALUES ('cases', NEW.case_id, 'UPDATE', 'SYSTEM_AUDITOR', 
                CONCAT('Status updated from ', OLD.status, ' to ', NEW.status, ' for Case: ', NEW.case_number));
    END IF;
    IF OLD.severity != NEW.severity THEN
        INSERT INTO audit_logs (table_name, record_id, action_performed, performed_by_user, change_details)
        VALUES ('cases', NEW.case_id, 'UPDATE', 'SYSTEM_AUDITOR', 
                CONCAT('Severity changed from ', OLD.severity, ' to ', NEW.severity, ' for Case: ', NEW.case_number));
    END IF;
END$$

-- Trigger 2: Automatically audit evidence registration
DROP TRIGGER IF EXISTS trg_audit_evidence_insert$$
CREATE TRIGGER trg_audit_evidence_insert
AFTER INSERT ON evidence
FOR EACH ROW
BEGIN
    INSERT INTO audit_logs (table_name, record_id, action_performed, performed_by_user, change_details)
    VALUES ('evidence', NEW.evidence_id, 'INSERT', 'SYSTEM_AUDITOR', 
            CONCAT('New evidence registered: ', NEW.evidence_code, ' - ', NEW.evidence_name, ' (SHA-256: ', LEFT(NEW.sha256_hash, 16), '...)'));
END$$

-- Trigger 3: Automatically audit evidence custody transfers
DROP TRIGGER IF EXISTS trg_audit_custody_transfer$$
CREATE TRIGGER trg_audit_custody_transfer
AFTER INSERT ON evidence_custody
FOR EACH ROW
BEGIN
    INSERT INTO audit_logs (table_name, record_id, action_performed, performed_by_user, change_details)
    VALUES ('evidence_custody', NEW.custody_id, 'INSERT', 'SYSTEM_AUDITOR', 
            CONCAT('Custody transfer for Evidence ID ', NEW.evidence_id, ': Action=', NEW.action_type, ', From=', NEW.transferred_from_id, ' to ', NEW.transferred_to_id));
END$$

-- =====================================================================
-- DBMS CONCEPTS: STORED PROCEDURES
-- =====================================================================

-- Procedure 1: Transfer Evidence Custody within Transaction
DROP PROCEDURE IF EXISTS sp_transfer_evidence_custody$$
CREATE PROCEDURE sp_transfer_evidence_custody(
    IN p_evidence_id INT,
    IN p_transferred_from_id INT,
    IN p_transferred_to_id INT,
    IN p_action_type VARCHAR(50),
    IN p_purpose_remarks TEXT,
    IN p_new_status VARCHAR(50),
    OUT p_status_msg VARCHAR(255)
)
proc_label: BEGIN
    DECLARE v_count INT;
    
    -- Check if evidence exists
    SELECT COUNT(*) INTO v_count FROM evidence WHERE evidence_id = p_evidence_id;
    IF v_count = 0 THEN
        SET p_status_msg = 'ERROR: Evidence ID does not exist';
        LEAVE proc_label;
    END IF;

    -- Execute transaction
    START TRANSACTION;
        -- Insert custody record
        INSERT INTO evidence_custody (
            evidence_id, transferred_from_id, transferred_to_id, action_type, purpose_remarks, integrity_verified
        ) VALUES (
            p_evidence_id, p_transferred_from_id, p_transferred_to_id, p_action_type, p_purpose_remarks, TRUE
        );

        -- Update evidence status if specified
        IF p_new_status IS NOT NULL AND p_new_status != '' THEN
            UPDATE evidence 
            SET status = p_new_status 
            WHERE evidence_id = p_evidence_id;
        END IF;
    COMMIT;

    SET p_status_msg = 'SUCCESS: Custody transfer recorded';
END$$

-- Procedure 2: Assign Investigator to Case
DROP PROCEDURE IF EXISTS sp_assign_investigator$$
CREATE PROCEDURE sp_assign_investigator(
    IN p_case_id INT,
    IN p_investigator_id INT,
    IN p_assigned_by INT,
    IN p_lead BOOLEAN,
    IN p_notes TEXT,
    OUT p_result VARCHAR(255)
)
proc_label: BEGIN
    DECLARE v_role VARCHAR(20);
    
    -- Verify investigator role
    SELECT role INTO v_role FROM campus_users WHERE user_id = p_investigator_id;
    IF v_role NOT IN ('INVESTIGATOR', 'ADMIN') THEN
        SET p_result = 'ERROR: User is not an authorized investigator or administrator';
        LEAVE proc_label;
    END IF;

    -- Insert assignment
    INSERT INTO case_assignments (case_id, investigator_id, assigned_by, lead_investigator, notes)
    VALUES (p_case_id, p_investigator_id, p_assigned_by, p_lead, p_notes);

    -- Update case status to UNDER_INVESTIGATION if currently OPEN
    UPDATE cases 
    SET status = 'UNDER_INVESTIGATION' 
    WHERE case_id = p_case_id AND status = 'OPEN';

    SET p_result = 'SUCCESS: Investigator assigned';
END$$

DELIMITER ;

-- =====================================================================
-- REALISTIC SAMPLE DATA
-- Password for all sample users: "password123"
-- SHA-256 for "password123":
-- ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f
-- For "admin123":
-- 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
-- =====================================================================

-- 1. Insert Campus Users (12 Users: 2 Admins, 3 Investigators, 3 Faculty, 3 Students, 1 Staff)
INSERT INTO campus_users (user_id, username, password_hash, full_name, email, department, role, status, phone_number) VALUES
(1, 'admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Dr. Rajesh Sharma', 'admin@campus.edu', 'Cybersecurity Division', 'ADMIN', 'ACTIVE', '+91-9876543210'),
(2, 'sec_lead', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Elena Vance', 'elena.vance@campus.edu', 'Campus Security Operations', 'ADMIN', 'ACTIVE', '+91-9876543211'),
(3, 'inv_marcus', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'Marcus Thorne', 'm.thorne@campus.edu', 'Digital Forensics Unit', 'INVESTIGATOR', 'ACTIVE', '+91-9876543212'),
(4, 'inv_sarah', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'Sarah Jenkins', 's.jenkins@campus.edu', 'Digital Forensics Unit', 'INVESTIGATOR', 'ACTIVE', '+91-9876543213'),
(5, 'inv_arjun', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'Arjun Kapoor', 'a.kapoor@campus.edu', 'Incident Response Team', 'INVESTIGATOR', 'ACTIVE', '+91-9876543214'),
(6, 'prof_clara', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'Prof. Clara Oswald', 'clara.o@campus.edu', 'Computer Science & Eng', 'FACULTY', 'ACTIVE', '+91-9876543215'),
(7, 'prof_morris', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'Prof. Alan Morris', 'a.morris@campus.edu', 'Information Systems', 'FACULTY', 'ACTIVE', '+91-9876543216'),
(8, 'dr_kavita', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'Dr. Kavita Rao', 'kavita.r@campus.edu', 'Dean Academics Office', 'FACULTY', 'ACTIVE', '+91-9876543217'),
(9, 'staff_mohan', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'Mohan Lal', 'mohan.l@campus.edu', 'IT Services Infrastructure', 'STAFF', 'ACTIVE', '+91-9876543218'),
(10, 'stu_vikram', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'Vikram Aditya', 'vikram.a22@campus.edu', 'Computer Science & Eng', 'STUDENT', 'ACTIVE', '+91-9876543219'),
(11, 'stu_ananya', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'Ananya Deshmukh', 'ananya.d23@campus.edu', 'Electrical Engineering', 'STUDENT', 'ACTIVE', '+91-9876543220'),
(12, 'stu_david', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'David Kim', 'david.k21@campus.edu', 'Mechanical Engineering', 'STUDENT', 'SUSPENDED', '+91-9876543221');

-- 2. Insert Campus Locations (6 Locations)
INSERT INTO locations (location_id, building_name, room_number, zone_type, access_level_required, description) VALUES
(1, 'Turing Technology Center', 'Lab-301 (AI & Forensics)', 'LABORATORY', 'RESTRICTED', 'High performance compute and digital forensic workstations'),
(2, 'Aryabhata Computing Complex', 'Server Room DC-01', 'DATA_CENTER', 'SECURITY_ONLY', 'Campus core routers, firewall appliances, and SAN storage'),
(3, 'Main Academic Block', 'Room 105 (Exam Cell)', 'ADMINISTRATIVE', 'FACULTY', 'Confidential exam papers and grading records repository'),
(4, 'Ramanujan Library', 'Digital Reading Hall 2', 'LIBRARY', 'STUDENT', 'Public terminal stations and high speed wireless access area'),
(5, 'Newton Hall of Residence', 'Block-C 4th Floor', 'HOSTEL', 'STUDENT', 'Undergraduate hostel dormitory network hub'),
(6, 'Admin Tower', 'Dean Examination Chambers', 'ADMINISTRATIVE', 'SECURITY_ONLY', 'Executive administrative suites and primary database terminals');

-- 3. Insert Devices (10 Registered Devices)
INSERT INTO devices (device_id, user_id, device_name, device_type, serial_number, mac_address, operating_system, ip_address, status, notes) VALUES
(1, 10, 'Vikram ThinkPad X1', 'LAPTOP', 'SN-LEN-984210', '00:1A:2B:3C:4D:5E', 'Ubuntu Linux 22.04', '10.20.45.112', 'SEIZED', 'Confiscated during exam tampering investigation'),
(2, 6, 'Prof Clara MacBook Pro', 'LAPTOP', 'SN-APL-774192', 'A4:83:E7:2B:10:99', 'macOS Sonoma 14.2', '10.20.10.45', 'ACTIVE', 'Faculty research device with encrypted APFS volume'),
(3, 9, 'IT Core Sysadmin Desktop', 'DESKTOP', 'SN-DEL-109482', 'F0:2F:74:18:99:A1', 'Windows 11 Pro', '10.10.1.20', 'ACTIVE', 'Campus network monitoring workstation in DC-01'),
(4, 12, 'David Rog Strix Gaming', 'LAPTOP', 'SN-ASU-663819', '7C:10:C9:81:4A:22', 'Kali Linux 2024.1', '10.20.45.198', 'QUARANTINED', 'Detected executing unauthorized mass port scan and brute-force'),
(5, 7, 'Prof Morris Dell Latitude', 'LAPTOP', 'SN-DEL-448190', '58:6D:8F:90:1B:33', 'Windows 11 Pro', '10.20.10.88', 'ACTIVE', 'Departmental laptop with department share access'),
(6, 11, 'Ananya Galaxy Book', 'LAPTOP', 'SN-SAM-330192', '90:B6:86:14:87:D5', 'Windows 11 Home', '10.20.45.140', 'ACTIVE', 'Student lab workstation connection'),
(7, 3, 'Forensics Workstation Falcon', 'DESKTOP', 'SN-CST-882001', '02:00:4C:4F:4F:01', 'CAINE OS 13.0 Forensic', '10.10.5.10', 'ACTIVE', 'Write-blocked acquisition and memory forensic platform'),
(8, 4, 'Forensics Portable Lab', 'LAPTOP', 'SN-HP-559123', 'E4:A7:A0:51:77:88', 'SANS SIFT Workstation', '10.10.5.15', 'ACTIVE', 'Field response unit with Tableau write blockers'),
(9, 10, 'OnePlus 11 5G Phone', 'SMARTPHONE', 'SN-ONE-817263', 'B2:18:4E:99:43:AA', 'Android 14', '10.30.12.80', 'ACTIVE', 'Secondary mobile device used for campus WiFi MFA'),
(10, 1, 'Admin Enterprise Workstation', 'DESKTOP', 'SN-HP-990112', '18:66:DA:22:90:FF', 'Windows 11 Enterprise', '10.10.1.5', 'ACTIVE', 'Chief forensics administrator terminal');

-- 4. Insert Cases (Incidents) (8 Cases)
INSERT INTO cases (case_id, case_number, title, incident_type, description, severity, status, location_id, reported_by_user_id, reported_at) VALUES
(1, 'CDF-2026-001', 'Mid-Term Exam Paper Leak via Exam Cell', 'EXAM_TAMPERING', 'CS401 Computer Networks question paper leaked online 4 hours before scheduled examination. Digital unauthorized intrusion detected in Exam Cell NAS.', 'CRITICAL', 'UNDER_INVESTIGATION', 3, 8, '2026-10-12 08:30:00'),
(2, 'CDF-2026-002', 'Data Center Core Switch ARP Poisoning Attack', 'NETWORK_INTRUSION', 'Widespread packet capture and man-in-the-middle ARP spoofing detected redirecting administrative subnet traffic in Aryabhata Complex.', 'HIGH', 'UNDER_INVESTIGATION', 2, 9, '2026-10-15 14:15:00'),
(3, 'CDF-2026-003', 'Ransomware Infection in Mechanical Dept Lab', 'MALWARE_OUTBREAK', 'LockBit variant detected encrypting CAD files on 5 lab desktop machines. Prompt isolation prevented lateral propagation across campus VLANs.', 'HIGH', 'RESOLVED', 1, 6, '2026-09-28 11:00:00'),
(4, 'CDF-2026-004', 'Mass Phishing Campaign Impersonating Dean Office', 'PHISHING_ATTACK', 'Spoofed emails sent to 450 students soliciting portal passwords under guise of scholarship verification. 18 credentials compromised.', 'MEDIUM', 'CLOSED', 4, 1, '2026-09-10 09:20:00'),
(5, 'CDF-2026-005', 'Hostel C Off-Hours Port Scanning and SSH Brute Force', 'UNAUTHORIZED_ACCESS', 'Over 10,000 automated SSH connection attempts originating from IP 10.20.45.198 targeting campus grading server between 02:00 and 04:00 AM.', 'HIGH', 'UNDER_INVESTIGATION', 5, 2, '2026-10-18 03:15:00'),
(6, 'CDF-2026-006', 'Unauthorized USB Insertion in Restricted Research Lab', 'POLICY_VIOLATION', 'Unapproved USB mass storage device connected to AI model training cluster containing proprietary research dataset.', 'MEDIUM', 'RESOLVED', 1, 6, '2026-10-02 16:40:00'),
(7, 'CDF-2026-007', 'Credential Stuffing against Student ERP Portal', 'CREDENTIAL_STUFFING', 'Distributed login attempts using compromised third-party credential dump across campus portal API endpoints.', 'MEDIUM', 'OPEN', 2, 9, '2026-10-24 19:50:00'),
(8, 'CDF-2026-008', 'Suspected Academic Research Data Exfiltration', 'DATA_EXFILTRATION', 'Abnormal outbound HTTPS payload transfer of 42GB to external Mega.nz endpoint during non-instructional weekend hours.', 'CRITICAL', 'OPEN', 1, 7, '2026-10-25 01:10:00');

-- 5. Insert Event Logs (15 Events)
INSERT INTO event_logs (log_id, user_id, device_id, location_id, event_type, ip_address, event_details, event_timestamp) VALUES
(1, 10, 1, 3, 'USER_LOGIN', '10.20.45.112', 'SSH session initiated to Exam Cell Local Gateway 10.20.10.15:22', '2026-10-12 02:14:20'),
(2, 10, 1, 3, 'FILE_ACCESSED', '10.20.45.112', 'File downloaded: /srv/shares/exams/2026_midsem/CS401_Final.pdf', '2026-10-12 02:22:15'),
(3, 10, 1, 3, 'USER_LOGOUT', '10.20.45.112', 'SSH session terminated cleanly after transferring 1.4 MB', '2026-10-12 02:24:05'),
(4, 12, 4, 5, 'PORT_SCAN_DETECTED', '10.20.45.198', 'Nmap SYN stealth scan executed against 10.20.10.0/24 subnet (ports 21, 22, 80, 443, 3306, 8080)', '2026-10-18 02:45:00'),
(5, 12, 4, 5, 'AUTH_FAILURE', '10.20.45.198', 'Failed SSH authentication for root user on grading server 10.10.1.20 (attempt 148)', '2026-10-18 03:02:11'),
(6, 12, 4, 5, 'AUTH_FAILURE', '10.20.45.198', 'Failed SSH authentication for admin user on grading server 10.10.1.20 (attempt 320)', '2026-10-18 03:08:44'),
(7, 9, 3, 2, 'DEVICE_CONNECTED', '10.10.1.20', 'Cisco Catalyst 9300 switch management console attached via Serial RJ45', '2026-10-15 14:20:00'),
(8, NULL, 4, 2, 'NETWORK_INTRUSION', '10.20.45.198', 'Gratuitous ARP replies sent claiming default gateway MAC address (MAC Spoof)', '2026-10-15 14:25:30'),
(9, 6, 2, 1, 'DEVICE_CONNECTED', '10.20.10.45', 'External SanDisk Extreme SSD 1TB plugged into research workstation port 2', '2026-10-02 16:30:10'),
(10, 6, 2, 1, 'USER_LOGIN', '10.20.10.45', 'Kerberos single-sign-on authenticated for Prof. Clara Oswald in AI Lab', '2026-10-02 16:31:00'),
(11, 10, 9, 4, 'USER_LOGIN', '10.30.12.80', 'Radius 802.1X wireless login on SSID Campus-Secure via OnePlus 11', '2026-10-12 08:00:15'),
(12, 11, 6, 4, 'USER_LOGIN', '10.20.45.140', 'Portal session opened from Digital Reading Hall workstation 14', '2026-10-24 19:45:10'),
(13, NULL, NULL, 2, 'MALWARE_TRIGGER', '10.10.1.99', 'Snort IDS rule alert: ET TROJAN LockBit Ransomware outbound handshake detected', '2026-09-28 10:45:22'),
(14, 3, 7, 1, 'DEVICE_CONNECTED', '10.10.5.10', 'Tableau T8u USB 3.0 Forensic Forensic Bridge connected to analysis machine', '2026-10-13 10:00:00'),
(15, 4, 8, 1, 'PRIVILEGE_ESCALATION', '10.10.5.15', 'Local sudo session opened for investigator Jenkins to mount disk image read-only', '2026-10-13 11:30:00');

-- 6. Insert Access Logs (Physical Card Swipes) (10 Swipes)
INSERT INTO access_logs (access_id, user_id, location_id, card_number, access_result, access_timestamp) VALUES
(1, 10, 3, 'CRD-STU-99014', 'DENIED_OFF_HOURS', '2026-10-12 01:55:10'),
(2, 10, 3, 'CRD-FAC-77419', 'GRANTED', '2026-10-12 02:05:00'),
(3, 6, 1, 'CRD-FAC-77419', 'GRANTED', '2026-10-02 16:25:00'),
(4, 12, 5, 'CRD-STU-66381', 'GRANTED', '2026-10-18 01:30:00'),
(5, 9, 2, 'CRD-STF-10948', 'GRANTED', '2026-10-15 14:05:00'),
(6, 10, 4, 'CRD-STU-99014', 'GRANTED', '2026-10-12 07:50:00'),
(7, 3, 1, 'CRD-INV-88200', 'GRANTED', '2026-10-13 09:45:00'),
(8, 4, 1, 'CRD-INV-55912', 'GRANTED', '2026-10-13 09:50:00'),
(9, 12, 2, 'CRD-STU-66381', 'DENIED_PERMISSION', '2026-10-15 13:58:00'),
(10, 8, 3, 'CRD-ADM-00192', 'GRANTED', '2026-10-12 08:15:00');

-- 7. Insert Case Users (Person Involvement)
INSERT INTO case_users (case_user_id, case_id, user_id, association_type, notes) VALUES
(1, 1, 10, 'SUSPECT', 'Matched IP and card swipe during unauthorized access window'),
(2, 1, 8, 'REPORTING_PARTY', 'Dean Academics who reported examination compromise'),
(3, 1, 6, 'VICTIM', 'Faculty whose credential / access card was cloned or misplaced'),
(4, 2, 12, 'SUSPECT', 'Gaming laptop identified transmitting spoofed ARP broadcasts'),
(5, 2, 9, 'REPORTING_PARTY', 'Network administrator who noticed abnormal MAC table churn'),
(6, 3, 6, 'REPORTING_PARTY', 'Lab head reporting encrypted files'),
(7, 5, 12, 'SUSPECT', 'Hostel room network port mapped to repeated SSH brute force'),
(8, 6, 6, 'PERSON_OF_INTEREST', 'Owner of unauthorized storage device plugged in AI Lab');

-- 8. Insert Case Assignments
INSERT INTO case_assignments (assignment_id, case_id, investigator_id, assigned_by, assigned_date, assignment_status, lead_investigator, notes) VALUES
(1, 1, 3, 1, '2026-10-12 09:00:00', 'IN_PROGRESS', TRUE, 'Lead forensic analyst on Exam Cell penetration case'),
(2, 1, 4, 1, '2026-10-12 09:15:00', 'IN_PROGRESS', FALSE, 'Co-investigator handling physical evidence acquisition'),
(3, 2, 5, 2, '2026-10-15 15:00:00', 'IN_PROGRESS', TRUE, 'Packet capture and network traffic analysis lead'),
(4, 3, 3, 1, '2026-09-28 11:30:00', 'COMPLETED', TRUE, 'Ransomware isolated, decrypter tested and lab restored'),
(5, 4, 4, 1, '2026-09-10 10:00:00', 'COMPLETED', TRUE, 'Domain takedown coordinated and compromised creds reset'),
(6, 5, 5, 2, '2026-10-18 08:00:00', 'IN_PROGRESS', TRUE, 'Investigating source of hostel SSH attacks'),
(7, 8, 3, 1, '2026-10-25 09:00:00', 'ASSIGNED', TRUE, 'Assigned to triage large data transfer breach');

-- 9. Insert Evidence Records (9 Evidence items)
INSERT INTO evidence (evidence_id, case_id, evidence_code, evidence_name, evidence_type, description, file_reference, sha256_hash, collected_by_id, collection_date, storage_location, status) VALUES
(1, 1, 'EVD-2026-001', 'Seized Lenovo ThinkPad X1 NVMe SSD (512GB)', 'HARD_DRIVE', 'Raw bit-stream image acquired from Vikram ThinkPad using Tableau forensic bridge', '/evidence/cases/CDF-2026-001/nvme_01.raw', 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855', 3, '2026-10-13 10:15:00', 'Forensic Safe Cabinet A - Shelf 2', 'IN_ANALYSIS'),
(2, 1, 'EVD-2026-002', 'Exam Cell NAS Access Logs Tarball', 'LOG_ARCHIVE', 'Apache, SSH and Samba access logs covering Oct 10 to Oct 13 2026', '/evidence/cases/CDF-2026-001/exam_cell_logs.tar.gz', '7d793037a0760186574b0282f2f435e70d7168765f974381508cc704192323ed', 4, '2026-10-12 11:00:00', 'Forensics Server Repository /evd/002', 'SECURED'),
(3, 1, 'EVD-2026-003', 'RAM Memory Dump of ThinkPad Laptop', 'RAM_DUMP', 'Volatile memory captured using LiME kernel module prior to power off', '/evidence/cases/CDF-2026-001/ram_capture.dump', 'a5835925230d303b60057f6488344e4735870ff0617037877e6114e931e61f28', 3, '2026-10-13 09:40:00', 'Forensic Vault High Priority Storage', 'IN_ANALYSIS'),
(4, 2, 'EVD-2026-004', 'Aryabhata Core Switch PCAP Dump (12.4GB)', 'NETWORK_PCAP', 'Promiscuous port mirror capture of malicious ARP responses and sniffing payload', '/evidence/cases/CDF-2026-002/core_mirror_oct15.pcap', '126a8e44b64f5f37d2823c72f7b83641a2286612ec131a4000da4545e6735292', 5, '2026-10-15 16:30:00', 'Forensic Storage SAN Array Vol 3', 'IN_ANALYSIS'),
(5, 3, 'EVD-2026-005', 'Infected CAD Lab Machine Snapshot (VMDK)', 'SYSTEM_SNAPSHOT', 'Forensic disk copy containing LockBit ransomware sample and encrypted test vector', '/evidence/cases/CDF-2026-003/lab_pc4_snap.vmdk', '8f434346648f6b96df89dda901c5176b10e6d0ceec3e6a14bbb47d0ca73ab04c', 3, '2026-09-28 14:00:00', 'Offline Malware Isolation Safe', 'ARCHIVED'),
(6, 4, 'EVD-2026-006', 'Phishing Kit Source HTML and PHP Script Dump', 'LOG_ARCHIVE', 'Recovered malicious harvest scripts hosted on hijacked campus sub-domain', '/evidence/cases/CDF-2026-004/phish_kit_src.zip', 'ca978112ca1bbdcafac231b39a23dc4da786eff8147c4e72b9807785afee48bb', 4, '2026-09-10 12:45:00', 'Forensic Server Repository /evd/006', 'ARCHIVED'),
(7, 5, 'EVD-2026-007', 'Hostel C Switch Port 24 Traffic PCAP', 'NETWORK_PCAP', '24-hour network capture exhibiting systematic brute-force dictionary traffic', '/evidence/cases/CDF-2026-005/hostel_c_p24.pcap', '9f83c24c9152f627324de376707ba13f74f42501a6ec6e69280e1184e60104be', 5, '2026-10-18 10:15:00', 'Forensic Storage SAN Array Vol 2', 'SECURED'),
(8, 6, 'EVD-2026-008', 'SanDisk Extreme SSD 1TB Hardware Device', 'USB_DRIVE', 'Physical SSD seized from AI Lab connected to unauthorized computer', '/evidence/cases/CDF-2026-006/sandisk_1tb.dd', '3b10e777d3fc7e3420e79546e2a5d1641609b32d8b03373a43183556e3a77162', 4, '2026-10-02 17:30:00', 'Evidence Locker Room B - Bin 14', 'RETURNED'),
(9, 8, 'EVD-2026-009', 'Border Firewall Mega.nz Flow NetFlow Logs', 'LOG_ARCHIVE', 'Cisco ASA NetFlow records verifying 42GB SSL exfiltration session', '/evidence/cases/CDF-2026-008/netflow_oct25.csv', 'b843109a96e549d4be21ec7e2f518e1d283c784f183c5a6d38e2118f6735e294', 3, '2026-10-25 08:30:00', 'Forensics Server Repository /evd/009', 'SECURED');

-- 10. Insert Evidence Custody Transfers (6 Custody Transitions)
INSERT INTO evidence_custody (custody_id, evidence_id, transferred_from_id, transferred_to_id, transfer_date, action_type, purpose_remarks, integrity_verified) VALUES
(1, 1, 4, 3, '2026-10-13 10:45:00', 'INTERNAL_TRANSFER', 'Transferred NVMe bitstream image from acquisition field kit to lead forensic investigator', TRUE),
(2, 1, 3, 3, '2026-10-13 14:00:00', 'FORENSIC_ANALYSIS', 'Mounted image in readonly mode using Autopsy for deleted SQLite chat and file recovery', TRUE),
(3, 2, 4, 1, '2026-10-12 12:00:00', 'SECURE_VAULT_DEPOSIT', 'Exam cell log archive deposited into central secure evidence server vault', TRUE),
(4, 4, 5, 5, '2026-10-15 17:00:00', 'FORENSIC_ANALYSIS', 'Opened network packet trace in Wireshark for ARP Poisoning signature validation', TRUE),
(5, 5, 3, 1, '2026-10-01 09:30:00', 'ADMINISTRATIVE_REVIEW', 'Delivered snapshot analysis report to Chief Security Administrator Dr. Sharma', TRUE),
(6, 8, 4, 6, '2026-10-05 16:00:00', 'RETURN_TO_OWNER', 'Returned seized SSD to Prof. Clara after verifying no classified data resided thereon', TRUE);

-- 11. Insert Investigation Activities (8 Activities)
INSERT INTO investigation_activities (activity_id, case_id, investigator_id, activity_type, activity_summary, detailed_notes, activity_timestamp) VALUES
(1, 1, 3, 'EVIDENCE_SEIZURE', 'Seizure of Laptop and Volatile Memory', 'Executed immediate seizure of student laptop in hostel following Dean authorization. Acquired RAM before reboot.', '2026-10-13 09:30:00'),
(2, 1, 3, 'HEX_ANALYSIS', 'File Signature and Deleted MFT Record Scan', 'Recovered deleted PDF matching MD5 hash of official CS401 exam paper from unallocated clusters.', '2026-10-14 11:20:00'),
(3, 1, 4, 'INTERVIEW', 'Subject Interview with Vikram Aditya', 'Conducted formal interview with student regarding card swipe anomaly at Exam Cell door at 02:05 AM.', '2026-10-14 15:30:00'),
(4, 2, 5, 'LOG_CORRELATION', 'Correlated MAC Table flapping with Rogue IP', 'Identified MAC 7C:10:C9:81:4A:22 advertising gateway IP 10.20.45.1 across 4 switch trunk ports.', '2026-10-16 09:45:00'),
(5, 3, 3, 'TIMELINE_RECONSTRUCTION', 'Patient Zero Infection Vector Analysis', 'Identified phishing link opened in faculty web browser initiating Powershell payload download.', '2026-09-29 14:00:00'),
(6, 4, 4, 'REPORT_DRAFTING', 'Comprehensive Post-Incident Phishing Dossier', 'Compiled IOC report and submitted recommendations for campus-wide FIDO2 MFA enforcement.', '2026-09-12 16:00:00'),
(7, 5, 5, 'LOG_CORRELATION', 'Correlation of Auth Log Failures with Port Swipes', 'Confirmed over 9,000 attempts against root account originated from Student David Kim room port.', '2026-10-19 11:15:00'),
(8, 1, 3, 'PEER_REVIEW', 'Peer Review of Autopsy Artifact Findings', 'Reviewed SQLite forensic extraction results with Administrator Dr. Sharma for evidentiary integrity.', '2026-10-16 17:00:00');

-- 12. Insert Case Outcomes (2 Completed Cases)
INSERT INTO case_outcomes (outcome_id, case_id, findings_summary, recommended_action, finalized_by_id, resolution_date, is_appealable) VALUES
(1, 3, 'Ransomware outbreak contained to 5 isolated lab endpoints without data exfiltration. Automated shadow copies allowed 100% CAD file recovery. Network segmentation policy verified.', 'SYSTEM_PATCHED', 1, '2026-10-01 10:00:00', FALSE),
(2, 4, 'Phishing campaign credential harvesters neutralized within 90 minutes. 18 affected accounts reset without unauthorized logins detected. Campus DNS sinkholing rules applied.', 'NO_ACTION_REQUIRED', 1, '2026-09-12 17:30:00', FALSE);

-- 13. Insert Initial Audit Logs
INSERT INTO audit_logs (audit_id, table_name, record_id, action_performed, performed_by_user, change_details, timestamp) VALUES
(1, 'cases', 1, 'INSERT', 'admin', 'Created Case CDF-2026-001 (Mid-Term Exam Paper Leak via Exam Cell)', '2026-10-12 08:30:00'),
(2, 'evidence', 1, 'INSERT', 'inv_marcus', 'Registered primary seized NVMe SSD evidence EVD-2026-001', '2026-10-13 10:15:00'),
(3, 'evidence_custody', 1, 'INSERT', 'inv_marcus', 'Custody transfer of EVD-2026-001 from inv_sarah to inv_marcus for analysis', '2026-10-13 10:45:00'),
(4, 'cases', 3, 'UPDATE', 'admin', 'Status updated to RESOLVED for Case CDF-2026-003', '2026-10-01 10:00:00'),
(5, 'case_outcomes', 1, 'INSERT', 'admin', 'Finalized outcome and remediation for Ransomware Incident', '2026-10-01 10:00:00');

COMMIT;
