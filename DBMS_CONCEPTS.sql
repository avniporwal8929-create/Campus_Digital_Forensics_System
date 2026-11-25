-- =====================================================================
-- CAMPUS DIGITAL FORENSICS SYSTEM (CDF)
-- DEMONSTRATION OF CORE DBMS CONCEPTS
-- Target DBMS: MySQL 8.0+
-- Database: campus_digital_forensics
-- =====================================================================

USE campus_digital_forensics;

-- =====================================================================
-- 1. PRIMARY KEYS & UNIQUE CONSTRAINTS
-- Ensures entity integrity and unique hardware/case identification.
-- =====================================================================
-- campus_users.user_id (PK), username (UNIQUE), email (UNIQUE)
-- devices.device_id (PK), serial_number (UNIQUE)
-- cases.case_id (PK), case_number (UNIQUE)
-- evidence.evidence_id (PK), evidence_code (UNIQUE)
SELECT 
    table_name, column_name, constraint_name 
FROM information_schema.key_column_usage 
WHERE table_schema = 'campus_digital_forensics' 
  AND constraint_name IN ('PRIMARY', 'serial_number', 'case_number', 'evidence_code');

-- =====================================================================
-- 2. FOREIGN KEYS & REFERENTIAL INTEGRITY
-- Demonstrates parent-child constraints and cascading behaviors.
-- =====================================================================
-- devices.user_id -> campus_users.user_id (ON DELETE CASCADE)
-- cases.location_id -> locations.location_id (ON DELETE SET NULL)
-- evidence.case_id -> cases.case_id (ON DELETE CASCADE)
-- evidence_custody.evidence_id -> evidence.evidence_id (ON DELETE CASCADE)
SELECT 
    table_name, column_name, referenced_table_name, referenced_column_name, constraint_name
FROM information_schema.key_column_usage
WHERE table_schema = 'campus_digital_forensics' 
  AND referenced_table_name IS NOT NULL;

-- =====================================================================
-- 3. CARDINALITY RELATIONSHIPS
-- =====================================================================
-- A. One-to-One: cases <-> case_outcomes (one incident has exactly one final resolution)
SELECT 
    c.case_number, c.title, co.findings_summary, co.recommended_action
FROM cases c
INNER JOIN case_outcomes co ON c.case_id = co.case_id;

-- B. One-to-Many: campus_users -> devices (one user registers multiple devices)
SELECT 
    u.full_name, u.role, COUNT(d.device_id) AS total_devices_registered
FROM campus_users u
LEFT JOIN devices d ON u.user_id = d.user_id
GROUP BY u.user_id, u.full_name, u.role
ORDER BY total_devices_registered DESC;

-- C. Many-to-Many: cases <-> campus_users (linked via bridge table 'case_users')
SELECT 
    c.case_number, u.full_name, cu.association_type, cu.notes
FROM cases c
INNER JOIN case_users cu ON c.case_id = cu.case_id
INNER JOIN campus_users u ON cu.user_id = u.user_id;

-- =====================================================================
-- 4. BASIC CRUD OPERATIONS (INSERT, SELECT, UPDATE, DELETE)
-- =====================================================================
-- INSERT
INSERT INTO audit_logs (table_name, record_id, action_performed, performed_by_user, change_details)
VALUES ('campus_users', 1, 'UPDATE', 'admin', 'Routine security permission review completed');

-- SELECT with WHERE and ORDER BY
SELECT user_id, full_name, email, role, status 
FROM campus_users 
WHERE status = 'ACTIVE' 
ORDER BY role, full_name;

-- UPDATE with WHERE
UPDATE campus_users 
SET phone_number = '+91-9876500000' 
WHERE username = 'stu_vikram';

-- DELETE with WHERE
DELETE FROM audit_logs 
WHERE performed_by_user = 'test_cleanup';

-- =====================================================================
-- 5. INNER JOIN & MULTI-TABLE JOINS
-- Merges normalized evidence, custody transfers, and user profiles.
-- =====================================================================
SELECT 
    ev.evidence_code,
    ev.evidence_name,
    c.case_number,
    c.title AS case_title,
    u_from.full_name AS transferred_from,
    u_to.full_name AS transferred_to,
    ec.transfer_date,
    ec.action_type
FROM evidence_custody ec
INNER JOIN evidence ev ON ec.evidence_id = ev.evidence_id
INNER JOIN cases c ON ev.case_id = c.case_id
INNER JOIN campus_users u_from ON ec.transferred_from_id = u_from.user_id
INNER JOIN campus_users u_to ON ec.transferred_to_id = u_to.user_id
ORDER BY ec.transfer_date ASC;

