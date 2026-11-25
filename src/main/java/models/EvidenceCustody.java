package models;

import java.sql.Timestamp;

public class EvidenceCustody {
    private int custodyId;
    private int evidenceId;
    private String evidenceCode;
    private String evidenceName;
    private String caseNumber;
    private int transferredFromId;
    private String transferredFromName;
    private int transferredToId;
    private String transferredToName;
    private Timestamp transferDate;
    private String actionType;
    private String purposeRemarks;
    private boolean integrityVerified;

    public EvidenceCustody() {}

    public EvidenceCustody(int custodyId, int evidenceId, String evidenceCode, 
                           String evidenceName, String caseNumber, int transferredFromId, 
                           String transferredFromName, int transferredToId, 
                           String transferredToName, Timestamp transferDate, 
                           String actionType, String purposeRemarks, 
                           boolean integrityVerified) {
        this.custodyId = custodyId;
        this.evidenceId = evidenceId;
        this.evidenceCode = evidenceCode;
        this.evidenceName = evidenceName;
        this.caseNumber = caseNumber;
        this.transferredFromId = transferredFromId;
        this.transferredFromName = transferredFromName;
        this.transferredToId = transferredToId;
        this.transferredToName = transferredToName;
        this.transferDate = transferDate;
        this.actionType = actionType;
        this.purposeRemarks = purposeRemarks;
        this.integrityVerified = integrityVerified;
    }

    public int getCustodyId() { return custodyId; }
    public void setCustodyId(int custodyId) { this.custodyId = custodyId; }

    public int getEvidenceId() { return evidenceId; }
    public void setEvidenceId(int evidenceId) { this.evidenceId = evidenceId; }

    public String getEvidenceCode() { return evidenceCode; }
    public void setEvidenceCode(String evidenceCode) { this.evidenceCode = evidenceCode; }

    public String getEvidenceName() { return evidenceName; }
    public void setEvidenceName(String evidenceName) { this.evidenceName = evidenceName; }

    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }

    public int getTransferredFromId() { return transferredFromId; }
    public void setTransferredFromId(int transferredFromId) { this.transferredFromId = transferredFromId; }

    public String getTransferredFromName() { return transferredFromName; }
    public void setTransferredFromName(String transferredFromName) { this.transferredFromName = transferredFromName; }

    public int getTransferredToId() { return transferredToId; }
    public void setTransferredToId(int transferredToId) { this.transferredToId = transferredToId; }

    public String getTransferredToName() { return transferredToName; }
    public void setTransferredToName(String transferredToName) { this.transferredToName = transferredToName; }

    public Timestamp getTransferDate() { return transferDate; }
    public void setTransferDate(Timestamp transferDate) { this.transferDate = transferDate; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getPurposeRemarks() { return purposeRemarks; }
    public void setPurposeRemarks(String purposeRemarks) { this.purposeRemarks = purposeRemarks; }

    public boolean isIntegrityVerified() { return integrityVerified; }
    public void setIntegrityVerified(boolean integrityVerified) { this.integrityVerified = integrityVerified; }
}
