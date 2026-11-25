package models;

import java.sql.Timestamp;

public class Evidence {
    private int evidenceId;
    private int caseId;
    private String caseNumber;
    private String evidenceCode;
    private String evidenceName;
    private String evidenceType;
    private String description;
    private String fileReference;
    private String sha256Hash;
    private int collectedById;
    private String collectorName;
    private Timestamp collectionDate;
    private String storageLocation;
    private String status;

    public Evidence() {}

    public Evidence(int evidenceId, int caseId, String caseNumber, String evidenceCode, 
                    String evidenceName, String evidenceType, String description, 
                    String fileReference, String sha256Hash, int collectedById, 
                    String collectorName, Timestamp collectionDate, 
                    String storageLocation, String status) {
        this.evidenceId = evidenceId;
        this.caseId = caseId;
        this.caseNumber = caseNumber;
        this.evidenceCode = evidenceCode;
        this.evidenceName = evidenceName;
        this.evidenceType = evidenceType;
        this.description = description;
        this.fileReference = fileReference;
        this.sha256Hash = sha256Hash;
        this.collectedById = collectedById;
        this.collectorName = collectorName;
        this.collectionDate = collectionDate;
        this.storageLocation = storageLocation;
        this.status = status;
    }

    public int getEvidenceId() { return evidenceId; }
    public void setEvidenceId(int evidenceId) { this.evidenceId = evidenceId; }

    public int getCaseId() { return caseId; }
    public void setCaseId(int caseId) { this.caseId = caseId; }

    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }

    public String getEvidenceCode() { return evidenceCode; }
    public void setEvidenceCode(String evidenceCode) { this.evidenceCode = evidenceCode; }

    public String getEvidenceName() { return evidenceName; }
    public void setEvidenceName(String evidenceName) { this.evidenceName = evidenceName; }

    public String getEvidenceType() { return evidenceType; }
    public void setEvidenceType(String evidenceType) { this.evidenceType = evidenceType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getFileReference() { return fileReference; }
    public void setFileReference(String fileReference) { this.fileReference = fileReference; }

    public String getSha256Hash() { return sha256Hash; }
    public void setSha256Hash(String sha256Hash) { this.sha256Hash = sha256Hash; }

    public int getCollectedById() { return collectedById; }
    public void setCollectedById(int collectedById) { this.collectedById = collectedById; }

    public String getCollectorName() { return collectorName; }
    public void setCollectorName(String collectorName) { this.collectorName = collectorName; }

    public Timestamp getCollectionDate() { return collectionDate; }
    public void setCollectionDate(Timestamp collectionDate) { this.collectionDate = collectionDate; }

    public String getStorageLocation() { return storageLocation; }
    public void setStorageLocation(String storageLocation) { this.storageLocation = storageLocation; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return evidenceCode + " - " + evidenceName + " (" + status + ")";
    }
}
