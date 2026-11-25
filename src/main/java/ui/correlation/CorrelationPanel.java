package ui.correlation;

import dao.CorrelationDAO;
import models.CorrelationResult;
import ui.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CorrelationPanel extends JPanel {
    private final CorrelationDAO correlationDAO;

    private JComboBox<String> entityTypeCb;
    private JTextField searchField;
    private JTable correlationTable;
    private DefaultTableModel tableModel;
    private JLabel countLabel;

    public CorrelationPanel() {
        this.correlationDAO = new CorrelationDAO();

        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.BG_PANEL);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
        // Run initial demo correlation for Vikram Aditya
        runCorrelation("USER", "Vikram");
    }

    private void initUI() {
        // Top Action Bar
        JPanel topBar = new JPanel(new BorderLayout(14, 0));
        topBar.setOpaque(false);

        JLabel title = new JLabel("MODULE 9: MULTI-RELATIONAL INVESTIGATION CORRELATION MATRIX");
        title.setFont(Theme.FONT_HEADER_LARGE);
        title.setForeground(Theme.TEXT_PRIMARY);

        JPanel quickSamples = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        quickSamples.setOpaque(false);
        quickSamples.add(new JLabel("Quick Analysis:"));

        JButton sampleUser = Theme.createStyledButton("👤 Vikram (Exam Leak)", false);
        sampleUser.addActionListener(e -> {
            entityTypeCb.setSelectedItem("USER");
            searchField.setText("Vikram");
            runCorrelation("USER", "Vikram");
        });

        JButton sampleDev = Theme.createStyledButton("💻 David Kali Laptop", false);
        sampleDev.addActionListener(e -> {
            entityTypeCb.setSelectedItem("DEVICE");
            searchField.setText("SN-ASU-663819");
            runCorrelation("DEVICE", "SN-ASU-663819");
        });

        JButton sampleCase = Theme.createStyledButton("🚨 Case CDF-2026-001", false);
        sampleCase.addActionListener(e -> {
            entityTypeCb.setSelectedItem("INCIDENT");
            searchField.setText("CDF-2026-001");
            runCorrelation("INCIDENT", "CDF-2026-001");
        });

        quickSamples.add(sampleUser);
        quickSamples.add(sampleDev);
        quickSamples.add(sampleCase);

        topBar.add(title, BorderLayout.WEST);
        topBar.add(quickSamples, BorderLayout.EAST);

        // Filter / Search Bar Card
        JPanel searchCard = Theme.createCardPanel();
        searchCard.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 6));

        entityTypeCb = Theme.createStyledComboBox(new String[]{"USER", "DEVICE", "INCIDENT", "LOCATION"});
        entityTypeCb.setPreferredSize(new Dimension(140, 36));

        searchField = Theme.createStyledTextField();
        searchField.setPreferredSize(new Dimension(280, 36));
        searchField.setText("Vikram");

        JButton runBtn = Theme.createStyledButton("⚡ Execute SQL Multi-Join Correlation", true);
        runBtn.addActionListener(e -> runCorrelation((String) entityTypeCb.getSelectedItem(), searchField.getText().trim()));

        countLabel = new JLabel("Found 0 correlated links");
        countLabel.setFont(Theme.FONT_BOLD);
        countLabel.setForeground(Theme.ACCENT_CYAN);

        searchCard.add(new JLabel("Entity Pivot:"));
        searchCard.add(entityTypeCb);
        searchCard.add(new JLabel("Entity Identifier / Name:"));
        searchCard.add(searchField);
        searchCard.add(runBtn);
        searchCard.add(countLabel);

        // Forensic Correlation Safeguard Notice
        JPanel noticeBanner = new JPanel(new BorderLayout(10, 0));
        noticeBanner.setBackground(new Color(45, 26, 74));
        noticeBanner.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(168, 85, 247), 1, true),
            BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));
        JLabel noticeIcon = new JLabel("⚖");
        noticeIcon.setFont(Theme.FONT_BOLD);
        JLabel noticeText = new JLabel("EVIDENTIARY SAFEGUARD: Relational correlations depict temporal convergence, shared credentials, and device topologies across database joins. Correlations serve as investigative leads and must not be misconstrued as automated culpability or definitive attribution.");
        noticeText.setFont(Theme.FONT_SMALL);
        noticeText.setForeground(new Color(233, 213, 255));
        noticeBanner.add(noticeIcon, BorderLayout.WEST);
        noticeBanner.add(noticeText, BorderLayout.CENTER);

        // Results Table Card
        JPanel tableCard = Theme.createCardPanel();
        tableCard.setLayout(new BorderLayout());

        String[] cols = {
            "Pivot Subject", "Pivot Type", "Related Relational Domain", 
            "Matched Identifier", "Correlated Artifact / Activity", 
            "Forensic Rationale (WHY RECORDS ARE LINKED)", "Event Timestamp", "Status / Severity"
        };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        correlationTable = new JTable(tableModel);
        Theme.styleTable(correlationTable);

        // Format Rationale column in electric cyan
        correlationTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setForeground(Theme.ACCENT_CYAN);
                setFont(Theme.FONT_BOLD);
                return c;
            }
        });

        JScrollPane sp = new JScrollPane(correlationTable);
        sp.getViewport().setBackground(Theme.BG_PANEL);
        sp.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR, 1));
        tableCard.add(sp, BorderLayout.CENTER);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        centerPanel.add(searchCard);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(noticeBanner);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(tableCard);

        add(topBar, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    private void runCorrelation(String entityType, String key) {
        if (key == null || key.isBlank()) {
            JOptionPane.showMessageDialog(this, "Please enter an identifier, username, device serial, or case number.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        tableModel.setRowCount(0);
        List<CorrelationResult> results = correlationDAO.correlateEntity(entityType, key);

        for (CorrelationResult r : results) {
            tableModel.addRow(new Object[] {
                r.getTargetEntity(),
                r.getTargetType(),
                r.getRelatedCategory(),
                r.getIdentifier(),
                r.getSummary(),
                r.getConnectionReason(),
                r.getTimestamp(),
                r.getSeverityOrStatus()
            });
        }

        countLabel.setText(String.format("Found %d correlated forensic connections for '%s'", results.size(), key));
    }
}
