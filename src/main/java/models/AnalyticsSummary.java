package models;

import java.util.Map;
import java.util.LinkedHashMap;

public class AnalyticsSummary {
    private int totalUsers;
    private int totalDevices;
    private int totalIncidents;
    private int openIncidents;
    private int criticalIncidents;
    private int totalEvidence;
    private int activeInvestigations;

    private Map<String, Integer> severityDistribution = new LinkedHashMap<>();
    private Map<String, Integer> statusDistribution = new LinkedHashMap<>();
    private Map<String, Integer> monthlyIncidents = new LinkedHashMap<>();
    private Map<String, Integer> evidenceByType = new LinkedHashMap<>();
    private Map<String, Integer> investigatorWorkload = new LinkedHashMap<>();

    public AnalyticsSummary() {}

    public int getTotalUsers() { return totalUsers; }
    public void setTotalUsers(int totalUsers) { this.totalUsers = totalUsers; }

    public int getTotalDevices() { return totalDevices; }
    public void setTotalDevices(int totalDevices) { this.totalDevices = totalDevices; }

    public int getTotalIncidents() { return totalIncidents; }
    public void setTotalIncidents(int totalIncidents) { this.totalIncidents = totalIncidents; }

    public int getOpenIncidents() { return openIncidents; }
    public void setOpenIncidents(int openIncidents) { this.openIncidents = openIncidents; }

    public int getCriticalIncidents() { return criticalIncidents; }
    public void setCriticalIncidents(int criticalIncidents) { this.criticalIncidents = criticalIncidents; }

    public int getTotalEvidence() { return totalEvidence; }
    public void setTotalEvidence(int totalEvidence) { this.totalEvidence = totalEvidence; }

    public int getActiveInvestigations() { return activeInvestigations; }
    public void setActiveInvestigations(int activeInvestigations) { this.activeInvestigations = activeInvestigations; }

    public Map<String, Integer> getSeverityDistribution() { return severityDistribution; }
    public void setSeverityDistribution(Map<String, Integer> severityDistribution) { this.severityDistribution = severityDistribution; }

    public Map<String, Integer> getStatusDistribution() { return statusDistribution; }
    public void setStatusDistribution(Map<String, Integer> statusDistribution) { this.statusDistribution = statusDistribution; }

    public Map<String, Integer> getMonthlyIncidents() { return monthlyIncidents; }
    public void setMonthlyIncidents(Map<String, Integer> monthlyIncidents) { this.monthlyIncidents = monthlyIncidents; }

    public Map<String, Integer> getEvidenceByType() { return evidenceByType; }
    public void setEvidenceByType(Map<String, Integer> evidenceByType) { this.evidenceByType = evidenceByType; }

    public Map<String, Integer> getInvestigatorWorkload() { return investigatorWorkload; }
    public void setInvestigatorWorkload(Map<String, Integer> investigatorWorkload) { this.investigatorWorkload = investigatorWorkload; }
}
