package ui;

import dao.IncidentDAO;
import dao.ReportDAO;
import models.AnalyticsSummary;
import models.CaseIncident;
import ui.components.SimpleChartPanel;
import ui.components.StatCard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class DashboardPanel extends JPanel {
    private final ReportDAO reportDAO;
    private final IncidentDAO incidentDAO;
    private final Consumer<String> onNavigate;

    private JPanel statsContainer;
    private SimpleChartPanel severityChart;
    private SimpleChartPanel statusChart;
    private JTable recentIncidentsTable;
    private DefaultTableModel tableModel;

    public DashboardPanel(Consumer<String> onNavigate) {
        this.reportDAO = new ReportDAO();
        this.incidentDAO = new IncidentDAO();
        this.onNavigate = onNavigate;

        setLayout(new BorderLayout(0, 16));
        setBackground(Theme.BG_PANEL);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
        refreshData();
    }

    private void initUI() {
        // Top Header
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setOpaque(false);

        JLabel title = new JLabel("SECURITY OPERATIONS & FORENSICS DASHBOARD");
        title.setFont(Theme.FONT_HEADER_LARGE);
        title.setForeground(Theme.TEXT_PRIMARY);

        JButton refreshBtn = Theme.createStyledButton("🔄 Refresh Telemetry", false);
        refreshBtn.addActionListener(e -> refreshData());

        topHeader.add(title, BorderLayout.WEST);
        topHeader.add(refreshBtn, BorderLayout.EAST);

        // Stats Cards Grid
        statsContainer = new JPanel(new GridLayout(1, 5, 14, 0));
        statsContainer.setOpaque(false);

        // Charts Section
        JPanel chartsPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        chartsPanel.setOpaque(false);
        severityChart = new SimpleChartPanel("INCIDENT SEVERITY BREAKDOWN", null, new Color[] {
            Theme.STATUS_CRITICAL, Theme.STATUS_DANGER, Theme.STATUS_WARNING, Theme.STATUS_SUCCESS
        });
        statusChart = new SimpleChartPanel("INVESTIGATION STATUS DISTRIBUTION", null, new Color[] {
            Theme.ACCENT_CYAN, Theme.ACCENT_BLUE, Theme.STATUS_SUCCESS, Theme.TEXT_MUTED
        });
        chartsPanel.add(severityChart);
        chartsPanel.add(statusChart);

        // Lower Section: Recent Active Incidents Table
        JPanel tableCard = Theme.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 12));

        JPanel tableHead = new JPanel(new BorderLayout());
        tableHead.setOpaque(false);

        JLabel tableTitle = new JLabel("ACTIVE DIGITAL SECURITY INCIDENTS");
        tableTitle.setFont(Theme.FONT_TITLE);
        tableTitle.setForeground(Theme.ACCENT_CYAN);

        JButton viewAllBtn = Theme.createStyledButton("Open All Incidents →", true);
        viewAllBtn.addActionListener(e -> onNavigate.accept("INCIDENTS"));

        tableHead.add(tableTitle, BorderLayout.WEST);
        tableHead.add(viewAllBtn, BorderLayout.EAST);

        String[] cols = {"Case #", "Incident Title", "Classification", "Severity", "Status", "Location", "Reported Time"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        recentIncidentsTable = new JTable(tableModel);
        Theme.styleTable(recentIncidentsTable);

        JScrollPane scrollPane = new JScrollPane(recentIncidentsTable);
        scrollPane.getViewport().setBackground(Theme.BG_PANEL);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR, 1));

        tableCard.add(tableHead, BorderLayout.NORTH);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // Assemble Dashboard
        JPanel centerContent = new JPanel();
        centerContent.setLayout(new BoxLayout(centerContent, BoxLayout.Y_AXIS));
        centerContent.setOpaque(false);

        centerContent.add(statsContainer);
        centerContent.add(Box.createVerticalStrut(16));
        centerContent.add(chartsPanel);
        centerContent.add(Box.createVerticalStrut(16));
        centerContent.add(tableCard);

        JScrollPane mainScroll = new JScrollPane(centerContent);
        mainScroll.setBorder(null);
        mainScroll.setOpaque(false);
        mainScroll.getViewport().setOpaque(false);

        add(topHeader, BorderLayout.NORTH);
        add(mainScroll, BorderLayout.CENTER);
    }

    public void refreshData() {
        AnalyticsSummary s = reportDAO.getAnalyticsSummary();

        statsContainer.removeAll();
        statsContainer.add(new StatCard("REGISTERED USERS", String.valueOf(s.getTotalUsers()), "👥", Theme.ACCENT_CYAN, "Campus Accounts"));
        statsContainer.add(new StatCard("MANAGED DEVICES", String.valueOf(s.getTotalDevices()), "💻", Theme.TEXT_PRIMARY, "Hardware Assets"));
        statsContainer.add(new StatCard("TOTAL INCIDENTS", String.valueOf(s.getTotalIncidents()), "🚨", Theme.STATUS_WARNING, "Logged Events"));
        statsContainer.add(new StatCard("OPEN & CRITICAL", s.getOpenIncidents() + " / " + s.getCriticalIncidents(), "⚡", Theme.STATUS_CRITICAL, "Active Triage"));
        statsContainer.add(new StatCard("EVIDENCE ITEMS", String.valueOf(s.getTotalEvidence()), "🔬", Theme.STATUS_SUCCESS, "Seized Artifacts"));
        statsContainer.revalidate();
        statsContainer.repaint();

        severityChart.updateData(s.getSeverityDistribution());
        statusChart.updateData(s.getStatusDistribution());

        // Refresh Recent Table
        tableModel.setRowCount(0);
        List<CaseIncident> cases = incidentDAO.getAllCases();
        for (CaseIncident c : cases) {
            tableModel.addRow(new Object[] {
                c.getCaseNumber(),
                c.getTitle(),
                c.getIncidentType(),
                c.getSeverity(),
                c.getStatus(),
                (c.getLocationName() != null) ? c.getLocationName() : "Campus Wide",
                c.getReportedAt()
            });
        }
    }
}
