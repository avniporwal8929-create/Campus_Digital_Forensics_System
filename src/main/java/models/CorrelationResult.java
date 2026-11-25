package models;

public class CorrelationResult {
    private String targetEntity;
    private String targetType;      // USER, DEVICE, INCIDENT, LOCATION
    private String relatedCategory;  // INCIDENT, EVENT_LOG, ACCESS_LOG, DEVICE, EVIDENCE, ASSIGNMENT
    private String identifier;
    private String summary;
    private String connectionReason; // Explains WHY the records are connected
    private String timestamp;
    private String severityOrStatus;

    public CorrelationResult() {}

    public CorrelationResult(String targetEntity, String targetType, String relatedCategory, 
                             String identifier, String summary, String connectionReason, 
                             String timestamp, String severityOrStatus) {
        this.targetEntity = targetEntity;
        this.targetType = targetType;
        this.relatedCategory = relatedCategory;
        this.identifier = identifier;
        this.summary = summary;
        this.connectionReason = connectionReason;
        this.timestamp = timestamp;
        this.severityOrStatus = severityOrStatus;
    }

    public String getTargetEntity() { return targetEntity; }
    public void setTargetEntity(String targetEntity) { this.targetEntity = targetEntity; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public String getRelatedCategory() { return relatedCategory; }
    public void setRelatedCategory(String relatedCategory) { this.relatedCategory = relatedCategory; }

    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getConnectionReason() { return connectionReason; }
    public void setConnectionReason(String connectionReason) { this.connectionReason = connectionReason; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getSeverityOrStatus() { return severityOrStatus; }
    public void setSeverityOrStatus(String severityOrStatus) { this.severityOrStatus = severityOrStatus; }
}
