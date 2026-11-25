package ui.investigators;

import dao.IncidentDAO;
import dao.InvestigatorDAO;
import dao.UserDAO;
import models.CaseAssignment;
import models.CaseIncident;
import models.InvestigationActivity;
import models.User;
import ui.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class InvestigatorPanel extends JPanel {
    private final InvestigatorDAO investigatorDAO;
    private final UserDAO userDAO;
    private final IncidentDAO incidentDAO;

    private JTable assignmentTable;
    private DefaultTableModel assignmentModel;
    private JTable activityTable;
    private DefaultTableModel activityModel;

    public InvestigatorPanel() {
        this.investigatorDAO = new InvestigatorDAO();
        this.userDAO = new UserDAO();
        this.incidentDAO = new IncidentDAO();

        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.BG_PANEL);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
        refreshAll();
    }

    private void initUI() {
        // Top Action Bar
        JPanel topBar = new JPanel(new BorderLayout(14, 0));
        topBar.setOpaque(false);

        JLabel title = new JLabel("MODULE 7: INVESTIGATOR CASE ASSIGNMENTS & ACTIVITIES");
        title.setFont(Theme.FONT_HEADER_LARGE);
        title.setForeground(Theme.TEXT_PRIMARY);

        JPanel actionControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionControls.setOpaque(false);

        JButton assignBtn = Theme.createStyledButton("➕ Assign Case", true);
        assignBtn.addActionListener(e -> openAssignDialog());

        JButton updateStatBtn = Theme.createStyledButton("🔄 Update Status", false);
        updateStatBtn.addActionListener(e -> updateStatusDialog());

        JButton logActBtn = Theme.createStyledButton("📝 Log Activity / Notes", false);
        logActBtn.addActionListener(e -> openActivityDialog());

        JButton refreshBtn = Theme.createStyledButton("🔄 Refresh", false);
        refreshBtn.addActionListener(e -> refreshAll());

        actionControls.add(assignBtn);
        actionControls.add(updateStatBtn);
        actionControls.add(logActBtn);
        actionControls.add(refreshBtn);

        topBar.add(title, BorderLayout.WEST);
        topBar.add(actionControls, BorderLayout.EAST);

        // Tabbed View: Case Assignments vs Field Investigation Logs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(Theme.BG_CARD);
        tabbedPane.setForeground(Theme.TEXT_PRIMARY);
        tabbedPane.setFont(Theme.FONT_BOLD);

        // Tab 1: Assignments
        JPanel assignCard = Theme.createCardPanel();
        assignCard.setLayout(new BorderLayout());

        String[] assignCols = {"Assignment ID", "Case Number", "Case Title", "Assigned Investigator", "Email", "Assigned By", "Status", "Lead", "Assigned Date", "Notes"};
        assignmentModel = new DefaultTableModel(assignCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        assignmentTable = new JTable(assignmentModel);
        Theme.styleTable(assignmentTable);

        assignmentTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String val = (value != null) ? value.toString() : "";
                if ("IN_PROGRESS".equalsIgnoreCase(val)) {
                    setForeground(Theme.ACCENT_CYAN);
                    setFont(Theme.FONT_BOLD);
                } else if ("COMPLETED".equalsIgnoreCase(val)) {
                    setForeground(Theme.STATUS_SUCCESS);
                } else {
                    setForeground(Theme.STATUS_WARNING);
                }
                return c;
            }
        });

        JScrollPane sp1 = new JScrollPane(assignmentTable);
        sp1.getViewport().setBackground(Theme.BG_PANEL);
        sp1.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR, 1));
        assignCard.add(sp1, BorderLayout.CENTER);

        // Tab 2: Activity Logs
        JPanel actCard = Theme.createCardPanel();
        actCard.setLayout(new BorderLayout());

        String[] actCols = {"Activity ID", "Case Number", "Investigator", "Activity Classification", "Summary", "Detailed Forensic Findings", "Timestamp"};
        activityModel = new DefaultTableModel(actCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        activityTable = new JTable(activityModel);
        Theme.styleTable(activityTable);

        JScrollPane sp2 = new JScrollPane(activityTable);
        sp2.getViewport().setBackground(Theme.BG_PANEL);
        sp2.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR, 1));
        actCard.add(sp2, BorderLayout.CENTER);

        tabbedPane.addTab("📋 Active Case Assignments & Lead Roles", assignCard);
        tabbedPane.addTab("🔬 Forensic Investigation Activity & Findings Log", actCard);

        add(topBar, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }

    public void refreshAll() {
        assignmentModel.setRowCount(0);
        List<CaseAssignment> assignments = investigatorDAO.getAllAssignments();
        for (CaseAssignment ca : assignments) {
            assignmentModel.addRow(new Object[] {
                ca.getAssignmentId(),
                ca.getCaseNumber(),
                ca.getCaseTitle(),
                ca.getInvestigatorName(),
                ca.getInvestigatorEmail(),
                (ca.getAssignedByName() != null) ? ca.getAssignedByName() : "Administrator",
                ca.getAssignmentStatus(),
                ca.isLeadInvestigator() ? "⭐ LEAD" : "ANALYST",
                ca.getAssignedDate(),
                (ca.getNotes() != null) ? ca.getNotes() : ""
            });
        }

        activityModel.setRowCount(0);
        List<InvestigationActivity> acts = investigatorDAO.getAllActivities();
        for (InvestigationActivity a : acts) {
            activityModel.addRow(new Object[] {
                a.getActivityId(),
                a.getCaseNumber(),
                a.getInvestigatorName(),
                a.getActivityType(),
                a.getActivitySummary(),
                a.getDetailedNotes(),
                a.getActivityTimestamp()
            });
        }
    }

    private void updateStatusDialog() {
        int selected = assignmentTable.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Please select a case assignment to update status.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int assignId = (int) assignmentModel.getValueAt(selected, 0);
        String currentStat = (String) assignmentModel.getValueAt(selected, 6);

        String[] statuses = {"ASSIGNED", "IN_PROGRESS", "COMPLETED", "REASSIGNED"};
        String choice = (String) JOptionPane.showInputDialog(
            this,
            "Select new assignment status:",
            "Update Assignment Status",
            JOptionPane.QUESTION_MESSAGE,
            null,
            statuses,
            currentStat
        );

        if (choice != null && !choice.equalsIgnoreCase(currentStat)) {
            boolean ok = investigatorDAO.updateAssignmentStatus(assignId, choice);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Assignment status updated to " + choice, "Status Updated", JOptionPane.INFORMATION_MESSAGE);
                refreshAll();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update assignment status.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openAssignDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Assign Investigator to Incident Case", true);
        dialog.setSize(520, 440);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setBackground(Theme.BG_DARK);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel form = new JPanel(new GridLayout(5, 2, 10, 10));
        form.setOpaque(false);

        List<CaseIncident> cases = incidentDAO.getAllCases();
        JComboBox<CaseIncident> caseCb = new JComboBox<>(cases.toArray(new CaseIncident[0]));
        caseCb.setBackground(Theme.BG_INPUT);
        caseCb.setForeground(Theme.TEXT_PRIMARY);

        List<User> investigators = userDAO.getInvestigators();
        JComboBox<User> invCb = new JComboBox<>(investigators.toArray(new User[0]));
        invCb.setBackground(Theme.BG_INPUT);
        invCb.setForeground(Theme.TEXT_PRIMARY);

        JCheckBox leadCheck = new JCheckBox("Lead Forensic Investigator");
        leadCheck.setOpaque(false);
        leadCheck.setForeground(Theme.TEXT_PRIMARY);
        leadCheck.setFont(Theme.FONT_BOLD);
        leadCheck.setSelected(true);

        JComboBox<String> statusCb = Theme.createStyledComboBox(new String[]{"ASSIGNED", "IN_PROGRESS", "COMPLETED"});
        JTextField notesField = Theme.createStyledTextField();
        notesField.setText("Lead forensic analysis and memory acquisition");

        form.add(createLabel("Select Case:"));
        form.add(caseCb);
        form.add(createLabel("Assign Investigator:"));
        form.add(invCb);
        form.add(createLabel("Lead Authority:"));
        form.add(leadCheck);
        form.add(createLabel("Initial Status:"));
        form.add(statusCb);
        form.add(createLabel("Directive / Assignment Notes:"));
        form.add(notesField);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBar.setOpaque(false);
        JButton saveBtn = Theme.createStyledButton("Confirm Assignment", true);
        JButton cancelBtn = Theme.createStyledButton("Cancel", false);
        cancelBtn.addActionListener(e -> dialog.dispose());

        saveBtn.addActionListener(e -> {
            CaseIncident selCase = (CaseIncident) caseCb.getSelectedItem();
            User selInv = (User) invCb.getSelectedItem();
            if (selCase == null || selInv == null) {
                JOptionPane.showMessageDialog(dialog, "Select a valid case and investigator.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            CaseAssignment ca = new CaseAssignment();
            ca.setCaseId(selCase.getCaseId());
            ca.setCaseNumber(selCase.getCaseNumber());
            ca.setCaseTitle(selCase.getTitle());
            ca.setInvestigatorId(selInv.getUserId());
            ca.setInvestigatorName(selInv.getFullName());
            ca.setInvestigatorEmail(selInv.getEmail());
            ca.setAssignmentStatus((String) statusCb.getSelectedItem());
            ca.setLeadInvestigator(leadCheck.isSelected());
            ca.setNotes(notesField.getText().trim());

            boolean ok = investigatorDAO.assignInvestigator(ca);
            if (ok) {
                // Also update case status to UNDER_INVESTIGATION if currently OPEN
                if ("OPEN".equalsIgnoreCase(selCase.getStatus())) {
                    incidentDAO.updateCaseStatus(selCase.getCaseId(), "UNDER_INVESTIGATION");
                }
                JOptionPane.showMessageDialog(dialog, "Investigator assigned to case successfully.", "Assignment Created", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                refreshAll();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to create assignment.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnBar.add(saveBtn);
        btnBar.add(cancelBtn);

        panel.add(form, BorderLayout.CENTER);
        panel.add(btnBar, BorderLayout.SOUTH);
        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void openActivityDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Log Forensic Investigation Activity", true);
        dialog.setSize(520, 460);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setBackground(Theme.BG_DARK);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel form = new JPanel(new GridLayout(4, 2, 10, 10));
        form.setOpaque(false);

        List<CaseIncident> cases = incidentDAO.getAllCases();
        JComboBox<CaseIncident> caseCb = new JComboBox<>(cases.toArray(new CaseIncident[0]));
        caseCb.setBackground(Theme.BG_INPUT);
        caseCb.setForeground(Theme.TEXT_PRIMARY);

        List<User> investigators = userDAO.getInvestigators();
        JComboBox<User> invCb = new JComboBox<>(investigators.toArray(new User[0]));
        invCb.setBackground(Theme.BG_INPUT);
        invCb.setForeground(Theme.TEXT_PRIMARY);

        JComboBox<String> actTypeCb = Theme.createStyledComboBox(new String[]{
            "EVIDENCE_SEIZURE", "HEX_ANALYSIS", "TIMELINE_RECONSTRUCTION", "INTERVIEW", "LOG_CORRELATION", "REPORT_DRAFTING", "PEER_REVIEW"
        });

        JTextField summaryField = Theme.createStyledTextField();
        summaryField.setText("Memory string extraction and malicious DLL detection");

        form.add(createLabel("Case:"));
        form.add(caseCb);
        form.add(createLabel("Investigator:"));
        form.add(invCb);
        form.add(createLabel("Activity Protocol:"));
        form.add(actTypeCb);
        form.add(createLabel("Activity Headline:"));
        form.add(summaryField);

        JTextArea notesArea = new JTextArea(4, 20);
        notesArea.setFont(Theme.FONT_REGULAR);
        notesArea.setBackground(Theme.BG_INPUT);
        notesArea.setForeground(Theme.TEXT_PRIMARY);
        notesArea.setLineWrap(true);
        notesArea.setText("Executed Volatility framework plugins (pslist, malfind). Identified injected shellcode payload in host process.");
        notesArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));

        JPanel centerPanel = new JPanel(new BorderLayout(0, 8));
        centerPanel.setOpaque(false);
        centerPanel.add(form, BorderLayout.NORTH);
        centerPanel.add(createLabel("Detailed Methodological Notes & Findings:"), BorderLayout.CENTER);
        centerPanel.add(new JScrollPane(notesArea), BorderLayout.SOUTH);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBar.setOpaque(false);
        JButton saveBtn = Theme.createStyledButton("Save Activity", true);
        JButton cancelBtn = Theme.createStyledButton("Cancel", false);
        cancelBtn.addActionListener(e -> dialog.dispose());

        saveBtn.addActionListener(e -> {
            CaseIncident selCase = (CaseIncident) caseCb.getSelectedItem();
            User selInv = (User) invCb.getSelectedItem();
            String summary = summaryField.getText().trim();
            String notes = notesArea.getText().trim();

            if (selCase == null || selInv == null || summary.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill in all mandatory activity fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            InvestigationActivity act = new InvestigationActivity();
            act.setCaseId(selCase.getCaseId());
            act.setCaseNumber(selCase.getCaseNumber());
            act.setInvestigatorId(selInv.getUserId());
            act.setInvestigatorName(selInv.getFullName());
            act.setActivityType((String) actTypeCb.getSelectedItem());
            act.setActivitySummary(summary);
            act.setDetailedNotes(notes);

            boolean ok = investigatorDAO.addActivity(act);
            if (ok) {
                JOptionPane.showMessageDialog(dialog, "Investigation activity logged successfully.", "Activity Logged", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                refreshAll();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to log activity.", "Error", JOptionPane.ERROR_MESSAGE);
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
