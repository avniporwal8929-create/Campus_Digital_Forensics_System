package ui.incidents;

import dao.IncidentDAO;
import dao.UserDAO;
import models.CaseIncident;
import models.User;
import ui.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class IncidentPanel extends JPanel {
    private final IncidentDAO incidentDAO;
    private final UserDAO userDAO;
    private JTable incidentTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> statusFilterCb;
    private JComboBox<String> severityFilterCb;

    public IncidentPanel() {
        this.incidentDAO = new IncidentDAO();
        this.userDAO = new UserDAO();
        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.BG_PANEL);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
        refreshTable();
    }

    private void initUI() {
        // Top Action Bar
        JPanel topBar = new JPanel(new BorderLayout(14, 0));
        topBar.setOpaque(false);

        JLabel title = new JLabel("MODULE 3: SECURITY INCIDENTS & CASES");
        title.setFont(Theme.FONT_HEADER_LARGE);
        title.setForeground(Theme.TEXT_PRIMARY);

        JPanel actionControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionControls.setOpaque(false);

        searchField = Theme.createStyledTextField();
        searchField.setPreferredSize(new Dimension(180, 34));
        searchField.putClientProperty("JTextField.placeholderText", "Search title or case...");

        statusFilterCb = Theme.createStyledComboBox(new String[]{"ALL", "OPEN", "UNDER_INVESTIGATION", "RESOLVED", "CLOSED"});
        severityFilterCb = Theme.createStyledComboBox(new String[]{"ALL", "CRITICAL", "HIGH", "MEDIUM", "LOW"});

        JButton filterBtn = Theme.createStyledButton("Filter", false);
        filterBtn.addActionListener(e -> applyFilter());

        JButton addBtn = Theme.createStyledButton("➕ Log Incident", true);
        addBtn.addActionListener(e -> openIncidentDialog(null));

        JButton updateStatBtn = Theme.createStyledButton("🔄 Update Status", false);
        updateStatBtn.addActionListener(e -> updateStatusDialog());

        JButton viewBtn = Theme.createStyledButton("👁 Details", false);
        viewBtn.addActionListener(e -> viewDetailsDialog());

        JButton refreshBtn = Theme.createStyledButton("🔄 Refresh", false);
        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            statusFilterCb.setSelectedIndex(0);
            severityFilterCb.setSelectedIndex(0);
            refreshTable();
        });

        actionControls.add(searchField);
        actionControls.add(new JLabel("Status:"));
        actionControls.add(statusFilterCb);
        actionControls.add(new JLabel("Severity:"));
        actionControls.add(severityFilterCb);
        actionControls.add(filterBtn);
        actionControls.add(addBtn);
        actionControls.add(updateStatBtn);
        actionControls.add(viewBtn);
        actionControls.add(refreshBtn);

        topBar.add(title, BorderLayout.WEST);
        topBar.add(actionControls, BorderLayout.EAST);

        // Table Card
        JPanel card = Theme.createCardPanel();
        card.setLayout(new BorderLayout());

        String[] cols = {"Case ID", "Case Number", "Title", "Classification", "Severity", "Status", "Campus Location", "Reported By", "Reported At"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        incidentTable = new JTable(tableModel);
        Theme.styleTable(incidentTable);

        // Custom severity coloring renderer for Severity column
        incidentTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String sev = (value != null) ? value.toString() : "";
                if ("CRITICAL".equalsIgnoreCase(sev)) {
                    setForeground(Theme.STATUS_CRITICAL);
                    setFont(Theme.FONT_BOLD);
                } else if ("HIGH".equalsIgnoreCase(sev)) {
                    setForeground(Theme.STATUS_DANGER);
                    setFont(Theme.FONT_BOLD);
                } else if ("MEDIUM".equalsIgnoreCase(sev)) {
                    setForeground(Theme.STATUS_WARNING);
                } else {
                    setForeground(Theme.STATUS_SUCCESS);
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(incidentTable);
        scrollPane.getViewport().setBackground(Theme.BG_PANEL);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR, 1));
        card.add(scrollPane, BorderLayout.CENTER);

        add(topBar, BorderLayout.NORTH);
        add(card, BorderLayout.CENTER);
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        List<CaseIncident> list = incidentDAO.getAllCases();
        for (CaseIncident c : list) {
            tableModel.addRow(new Object[] {
                c.getCaseId(),
                c.getCaseNumber(),
                c.getTitle(),
                c.getIncidentType(),
                c.getSeverity(),
                c.getStatus(),
                (c.getLocationName() != null) ? c.getLocationName() : "General Campus",
                (c.getReportedByName() != null) ? c.getReportedByName() : "System Alert",
                c.getReportedAt()
            });
        }
    }

    private void applyFilter() {
        String q = searchField.getText().trim();
        String stat = (String) statusFilterCb.getSelectedItem();
        String sev = (String) severityFilterCb.getSelectedItem();

        tableModel.setRowCount(0);
        List<CaseIncident> list = incidentDAO.searchCases(q, stat, sev);
        for (CaseIncident c : list) {
            tableModel.addRow(new Object[] {
                c.getCaseId(),
                c.getCaseNumber(),
                c.getTitle(),
                c.getIncidentType(),
                c.getSeverity(),
                c.getStatus(),
                (c.getLocationName() != null) ? c.getLocationName() : "General Campus",
                (c.getReportedByName() != null) ? c.getReportedByName() : "System Alert",
                c.getReportedAt()
            });
        }
    }

    private void updateStatusDialog() {
        int selected = incidentTable.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Please select an incident from the table to update status.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int caseId = (int) tableModel.getValueAt(selected, 0);
        String caseNum = (String) tableModel.getValueAt(selected, 1);
        String currentStat = (String) tableModel.getValueAt(selected, 5);

        String[] statuses = {"OPEN", "UNDER_INVESTIGATION", "RESOLVED", "CLOSED"};
        String choice = (String) JOptionPane.showInputDialog(
            this,
            "Select new investigation status for " + caseNum + ":",
            "Update Case Status",
            JOptionPane.QUESTION_MESSAGE,
            null,
            statuses,
            currentStat
        );

        if (choice != null && !choice.equalsIgnoreCase(currentStat)) {
            boolean ok = incidentDAO.updateCaseStatus(caseId, choice);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Case status updated to " + choice, "Status Updated", JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update case status.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void viewDetailsDialog() {
        int selected = incidentTable.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Please select an incident to view full details.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int caseId = (int) tableModel.getValueAt(selected, 0);
        CaseIncident c = incidentDAO.getCaseById(caseId);
        if (c == null) return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Incident Dossier: " + c.getCaseNumber(), true);
        dialog.setSize(560, 480);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setBackground(Theme.BG_DARK);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel titleLbl = new JLabel(c.getTitle());
        titleLbl.setFont(Theme.FONT_HEADER_MED);
        titleLbl.setForeground(Theme.ACCENT_CYAN);

        JTextArea descArea = new JTextArea();
        descArea.setFont(Theme.FONT_REGULAR);
        descArea.setBackground(Theme.BG_INPUT);
        descArea.setForeground(Theme.TEXT_PRIMARY);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setText(
            "CASE NUMBER: " + c.getCaseNumber() + "\n" +
            "CLASSIFICATION: " + c.getIncidentType() + "\n" +
            "SEVERITY LEVEL: " + c.getSeverity() + "\n" +
            "CURRENT STATUS: " + c.getStatus() + "\n" +
            "LOCATION: " + ((c.getLocationName() != null) ? c.getLocationName() : "General Campus") + "\n" +
            "REPORTED BY: " + ((c.getReportedByName() != null) ? c.getReportedByName() : "System Alert") + "\n" +
            "REPORTED AT: " + c.getReportedAt() + "\n" +
            "RESOLVED AT: " + ((c.getResolvedAt() != null) ? c.getResolvedAt() : "Active / Ongoing") + "\n\n" +
            "INCIDENT DESCRIPTION & INITIAL FINDINGS:\n" +
            c.getDescription()
        );

        JScrollPane sp = new JScrollPane(descArea);
        sp.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR, 1));

        JButton closeBtn = Theme.createStyledButton("Close", false);
        closeBtn.addActionListener(e -> dialog.dispose());
        JPanel bPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bPanel.setOpaque(false);
        bPanel.add(closeBtn);

        panel.add(titleLbl, BorderLayout.NORTH);
        panel.add(sp, BorderLayout.CENTER);
        panel.add(bPanel, BorderLayout.SOUTH);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void openIncidentDialog(CaseIncident existing) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Register New Security Incident", true);
        dialog.setSize(520, 500);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setBackground(Theme.BG_DARK);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 10));
        form.setOpaque(false);

        String autoCaseNum = "CDF-2026-" + String.format("%03d", incidentDAO.getAllCases().size() + 1);
        JTextField numField = Theme.createStyledTextField();
        numField.setText(autoCaseNum);
        numField.setEditable(false);

        JTextField titleField = Theme.createStyledTextField();
        JComboBox<String> typeCb = Theme.createStyledComboBox(new String[]{
            "UNAUTHORIZED_ACCESS", "DATA_EXFILTRATION", "MALWARE_OUTBREAK", "EXAM_TAMPERING",
            "NETWORK_INTRUSION", "CREDENTIAL_STUFFING", "PHISHING_ATTACK", "POLICY_VIOLATION"
        });
        JComboBox<String> sevCb = Theme.createStyledComboBox(new String[]{"LOW", "MEDIUM", "HIGH", "CRITICAL"});
        sevCb.setSelectedItem("HIGH");

        List<User> users = userDAO.getAllUsers();
        JComboBox<User> repCb = new JComboBox<>(users.toArray(new User[0]));
        repCb.setBackground(Theme.BG_INPUT);
        repCb.setForeground(Theme.TEXT_PRIMARY);

        JComboBox<String> locCb = Theme.createStyledComboBox(new String[]{
            "1 - Turing Tech Center Lab-301",
            "2 - Aryabhata Complex Server Room DC-01",
            "3 - Main Academic Block Exam Cell 105",
            "4 - Ramanujan Library Reading Hall 2",
            "5 - Newton Hostel Block-C",
            "6 - Admin Tower Chambers"
        });

        form.add(createLabel("Case Number:"));
        form.add(numField);
        form.add(createLabel("Incident Title:"));
        form.add(titleField);
        form.add(createLabel("Classification Type:"));
        form.add(typeCb);
        form.add(createLabel("Severity Level:"));
        form.add(sevCb);
        form.add(createLabel("Reported By:"));
        form.add(repCb);
        form.add(createLabel("Incident Location:"));
        form.add(locCb);

        JTextArea descArea = new JTextArea(4, 20);
        descArea.setFont(Theme.FONT_REGULAR);
        descArea.setBackground(Theme.BG_INPUT);
        descArea.setForeground(Theme.TEXT_PRIMARY);
        descArea.setLineWrap(true);
        descArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));

        JPanel centerPanel = new JPanel(new BorderLayout(0, 8));
        centerPanel.setOpaque(false);
        centerPanel.add(form, BorderLayout.NORTH);
        centerPanel.add(createLabel("Detailed Narrative & Indicators of Compromise:"), BorderLayout.CENTER);
        centerPanel.add(new JScrollPane(descArea), BorderLayout.SOUTH);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBar.setOpaque(false);
        JButton saveBtn = Theme.createStyledButton("Log Incident", true);
        JButton cancelBtn = Theme.createStyledButton("Cancel", false);
        cancelBtn.addActionListener(e -> dialog.dispose());

        saveBtn.addActionListener(e -> {
            String titleStr = titleField.getText().trim();
            String descStr = descArea.getText().trim();
            if (titleStr.isEmpty() || descStr.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please specify an incident title and narrative description.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            CaseIncident ni = new CaseIncident();
            ni.setCaseNumber(numField.getText().trim());
            ni.setTitle(titleStr);
            ni.setIncidentType((String) typeCb.getSelectedItem());
            ni.setSeverity((String) sevCb.getSelectedItem());
            ni.setStatus("OPEN");
            ni.setDescription(descStr);

            User reporter = (User) repCb.getSelectedItem();
            if (reporter != null) {
                ni.setReportedByUserId(reporter.getUserId());
                ni.setReportedByName(reporter.getFullName());
            }

            int locId = locCb.getSelectedIndex() + 1;
            ni.setLocationId(locId);

            boolean ok = incidentDAO.addCase(ni);
            if (ok) {
                dialog.dispose();
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to register incident case.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnBar.add(saveBtn);
        btnBar.add(cancelBtn);

        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(btnBar, BorderLayout.SOUTH);
        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_BOLD);
        l.setForeground(Theme.TEXT_SECONDARY);
        return l;
    }
}