-- =====================================================================
-- 6. LEFT OUTER JOIN
-- Identifies users who currently have ZERO registered devices or ZERO incidents.
-- =====================================================================
SELECT 
    u.user_id,
    u.full_name,
    u.email,
    u.role,
    COUNT(d.device_id) AS device_count
FROM campus_users u
LEFT JOIN devices d ON u.user_id = d.user_id
GROUP BY u.user_id, u.full_name, u.email, u.role
HAVING device_count = 0;

-- =====================================================================
-- 7. GROUP BY, HAVING & AGGREGATE FUNCTIONS (COUNT, SUM, AVG)
-- Summarizes incident severity distribution with filtered threshold.
-- =====================================================================
SELECT 
    c.severity,
    COUNT(*) AS incident_count,
    SUM(CASE WHEN c.status = 'UNDER_INVESTIGATION' THEN 1 ELSE 0 END) AS active_investigations,
    SUM(CASE WHEN c.status = 'RESOLVED' THEN 1 ELSE 0 END) AS resolved_count
FROM cases c
GROUP BY c.severity
HAVING incident_count > 0
ORDER BY incident_count DESC;

-- =====================================================================
-- 8. SUBQUERIES (Nested SQL Queries)
-- Find all devices owned by suspects in CRITICAL severity incidents.
-- =====================================================================
SELECT 
    d.device_name,
    d.serial_number,
    d.ip_address,
    d.status AS device_status,
    u.full_name AS suspect_name
FROM devices d
INNER JOIN campus_users u ON d.user_id = u.user_id
WHERE d.user_id IN (
    -- Subquery: users associated as SUSPECT in CRITICAL cases
    SELECT cu.user_id
    FROM case_users cu
    WHERE cu.association_type = 'SUSPECT'
      AND cu.case_id IN (
          SELECT case_id FROM cases WHERE severity = 'CRITICAL'
      )
);

-- =====================================================================
-- 9. DATABASE VIEWS
-- Simplifies complex joins for reporting and security abstraction.
-- =====================================================================
-- Querying predefined View: v_active_investigations
SELECT * FROM v_active_investigations WHERE status = 'UNDER_INVESTIGATION';

-- Querying predefined View: v_investigator_workload
SELECT * FROM v_investigator_workload WHERE total_assignments > 0;

-- =====================================================================
-- 10. STORED PROCEDURES
-- Encapsulates business logic, transactions, and role validation.
-- =====================================================================
CALL sp_transfer_evidence_custody(
    1,                                  -- Evidence ID
    3,                                  -- Transferred from Marcus Jackson
    1,                                  -- Transferred to Administrator Sharma
    'SECURE_VAULT_DEPOSIT',             -- Action type
    'Transferred disk clone to secure campus vault following initial hash check',
    'SECURED',                          -- New status
    @status_message                     -- Output message
);
SELECT @status_message AS procedure_result;

-- =====================================================================
-- 11. DATABASE TRIGGERS
-- Automatic audit logging on data modification.
-- =====================================================================
-- Test trigger 'trg_audit_case_status_update'
UPDATE cases 
SET status = 'RESOLVED' 
WHERE case_number = 'CDF-2026-006';

-- Verify trigger generated audit entry
SELECT * FROM audit_logs 
WHERE table_name = 'cases' 
ORDER BY timestamp DESC 
LIMIT 3;

-- =====================================================================
-- 12. TRANSACTIONS (ACID COMPLIANCE)
-- Explicit atomic commit/rollback.
-- =====================================================================
START TRANSACTION;
    -- Step A: Log an activity
    INSERT INTO investigation_activities (case_id, investigator_id, activity_type, activity_summary, detailed_notes)
    VALUES (1, 3, 'PEER_REVIEW', 'Final Evidence Verification', 'Checked all MD5 and SHA-256 hashes against original seizure log');

    -- Step B: Update incident status
    UPDATE cases SET status = 'RESOLVED', resolved_at = CURRENT_TIMESTAMP WHERE case_id = 1;

    -- Step C: Record case outcome
    INSERT INTO case_outcomes (case_id, findings_summary, recommended_action, finalized_by_id)
    VALUES (1, 'Exam leak traced to compromised physical gateway. Full audit conducted.', 'DISCIPLINARY_HEARING', 1);
COMMIT;

-- =====================================================================
-- 13. INDEXING FOR PERFORMANCE OPTIMIZATION
-- =====================================================================
EXPLAIN SELECT * FROM devices WHERE serial_number = 'SN-LEN-984210';
EXPLAIN SELECT * FROM event_logs WHERE event_type = 'PORT_SCAN_DETECTED' AND event_timestamp >= '2026-10-01';
