package models;

import java.sql.Timestamp;

public class CaseOutcome {
    private int outcomeId;
    private int caseId;
    private String caseNumber;
    private String findingsSummary;
    private String recommendedAction;
    private int finalizedById;
    private String finalizedByName;
    private Timestamp resolutionDate;
    private boolean appealable;

    public CaseOutcome() {}

    public CaseOutcome(int outcomeId, int caseId, String caseNumber, String findingsSummary, 
                       String recommendedAction, int finalizedById, String finalizedByName, 
                       Timestamp resolutionDate, boolean appealable) {
        this.outcomeId = outcomeId;
        this.caseId = caseId;
        this.caseNumber = caseNumber;
        this.findingsSummary = findingsSummary;
        this.recommendedAction = recommendedAction;
        this.finalizedById = finalizedById;
        this.finalizedByName = finalizedByName;
        this.resolutionDate = resolutionDate;
        this.appealable = appealable;
    }

    public int getOutcomeId() { return outcomeId; }
    public void setOutcomeId(int outcomeId) { this.outcomeId = outcomeId; }

    public int getCaseId() { return caseId; }
    public void setCaseId(int caseId) { this.caseId = caseId; }

    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }

    public String getFindingsSummary() { return findingsSummary; }
    public void setFindingsSummary(String findingsSummary) { this.findingsSummary = findingsSummary; }

    public String getRecommendedAction() { return recommendedAction; }
    public void setRecommendedAction(String recommendedAction) { this.recommendedAction = recommendedAction; }

    public int getFinalizedById() { return finalizedById; }
    public void setFinalizedById(int finalizedById) { this.finalizedById = finalizedById; }

    public String getFinalizedByName() { return finalizedByName; }
    public void setFinalizedByName(String finalizedByName) { this.finalizedByName = finalizedByName; }

    public Timestamp getResolutionDate() { return resolutionDate; }
    public void setResolutionDate(Timestamp resolutionDate) { this.resolutionDate = resolutionDate; }

    public boolean isAppealable() { return appealable; }
    public void setAppealable(boolean appealable) { this.appealable = appealable; }
}
