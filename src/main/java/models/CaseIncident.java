package models;

import java.sql.Timestamp;

public class CaseIncident {
    private int caseId;
    private String caseNumber;
    private String title;
    private String incidentType;
    private String description;
    private String severity;       // LOW, MEDIUM, HIGH, CRITICAL
    private String status;         // OPEN, UNDER_INVESTIGATION, RESOLVED, CLOSED
    private Integer locationId;
    private String locationName;
    private Integer reportedByUserId;
    private String reportedByName;
    private Timestamp reportedAt;
    private Timestamp resolvedAt;

    public CaseIncident() {}

    public CaseIncident(int caseId, String caseNumber, String title, String incidentType, 
                        String description, String severity, String status, Integer locationId, 
                        String locationName, Integer reportedByUserId, String reportedByName, 
                        Timestamp reportedAt, Timestamp resolvedAt) {
        this.caseId = caseId;
        this.caseNumber = caseNumber;
        this.title = title;
        this.incidentType = incidentType;
        this.description = description;
        this.severity = severity;
        this.status = status;
        this.locationId = locationId;
        this.locationName = locationName;
        this.reportedByUserId = reportedByUserId;
        this.reportedByName = reportedByName;
        this.reportedAt = reportedAt;
        this.resolvedAt = resolvedAt;
    }

    public int getCaseId() { return caseId; }
    public void setCaseId(int caseId) { this.caseId = caseId; }

    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getIncidentType() { return incidentType; }
    public void setIncidentType(String incidentType) { this.incidentType = incidentType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getLocationId() { return locationId; }
    public void setLocationId(Integer locationId) { this.locationId = locationId; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public Integer getReportedByUserId() { return reportedByUserId; }
    public void setReportedByUserId(Integer reportedByUserId) { this.reportedByUserId = reportedByUserId; }

    public String getReportedByName() { return reportedByName; }
    public void setReportedByName(String reportedByName) { this.reportedByName = reportedByName; }

    public Timestamp getReportedAt() { return reportedAt; }
    public void setReportedAt(Timestamp reportedAt) { this.reportedAt = reportedAt; }

    public Timestamp getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Timestamp resolvedAt) { this.resolvedAt = resolvedAt; }

    @Override
    public String toString() {
        return caseNumber + " - " + title + " (" + severity + ")";
    }
}
