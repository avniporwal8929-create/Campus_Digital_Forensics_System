package ui;

import services.AuthService;
import ui.components.DbConfigDialog;
import ui.components.HeaderPanel;
import ui.components.SidebarPanel;
import ui.correlation.CorrelationPanel;
import ui.custody.CustodyPanel;
import ui.devices.DevicePanel;
import ui.events.EventLogPanel;
import ui.evidence.EvidencePanel;
import ui.incidents.IncidentPanel;
import ui.investigators.InvestigatorPanel;
import ui.reports.ReportsPanel;
import ui.users.UserPanel;

import javax.swing.*;
import java.awt.*;

public class DashboardFrame extends JFrame {
    private final CardLayout cardLayout;
    private final JPanel contentCards;
    private final HeaderPanel headerPanel;
    private final SidebarPanel sidebarPanel;

    private final DashboardPanel dashboardPanel;
    private final UserPanel userPanel;
    private final DevicePanel devicePanel;
    private final IncidentPanel incidentPanel;
    private final EventLogPanel eventLogPanel;
    private final EvidencePanel evidencePanel;
    private final CustodyPanel custodyPanel;
    private final InvestigatorPanel investigatorPanel;
    private final ReportsPanel reportsPanel;
    private final CorrelationPanel correlationPanel;

    public DashboardFrame() {
        super("Campus Digital Forensics System (CDF) - Enterprise Console");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1360, 860);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(Theme.BG_DARK);

        // Header
        headerPanel = new HeaderPanel(this::handleLogout, this::handleOpenDbConfig);
        rootPanel.add(headerPanel, BorderLayout.NORTH);

        // Sidebar
        sidebarPanel = new SidebarPanel(this::showModule);
        rootPanel.add(sidebarPanel, BorderLayout.WEST);

        // Central Content Card Panel
        cardLayout = new CardLayout();
        contentCards = new JPanel(cardLayout);
        contentCards.setBackground(Theme.BG_PANEL);

        // Instantiate all 9 modules + dashboard
        dashboardPanel = new DashboardPanel(this::showModule);
        userPanel = new UserPanel();
        devicePanel = new DevicePanel();
        incidentPanel = new IncidentPanel();
        eventLogPanel = new EventLogPanel();
        evidencePanel = new EvidencePanel();
        custodyPanel = new CustodyPanel();
        investigatorPanel = new InvestigatorPanel();
        reportsPanel = new ReportsPanel();
        correlationPanel = new CorrelationPanel();

        contentCards.add(dashboardPanel, "DASHBOARD");
        contentCards.add(userPanel, "USERS");
        contentCards.add(devicePanel, "DEVICES");
        contentCards.add(incidentPanel, "INCIDENTS");
        contentCards.add(eventLogPanel, "LOGS");
        contentCards.add(evidencePanel, "EVIDENCE");
        contentCards.add(custodyPanel, "CUSTODY");
        contentCards.add(investigatorPanel, "INVESTIGATORS");
        contentCards.add(reportsPanel, "REPORTS");
        contentCards.add(correlationPanel, "CORRELATION");

        rootPanel.add(contentCards, BorderLayout.CENTER);
        setContentPane(rootPanel);
    }

    public void showModule(String moduleKey) {
        sidebarPanel.setActiveButton(moduleKey);
        cardLayout.show(contentCards, moduleKey);

        // Auto-refresh target module when opened
        switch (moduleKey) {
            case "DASHBOARD" -> dashboardPanel.refreshData();
            case "USERS" -> userPanel.refreshTable();
            case "DEVICES" -> devicePanel.refreshTable();
            case "INCIDENTS" -> incidentPanel.refreshTable();
            case "LOGS" -> eventLogPanel.refreshLogs();
            case "EVIDENCE" -> evidencePanel.refreshTable();
            case "CUSTODY" -> custodyPanel.refreshTable();
            case "INVESTIGATORS" -> investigatorPanel.refreshAll();
            case "REPORTS" -> reportsPanel.refreshReports();
        }
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to end your digital forensics session and log out?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            AuthService.getInstance().logout();
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        }
    }

    private void handleOpenDbConfig() {
        new DbConfigDialog(this, () -> {
            headerPanel.updateDbStatus();
            dashboardPanel.refreshData();
        }).setVisible(true);
    }
}
