# Campus Digital Forensics System (CDF) - Database Architecture & Schema Specification

## 1. Relational Database Overview
- **Database Engine**: MySQL 8.0+ (InnoDB Storage Engine)
- **Database Name**: `campus_digital_forensics`
- **Character Set**: `utf8mb4`
- **Collation**: `utf8mb4_unicode_ci`
- **ACID Support**: Fully supported via InnoDB Transactions and Row-Level Locking

---

## 2. Entity-Relationship (ER) Diagram

```mermaid
erDiagram
    campus_users ||--o{ devices : "registers/owns"
    campus_users ||--o{ cases : "reports"
    campus_users ||--o{ event_logs : "initiates"
    campus_users ||--o{ access_logs : "swipes card at"
    campus_users ||--o{ case_users : "involved as"
    campus_users ||--o{ case_assignments : "assigned as investigator"
    campus_users ||--o{ evidence : "collects"
    campus_users ||--o{ evidence_custody : "transfers from/to"
    campus_users ||--o{ investigation_activities : "performs"
    campus_users ||--o{ case_outcomes : "finalizes"

    locations ||--o{ cases : "incident locus"
    locations ||--o{ event_logs : "network endpoint"
    locations ||--o{ access_logs : "card reader reader"

    devices ||--o{ event_logs : "generates telemetry"

    cases ||--o{ case_users : "links people"
    cases ||--o{ case_assignments : "assigned to"
    cases ||--o{ evidence : "contains"
    cases ||--o{ investigation_activities : "chronicles"
    cases ||--|| case_outcomes : "concludes with"

    evidence ||--o{ evidence_custody : "tracked through"

    campus_users {
        int user_id PK
        string username UK
        string password_hash
        string full_name
        string email UK
        string department
        enum role
        enum status
        string phone_number
        timestamp created_at
    }

    devices {
        int device_id PK
        int user_id FK
        string device_name
        enum device_type
        string serial_number UK
        string mac_address
        string operating_system
        string ip_address
        enum status
        timestamp registration_date
    }

    locations {
        int location_id PK
        string building_name
        string room_number
        enum zone_type
        enum access_level_required
        string description
    }

    cases {
        int case_id PK
        string case_number UK
        string title
        enum incident_type
        text description
        enum severity
        enum status
        int location_id FK
        int reported_by_user_id FK
        timestamp reported_at
        timestamp resolved_at
    }

    event_logs {
        int log_id PK
        int user_id FK
        int device_id FK
        int location_id FK
        enum event_type
        string ip_address
        text event_details
        timestamp event_timestamp
    }

    access_logs {
        int access_id PK
        int user_id FK
        int location_id FK
        string card_number
        enum access_result
        timestamp access_timestamp
    }

    case_users {
        int case_user_id PK
        int case_id FK
        int user_id FK
        enum association_type
        text notes
        timestamp associated_at
    }

    case_assignments {
        int assignment_id PK
        int case_id FK
        int investigator_id FK
        int assigned_by FK
        enum assignment_status
        boolean lead_investigator
        timestamp assigned_date
    }

    evidence {
        int evidence_id PK
        int case_id FK
        string evidence_code UK
        string evidence_name
        enum evidence_type
        string file_reference
        string sha256_hash
        int collected_by_id FK
        string storage_location
        enum status
        timestamp collection_date
    }

    evidence_custody {
        int custody_id PK
        int evidence_id FK
        int transferred_from_id FK
        int transferred_to_id FK
        enum action_type
        text purpose_remarks
        boolean integrity_verified
        timestamp transfer_date
    }

    investigation_activities {
        int activity_id PK
        int case_id FK
        int investigator_id FK
        enum activity_type
        string activity_summary
        text detailed_notes
        timestamp activity_timestamp
    }

    case_outcomes {
        int outcome_id PK
        int case_id FK,UK
        text findings_summary
        enum recommended_action
        int finalized_by_id FK
        timestamp resolution_date
        boolean is_appealable
    }

    audit_logs {
        int audit_id PK
        string table_name
        int record_id
        enum action_performed
        string performed_by_user
        text change_details
        timestamp timestamp
    }
```

---

## 3. Relational Cardinality & Referential Integrity Summary

| Primary Entity | Child / Associative Entity | Relationship Cardinality | Constraint Behavior | Forensic & Business Purpose |
|---|---|---|---|---|
| `campus_users` | `devices` | 1-to-Many (`1:N`) | `ON DELETE CASCADE` | Associates physical hardware assets to campus account holders. |
| `campus_users` | `case_users` | 1-to-Many (`1:N`) | `ON DELETE CASCADE` | Captures individual roles (Suspect, Victim, Witness, Reporting Party). |
| `cases` | `case_users` | 1-to-Many (`1:N`) | `ON DELETE CASCADE` | Resolves Many-to-Many (`M:N`) between Incidents and Campus Users. |
| `cases` | `case_assignments` | 1-to-Many (`1:N`) | `ON DELETE CASCADE` | Manages investigator casework assignments and lead analyst roles. |
| `cases` | `evidence` | 1-to-Many (`1:N`) | `ON DELETE CASCADE` | Groups all seized digital artifacts under an incident dossier. |
| `evidence` | `evidence_custody` | 1-to-Many (`1:N`) | `ON DELETE CASCADE` | Preserves immutable chronological chain-of-custody transfers. |
| `cases` | `case_outcomes` | 1-to-1 (`1:1`) | `UNIQUE (case_id)` | Enforces exactly one final outcome per completed investigation. |
| `locations` | `cases` | 1-to-Many (`1:N`) | `ON DELETE SET NULL` | Maps incident locus while preserving history if room renumbered. |
| `locations` | `access_logs` | 1-to-Many (`1:N`) | `ON DELETE CASCADE` | Physical card-swipe entry logs across campus facilities. |

---

## 4. Normalization Principles Demonstrated

1. **First Normal Form (1NF)**:
   - All columns contain atomic, indivisible values.
   - Primary keys uniquely identify every row (`user_id`, `device_id`, `case_id`, etc.).
   - No repeating groups or array values stored in columns.

2. **Second Normal Form (2NF)**:
   - All tables are in 1NF.
   - All non-key attributes are fully functionally dependent on the entire primary key (no partial dependencies). In composite bridge tables like `case_users`, attributes depend on the composite key.

3. **Third Normal Form (3NF)**:
   - All tables are in 2NF.
   - Elimination of transitive dependencies ($X \rightarrow Y$ and $Y \rightarrow Z$). For example, user names and emails are not duplicated in `devices` or `cases`; they are normalized in `campus_users` and referenced via Foreign Keys.

4. **Boyce-Codd Normal Form (BCNF)**:
   - Every determinant is a candidate key throughout the normalized schema.
