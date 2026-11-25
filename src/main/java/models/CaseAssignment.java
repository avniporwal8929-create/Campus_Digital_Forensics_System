package models;

import java.sql.Timestamp;

public class CaseAssignment {
    private int assignmentId;
    private int caseId;
    private String caseNumber;
    private String caseTitle;
    private int investigatorId;
    private String investigatorName;
    private String investigatorEmail;
    private Integer assignedBy;
    private String assignedByName;
    private Timestamp assignedDate;
    private String assignmentStatus;
    private boolean leadInvestigator;
    private String notes;

    public CaseAssignment() {}

    public CaseAssignment(int assignmentId, int caseId, String caseNumber, String caseTitle, 
                          int investigatorId, String investigatorName, String investigatorEmail, 
                          Integer assignedBy, String assignedByName, Timestamp assignedDate, 
                          String assignmentStatus, boolean leadInvestigator, String notes) {
        this.assignmentId = assignmentId;
        this.caseId = caseId;
        this.caseNumber = caseNumber;
        this.caseTitle = caseTitle;
        this.investigatorId = investigatorId;
        this.investigatorName = investigatorName;
        this.investigatorEmail = investigatorEmail;
        this.assignedBy = assignedBy;
        this.assignedByName = assignedByName;
        this.assignedDate = assignedDate;
        this.assignmentStatus = assignmentStatus;
        this.leadInvestigator = leadInvestigator;
        this.notes = notes;
    }

    public int getAssignmentId() { return assignmentId; }
    public void setAssignmentId(int assignmentId) { this.assignmentId = assignmentId; }

    public int getCaseId() { return caseId; }
    public void setCaseId(int caseId) { this.caseId = caseId; }

    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }

    public String getCaseTitle() { return caseTitle; }
    public void setCaseTitle(String caseTitle) { this.caseTitle = caseTitle; }

    public int getInvestigatorId() { return investigatorId; }
    public void setInvestigatorId(int investigatorId) { this.investigatorId = investigatorId; }

    public String getInvestigatorName() { return investigatorName; }
    public void setInvestigatorName(String investigatorName) { this.investigatorName = investigatorName; }

    public String getInvestigatorEmail() { return investigatorEmail; }
    public void setInvestigatorEmail(String investigatorEmail) { this.investigatorEmail = investigatorEmail; }

    public Integer getAssignedBy() { return assignedBy; }
    public void setAssignedBy(Integer assignedBy) { this.assignedBy = assignedBy; }

    public String getAssignedByName() { return assignedByName; }
    public void setAssignedByName(String assignedByName) { this.assignedByName = assignedByName; }

    public Timestamp getAssignedDate() { return assignedDate; }
    public void setAssignedDate(Timestamp assignedDate) { this.assignedDate = assignedDate; }

    public String getAssignmentStatus() { return assignmentStatus; }
    public void setAssignmentStatus(String assignmentStatus) { this.assignmentStatus = assignmentStatus; }

    public boolean isLeadInvestigator() { return leadInvestigator; }
    public void setLeadInvestigator(boolean leadInvestigator) { this.leadInvestigator = leadInvestigator; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
