package models;

import java.sql.Timestamp;

public class InvestigationActivity {
    private int activityId;
    private int caseId;
    private String caseNumber;
    private int investigatorId;
    private String investigatorName;
    private String activityType;
    private String activitySummary;
    private String detailedNotes;
    private Timestamp activityTimestamp;

    public InvestigationActivity() {}

    public InvestigationActivity(int activityId, int caseId, String caseNumber, 
                                 int investigatorId, String investigatorName, 
                                 String activityType, String activitySummary, 
                                 String detailedNotes, Timestamp activityTimestamp) {
        this.activityId = activityId;
        this.caseId = caseId;
        this.caseNumber = caseNumber;
        this.investigatorId = investigatorId;
        this.investigatorName = investigatorName;
        this.activityType = activityType;
        this.activitySummary = activitySummary;
        this.detailedNotes = detailedNotes;
        this.activityTimestamp = activityTimestamp;
    }

    public int getActivityId() { return activityId; }
    public void setActivityId(int activityId) { this.activityId = activityId; }

    public int getCaseId() { return caseId; }
    public void setCaseId(int caseId) { this.caseId = caseId; }

    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }

    public int getInvestigatorId() { return investigatorId; }
    public void setInvestigatorId(int investigatorId) { this.investigatorId = investigatorId; }

    public String getInvestigatorName() { return investigatorName; }
    public void setInvestigatorName(String investigatorName) { this.investigatorName = investigatorName; }

    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }

    public String getActivitySummary() { return activitySummary; }
    public void setActivitySummary(String activitySummary) { this.activitySummary = activitySummary; }

    public String getDetailedNotes() { return detailedNotes; }
    public void setDetailedNotes(String detailedNotes) { this.detailedNotes = detailedNotes; }

    public Timestamp getActivityTimestamp() { return activityTimestamp; }
    public void setActivityTimestamp(Timestamp activityTimestamp) { this.activityTimestamp = activityTimestamp; }
}
