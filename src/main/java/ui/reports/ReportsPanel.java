package ui.reports;

import dao.ReportDAO;
import models.AnalyticsSummary;
import ui.Theme;
import ui.components.SimpleChartPanel;
import ui.components.StatCard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;

public class ReportsPanel extends JPanel {
    private final ReportDAO reportDAO;

    private JPanel statsGrid;
    private SimpleChartPanel severityChart;
    private SimpleChartPanel statusChart;
    private SimpleChartPanel evidenceChart;
    private SimpleChartPanel workloadChart;
    private JTable monthlyTable;
    private DefaultTableModel monthlyModel;

    public ReportsPanel() {
        this.reportDAO = new ReportDAO();

        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.BG_PANEL);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
        refreshReports();
    }

    private void initUI() {
        // Top Action Bar
        JPanel topBar = new JPanel(new BorderLayout(14, 0));
        topBar.setOpaque(false);

        JLabel title = new JLabel("MODULE 8: FORENSIC REPORTS & STATISTICAL ANALYTICS");
        title.setFont(Theme.FONT_HEADER_LARGE);
        title.setForeground(Theme.TEXT_PRIMARY);

        JPanel actionControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionControls.setOpaque(false);

        JButton refreshBtn = Theme.createStyledButton("🔄 Refresh Analytics", true);
        refreshBtn.addActionListener(e -> refreshReports());

        actionControls.add(refreshBtn);
        topBar.add(title, BorderLayout.WEST);
        topBar.add(actionControls, BorderLayout.EAST);

        // Center Content Scroll
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        // Stats Grid
        statsGrid = new JPanel(new GridLayout(1, 5, 12, 0));
        statsGrid.setOpaque(false);

        // Visual Charts Section (2x2 Grid)
        JPanel chartsGrid = new JPanel(new GridLayout(2, 2, 14, 14));
        chartsGrid.setOpaque(false);
        chartsGrid.setPreferredSize(new Dimension(800, 480));

        severityChart = new SimpleChartPanel("INCIDENT SEVERITY BREAKDOWN", null, new Color[]{
            Theme.STATUS_CRITICAL, Theme.STATUS_DANGER, Theme.STATUS_WARNING, Theme.STATUS_SUCCESS
        });
        statusChart = new SimpleChartPanel("INVESTIGATION CASE STATUS", null, new Color[]{
            Theme.ACCENT_CYAN, Theme.ACCENT_BLUE, Theme.STATUS_SUCCESS, Theme.TEXT_MUTED
        });
        evidenceChart = new SimpleChartPanel("EVIDENCE BY ARTIFACT TYPE", null, new Color[]{
            Theme.ACCENT_CYAN, new Color(168, 85, 247), Theme.STATUS_WARNING, Theme.STATUS_SUCCESS, Theme.TEXT_PRIMARY
        });
        workloadChart = new SimpleChartPanel("INVESTIGATOR ACTIVE CASELOAD", null, new Color[]{
            Theme.ACCENT_BLUE, Theme.ACCENT_CYAN, new Color(56, 189, 248), new Color(129, 140, 248)
        });

        chartsGrid.add(severityChart);
        chartsGrid.add(statusChart);
        chartsGrid.add(evidenceChart);
        chartsGrid.add(workloadChart);

        // DBMS Concepts Aggregate Table
        JPanel tableCard = Theme.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 10));

        JLabel tableTitle = new JLabel("SQL AGGREGATION: MONTHLY INCIDENT TIME-SERIES (GROUP BY DATE)");
        tableTitle.setFont(Theme.FONT_TITLE);
        tableTitle.setForeground(Theme.ACCENT_CYAN);

        String[] cols = {"Month Timeframe", "Total Reported Incidents", "Percentage of Annual Volume", "Aggregated Query Expression"};
        monthlyModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        monthlyTable = new JTable(monthlyModel);
        Theme.styleTable(monthlyTable);

        JScrollPane sp = new JScrollPane(monthlyTable);
        sp.getViewport().setBackground(Theme.BG_PANEL);
        sp.setPreferredSize(new Dimension(800, 160));
        sp.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR, 1));

        tableCard.add(tableTitle, BorderLayout.NORTH);
        tableCard.add(sp, BorderLayout.CENTER);

        centerPanel.add(statsGrid);
        centerPanel.add(Box.createVerticalStrut(16));
        centerPanel.add(chartsGrid);
        centerPanel.add(Box.createVerticalStrut(16));
        centerPanel.add(tableCard);

        JScrollPane mainScroll = new JScrollPane(centerPanel);
        mainScroll.setBorder(null);
        mainScroll.setOpaque(false);
        mainScroll.getViewport().setOpaque(false);

        add(topBar, BorderLayout.NORTH);
        add(mainScroll, BorderLayout.CENTER);
    }

    public void refreshReports() {
        AnalyticsSummary s = reportDAO.getAnalyticsSummary();

        statsGrid.removeAll();
        statsGrid.add(new StatCard("CAMPUS USERS", String.valueOf(s.getTotalUsers()), "👥", Theme.ACCENT_CYAN, "Total Registered"));
        statsGrid.add(new StatCard("ACTIVE ASSETS", String.valueOf(s.getTotalDevices()), "💻", Theme.TEXT_PRIMARY, "Inventory Devices"));
        statsGrid.add(new StatCard("SECURITY CASES", String.valueOf(s.getTotalIncidents()), "🚨", Theme.STATUS_WARNING, "Logged Incidents"));
        statsGrid.add(new StatCard("OPEN & TRIAGE", s.getOpenIncidents() + " / " + s.getCriticalIncidents(), "⚡", Theme.STATUS_CRITICAL, "Critical Priority"));
        statsGrid.add(new StatCard("CUSTODY ITEMS", String.valueOf(s.getTotalEvidence()), "🔬", Theme.STATUS_SUCCESS, "Vault Evidence"));
        statsGrid.revalidate();
        statsGrid.repaint();

        severityChart.updateData(s.getSeverityDistribution());
        statusChart.updateData(s.getStatusDistribution());
        evidenceChart.updateData(s.getEvidenceByType());
        workloadChart.updateData(s.getInvestigatorWorkload());

        // Update monthly table
        monthlyModel.setRowCount(0);
        int total = s.getTotalIncidents();
        for (Map.Entry<String, Integer> entry : s.getMonthlyIncidents().entrySet()) {
            double pct = (total > 0) ? ((double) entry.getValue() / total * 100.0) : 0;
            monthlyModel.addRow(new Object[] {
                entry.getKey(),
                entry.getValue() + " incident(s)",
                String.format("%.1f %%", pct),
                "SELECT DATE_FORMAT(reported_at, '%b %Y'), COUNT(*) FROM cases GROUP BY ..."
            });
        }
    }
}
