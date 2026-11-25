package ui.custody;

import dao.CustodyDAO;
import dao.EvidenceDAO;
import dao.UserDAO;
import models.Evidence;
import models.EvidenceCustody;
import models.User;
import ui.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CustodyPanel extends JPanel {
    private final CustodyDAO custodyDAO;
    private final EvidenceDAO evidenceDAO;
    private final UserDAO userDAO;

    private JTable custodyTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> evidenceFilterCb;

    public CustodyPanel() {
        this.custodyDAO = new CustodyDAO();
        this.evidenceDAO = new EvidenceDAO();
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

        JLabel title = new JLabel("MODULE 6: FORENSIC CHAIN OF CUSTODY");
        title.setFont(Theme.FONT_HEADER_LARGE);
        title.setForeground(Theme.TEXT_PRIMARY);

        JPanel actionControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionControls.setOpaque(false);

        evidenceFilterCb = new JComboBox<>();
        evidenceFilterCb.setFont(Theme.FONT_REGULAR);
        evidenceFilterCb.setBackground(Theme.BG_INPUT);
        evidenceFilterCb.setForeground(Theme.TEXT_PRIMARY);
        evidenceFilterCb.setPreferredSize(new Dimension(280, 34));
        populateEvidenceFilter();

        JButton filterBtn = Theme.createStyledButton("Filter Timeline", false);
        filterBtn.addActionListener(e -> applyCustodyFilter());

        JButton transferBtn = Theme.createStyledButton("⛓️ Transfer Custody (Transaction)", true);
        transferBtn.addActionListener(e -> openTransferDialog());

        JButton refreshBtn = Theme.createStyledButton("🔄 Refresh", false);
        refreshBtn.addActionListener(e -> {
            populateEvidenceFilter();
            refreshTable();
        });

        actionControls.add(new JLabel("Timeline:"));
        actionControls.add(evidenceFilterCb);
        actionControls.add(filterBtn);
        actionControls.add(transferBtn);
        actionControls.add(refreshBtn);

        topBar.add(title, BorderLayout.WEST);
        topBar.add(actionControls, BorderLayout.EAST);

        // Chain of custody preservation banner
        JPanel infoBanner = new JPanel(new BorderLayout(10, 0));
        infoBanner.setBackground(new Color(30, 27, 75));
        infoBanner.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.ACCENT_CYAN, 1, true),
            BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));
        JLabel icon = new JLabel("🔒");
        icon.setFont(Theme.FONT_BOLD);
        JLabel text = new JLabel("CUSTODY IMMUTABILITY RULE: Historical custody entries are strictly append-only. Custody transfers are atomically executed via SQL Transactions ensuring concurrent integrity between custody logs and evidence status.");
        text.setFont(Theme.FONT_SMALL);
        text.setForeground(Theme.TEXT_SECONDARY);
        infoBanner.add(icon, BorderLayout.WEST);
        infoBanner.add(text, BorderLayout.CENTER);

        // Table Card
        JPanel card = Theme.createCardPanel();
        card.setLayout(new BorderLayout());

        String[] cols = {"Custody ID", "Evidence Code", "Evidence Description", "Case #", "Transferred From (Sender)", "Transferred To (Recipient)", "Transfer Timestamp", "Action / Protocol", "Integrity Verified", "Remarks / Purpose"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        custodyTable = new JTable(tableModel);
        Theme.styleTable(custodyTable);

        custodyTable.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setForeground(Theme.ACCENT_CYAN);
                setFont(Theme.FONT_BOLD);
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(custodyTable);
        scrollPane.getViewport().setBackground(Theme.BG_PANEL);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR, 1));
        card.add(scrollPane, BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setOpaque(false);
        centerPanel.add(infoBanner, BorderLayout.NORTH);
        centerPanel.add(card, BorderLayout.CENTER);

        add(topBar, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    private void populateEvidenceFilter() {
        evidenceFilterCb.removeAllItems();
        evidenceFilterCb.addItem("ALL_EVIDENCE_ITEMS");
        for (Evidence ev : evidenceDAO.getAllEvidence()) {
            evidenceFilterCb.addItem(ev.getEvidenceId() + ": " + ev.getEvidenceCode() + " - " + ev.getEvidenceName());
        }
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        List<EvidenceCustody> list = custodyDAO.getAllCustodyTransfers();
        for (EvidenceCustody ec : list) {
            tableModel.addRow(new Object[] {
                ec.getCustodyId(),
                ec.getEvidenceCode(),
                ec.getEvidenceName(),
                ec.getCaseNumber(),
                ec.getTransferredFromName(),
                ec.getTransferredToName(),
                ec.getTransferDate(),
                ec.getActionType(),
                ec.isIntegrityVerified() ? "✔ VERIFIED" : "⚠ UNVERIFIED",
                ec.getPurposeRemarks()
            });
        }
    }

    private void applyCustodyFilter() {
        String sel = (String) evidenceFilterCb.getSelectedItem();
        if (sel == null || sel.startsWith("ALL")) {
            refreshTable();
            return;
        }

        int colonIdx = sel.indexOf(":");
        int evId = Integer.parseInt(sel.substring(0, colonIdx).trim());

        tableModel.setRowCount(0);
        List<EvidenceCustody> list = custodyDAO.getCustodyHistoryForEvidence(evId);
        for (EvidenceCustody ec : list) {
            tableModel.addRow(new Object[] {
                ec.getCustodyId(),
                ec.getEvidenceCode(),
                ec.getEvidenceName(),
                ec.getCaseNumber(),
                ec.getTransferredFromName(),
                ec.getTransferredToName(),
                ec.getTransferDate(),
                ec.getActionType(),
                ec.isIntegrityVerified() ? "✔ VERIFIED" : "⚠ UNVERIFIED",
                ec.getPurposeRemarks()
            });
        }
    }

    private void openTransferDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Execute Chain of Custody Transfer (SQL Transaction)", true);
        dialog.setSize(540, 480);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setBackground(Theme.BG_DARK);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 10));
        form.setOpaque(false);

        List<Evidence> evList = evidenceDAO.getAllEvidence();
        JComboBox<Evidence> evCb = new JComboBox<>(evList.toArray(new Evidence[0]));
        evCb.setBackground(Theme.BG_INPUT);
        evCb.setForeground(Theme.TEXT_PRIMARY);

        List<User> users = userDAO.getAllUsers();
        JComboBox<User> fromCb = new JComboBox<>(users.toArray(new User[0]));
        fromCb.setBackground(Theme.BG_INPUT);
        fromCb.setForeground(Theme.TEXT_PRIMARY);

        JComboBox<User> toCb = new JComboBox<>(users.toArray(new User[0]));
        toCb.setBackground(Theme.BG_INPUT);
        toCb.setForeground(Theme.TEXT_PRIMARY);
        if (users.size() > 1) toCb.setSelectedIndex(1);

        JComboBox<String> actionCb = Theme.createStyledComboBox(new String[]{
            "INTERNAL_TRANSFER", "FORENSIC_ANALYSIS", "SECURE_VAULT_DEPOSIT", "ADMINISTRATIVE_REVIEW", "RETURN_TO_OWNER"
        });

        JComboBox<String> statusCb = Theme.createStyledComboBox(new String[]{
            "IN_ANALYSIS", "IN_TRANSIT", "SECURED", "COURT_EVIDENCE", "RETURNED", "ARCHIVED"
        });

        JTextField remarksField = Theme.createStyledTextField();
        remarksField.setText("Transferred for bitstream image parsing and SQLite artifact recovery");

        form.add(createLabel("Select Evidence:"));
        form.add(evCb);
        form.add(createLabel("Transferred From (Sender):"));
        form.add(fromCb);
        form.add(createLabel("Transferred To (Recipient):"));
        form.add(toCb);
        form.add(createLabel("Transfer Action Protocol:"));
        form.add(actionCb);
        form.add(createLabel("New Evidence Status:"));
        form.add(statusCb);
        form.add(createLabel("Purpose / Forensic Remarks:"));
        form.add(remarksField);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBar.setOpaque(false);
        JButton saveBtn = Theme.createStyledButton("Execute Transfer (Commit)", true);
        JButton cancelBtn = Theme.createStyledButton("Cancel", false);
        cancelBtn.addActionListener(e -> dialog.dispose());

        saveBtn.addActionListener(e -> {
            Evidence selEv = (Evidence) evCb.getSelectedItem();
            User selFrom = (User) fromCb.getSelectedItem();
            User selTo = (User) toCb.getSelectedItem();
            String action = (String) actionCb.getSelectedItem();
            String newStatus = (String) statusCb.getSelectedItem();
            String remarks = remarksField.getText().trim();

            if (selEv == null || selFrom == null || selTo == null || remarks.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill in all custody transfer fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            EvidenceCustody ec = new EvidenceCustody();
            ec.setEvidenceId(selEv.getEvidenceId());
            ec.setEvidenceCode(selEv.getEvidenceCode());
            ec.setEvidenceName(selEv.getEvidenceName());
            ec.setCaseNumber(selEv.getCaseNumber());
            ec.setTransferredFromId(selFrom.getUserId());
            ec.setTransferredFromName(selFrom.getFullName());
            ec.setTransferredToId(selTo.getUserId());
            ec.setTransferredToName(selTo.getFullName());
            ec.setActionType(action);
            ec.setPurposeRemarks(remarks);
            ec.setIntegrityVerified(true);

            boolean ok = custodyDAO.recordTransfer(ec, newStatus);
            if (ok) {
                JOptionPane.showMessageDialog(dialog, "Custody transfer committed successfully within database transaction.", "Transfer Complete", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                populateEvidenceFilter();
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(dialog, "Transaction failed to commit custody transfer.", "Transaction Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnBar.add(saveBtn);
        btnBar.add(cancelBtn);

        panel.add(form, BorderLayout.CENTER);
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
