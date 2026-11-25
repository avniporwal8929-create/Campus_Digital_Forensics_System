package ui.evidence;

import dao.EvidenceDAO;
import dao.IncidentDAO;
import dao.UserDAO;
import models.CaseIncident;
import models.Evidence;
import models.User;
import services.ForensicsService;
import ui.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

public class EvidencePanel extends JPanel {
    private final EvidenceDAO evidenceDAO;
    private final IncidentDAO incidentDAO;
    private final UserDAO userDAO;

    private JTable evidenceTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> typeFilterCb;

    public EvidencePanel() {
        this.evidenceDAO = new EvidenceDAO();
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

        JLabel title = new JLabel("MODULE 5: DIGITAL FORENSICS EVIDENCE LOCKER");
        title.setFont(Theme.FONT_HEADER_LARGE);
        title.setForeground(Theme.TEXT_PRIMARY);

        JPanel actionControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionControls.setOpaque(false);

        searchField = Theme.createStyledTextField();
        searchField.setPreferredSize(new Dimension(180, 34));
        searchField.putClientProperty("JTextField.placeholderText", "Search code, name, hash...");

        typeFilterCb = Theme.createStyledComboBox(new String[]{
            "ALL", "HARD_DRIVE", "USB_DRIVE", "NETWORK_PCAP", "LOG_ARCHIVE",
            "RAM_DUMP", "SMARTPHONE_IMAGE", "CARD_SWIPE_EXPORT", "SYSTEM_SNAPSHOT"
        });

        JButton searchBtn = Theme.createStyledButton("Filter", false);
        searchBtn.addActionListener(e -> applyFilter());

        JButton addBtn = Theme.createStyledButton("➕ Seize / Add Evidence", true);
        addBtn.addActionListener(e -> openEvidenceDialog());

        JButton calcHashBtn = Theme.createStyledButton("#️⃣ SHA-256 Tool", false);
        calcHashBtn.addActionListener(e -> openHashCalculatorDialog());

        JButton viewHashBtn = Theme.createStyledButton("🔍 View Full Hash", false);
        viewHashBtn.addActionListener(e -> viewSelectedHash());

        JButton refreshBtn = Theme.createStyledButton("🔄 Refresh", false);
        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            typeFilterCb.setSelectedIndex(0);
            refreshTable();
        });

        actionControls.add(searchField);
        actionControls.add(typeFilterCb);
        actionControls.add(searchBtn);
        actionControls.add(addBtn);
        actionControls.add(calcHashBtn);
        actionControls.add(viewHashBtn);
        actionControls.add(refreshBtn);

        topBar.add(title, BorderLayout.WEST);
        topBar.add(actionControls, BorderLayout.EAST);

        // Integrity Notice Banner
        JPanel noticeBanner = new JPanel(new BorderLayout(10, 0));
        noticeBanner.setBackground(new Color(23, 37, 84));
        noticeBanner.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.ACCENT_CYAN, 1, true),
            BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));
        JLabel noticeIcon = new JLabel("ℹ");
        noticeIcon.setFont(Theme.FONT_BOLD);
        noticeIcon.setForeground(Theme.ACCENT_CYAN);
        JLabel noticeText = new JLabel("FORENSIC INTEGRITY PRINCIPLE: A cryptographic SHA-256 hash confirms bitstream integrity from the moment of acquisition. It guarantees data has not altered, but legal admissibility requires unbroken chain of custody and verifiable acquisition methodology.");
        noticeText.setFont(Theme.FONT_SMALL);
        noticeText.setForeground(Theme.TEXT_SECONDARY);
        noticeBanner.add(noticeIcon, BorderLayout.WEST);
        noticeBanner.add(noticeText, BorderLayout.CENTER);

        // Table Card
        JPanel card = Theme.createCardPanel();
        card.setLayout(new BorderLayout());

        String[] cols = {"Evidence ID", "Code", "Item Description", "Type", "Associated Case", "SHA-256 Hash Digest (Truncated)", "Seized By", "Storage Vault Location", "Status", "Date"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        evidenceTable = new JTable(tableModel);
        Theme.styleTable(evidenceTable);

        JScrollPane scrollPane = new JScrollPane(evidenceTable);
        scrollPane.getViewport().setBackground(Theme.BG_PANEL);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR, 1));
        card.add(scrollPane, BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setOpaque(false);
        centerPanel.add(noticeBanner, BorderLayout.NORTH);
        centerPanel.add(card, BorderLayout.CENTER);

        add(topBar, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        List<Evidence> list = evidenceDAO.getAllEvidence();
        for (Evidence ev : list) {
            String hash = ev.getSha256Hash();
            String truncHash = (hash != null && hash.length() > 16) ? hash.substring(0, 16) + "..." : hash;
            tableModel.addRow(new Object[] {
                ev.getEvidenceId(),
                ev.getEvidenceCode(),
                ev.getEvidenceName(),
                ev.getEvidenceType(),
                ev.getCaseNumber(),
                truncHash,
                ev.getCollectorName(),
                ev.getStorageLocation(),
                ev.getStatus(),
                ev.getCollectionDate()
            });
        }
    }

    private void applyFilter() {
        String q = searchField.getText().trim();
        String type = (String) typeFilterCb.getSelectedItem();
        tableModel.setRowCount(0);
        List<Evidence> list = evidenceDAO.searchEvidence(q, type);
        for (Evidence ev : list) {
            String hash = ev.getSha256Hash();
            String truncHash = (hash != null && hash.length() > 16) ? hash.substring(0, 16) + "..." : hash;
            tableModel.addRow(new Object[] {
                ev.getEvidenceId(),
                ev.getEvidenceCode(),
                ev.getEvidenceName(),
                ev.getEvidenceType(),
                ev.getCaseNumber(),
                truncHash,
                ev.getCollectorName(),
                ev.getStorageLocation(),
                ev.getStatus(),
                ev.getCollectionDate()
            });
        }
    }

    private void viewSelectedHash() {
        int selected = evidenceTable.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Please select an evidence row to view its complete hash.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int evId = (int) tableModel.getValueAt(selected, 0);
        for (Evidence ev : evidenceDAO.getAllEvidence()) {
            if (ev.getEvidenceId() == evId) {
                JTextArea hashArea = new JTextArea(ev.getSha256Hash());
                hashArea.setFont(Theme.FONT_MONO);
                hashArea.setEditable(false);
                hashArea.setLineWrap(true);
                JOptionPane.showMessageDialog(
                    this,
                    new Object[] {
                        "Evidence Code: " + ev.getEvidenceCode(),
                        "Evidence Name: " + ev.getEvidenceName(),
                        "Case: " + ev.getCaseNumber(),
                        "Full SHA-256 Checksum:",
                        hashArea
                    },
                    "Cryptographic Verification Digest",
                    JOptionPane.INFORMATION_MESSAGE
                );
                return;
            }
        }
    }

    private void openHashCalculatorDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "SHA-256 Cryptographic Hash Tool", true);
        dialog.setSize(540, 360);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setBackground(Theme.BG_DARK);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel title = new JLabel("Compute & Verify File Hash on Disk");
        title.setFont(Theme.FONT_HEADER_MED);
        title.setForeground(Theme.ACCENT_CYAN);

        JPanel form = new JPanel(new GridLayout(3, 1, 0, 8));
        form.setOpaque(false);

        JPanel filePickPanel = new JPanel(new BorderLayout(8, 0));
        filePickPanel.setOpaque(false);
        JTextField pathField = Theme.createStyledTextField();
        JButton browseBtn = Theme.createStyledButton("Browse...", false);
        filePickPanel.add(pathField, BorderLayout.CENTER);
        filePickPanel.add(browseBtn, BorderLayout.EAST);

        JTextArea hashResultArea = new JTextArea();
        hashResultArea.setFont(Theme.FONT_MONO);
        hashResultArea.setBackground(Theme.BG_INPUT);
        hashResultArea.setForeground(Theme.ACCENT_CYAN);
        hashResultArea.setLineWrap(true);
        hashResultArea.setEditable(false);
        hashResultArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        browseBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int res = chooser.showOpenDialog(dialog);
            if (res == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                pathField.setText(f.getAbsolutePath());
                String calculated = ForensicsService.calculateSHA256(f);
                hashResultArea.setText("SHA-256 Calculated:\n" + calculated + "\n\nFile Size: " + f.length() + " bytes");
            }
        });

        form.add(new JLabel("Select Forensic Disk Image, PCAP, or Document:"));
        form.add(filePickPanel);
        form.add(new JLabel("Calculated Hash Output:"));

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnBar.setOpaque(false);
        JButton closeBtn = Theme.createStyledButton("Close", false);
        closeBtn.addActionListener(e -> dialog.dispose());
        btnBar.add(closeBtn);

        panel.add(title, BorderLayout.NORTH);
        panel.add(form, BorderLayout.CENTER);
        panel.add(hashResultArea, BorderLayout.SOUTH);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void openEvidenceDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Register Seized Forensic Evidence", true);
        dialog.setSize(540, 520);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setBackground(Theme.BG_DARK);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel form = new JPanel(new GridLayout(8, 2, 10, 8));
        form.setOpaque(false);

        List<CaseIncident> cases = incidentDAO.getAllCases();
        JComboBox<CaseIncident> caseCb = new JComboBox<>(cases.toArray(new CaseIncident[0]));
        caseCb.setBackground(Theme.BG_INPUT);
        caseCb.setForeground(Theme.TEXT_PRIMARY);

        String autoCode = "EVD-2026-" + String.format("%03d", evidenceDAO.getAllEvidence().size() + 1);
        JTextField codeField = Theme.createStyledTextField();
        codeField.setText(autoCode);
        codeField.setEditable(false);

        JTextField nameField = Theme.createStyledTextField();
        JComboBox<String> typeCb = Theme.createStyledComboBox(new String[]{
            "HARD_DRIVE", "USB_DRIVE", "NETWORK_PCAP", "LOG_ARCHIVE",
            "RAM_DUMP", "SMARTPHONE_IMAGE", "CARD_SWIPE_EXPORT", "SYSTEM_SNAPSHOT"
        });

        JTextField fileRefField = Theme.createStyledTextField();
        fileRefField.setText("/evidence/cases/raw_img.dd");

        JTextField hashField = Theme.createStyledTextField();
        hashField.setText(ForensicsService.calculateSHA256(autoCode + System.currentTimeMillis()));

        List<User> investigators = userDAO.getInvestigators();
        JComboBox<User> invCb = new JComboBox<>(investigators.toArray(new User[0]));
        invCb.setBackground(Theme.BG_INPUT);
        invCb.setForeground(Theme.TEXT_PRIMARY);

        JTextField vaultField = Theme.createStyledTextField();
        vaultField.setText("Forensics Safe Cabinet A - Shelf 3");

        form.add(createLabel("Associated Incident:"));
        form.add(caseCb);
        form.add(createLabel("Evidence Code:"));
        form.add(codeField);
        form.add(createLabel("Artifact Description:"));
        form.add(nameField);
        form.add(createLabel("Evidence Type:"));
        form.add(typeCb);
        form.add(createLabel("File Reference / Path:"));
        form.add(fileRefField);
        form.add(createLabel("SHA-256 Hash Digest:"));
        form.add(hashField);
        form.add(createLabel("Collected By (Investigator):"));
        form.add(invCb);
        form.add(createLabel("Storage Vault Location:"));
        form.add(vaultField);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBar.setOpaque(false);
        JButton saveBtn = Theme.createStyledButton("Register Evidence", true);
        JButton cancelBtn = Theme.createStyledButton("Cancel", false);
        cancelBtn.addActionListener(e -> dialog.dispose());

        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String hash = hashField.getText().trim();
            CaseIncident selCase = (CaseIncident) caseCb.getSelectedItem();
            User selInv = (User) invCb.getSelectedItem();

            if (name.isEmpty() || hash.isEmpty() || selCase == null || selInv == null) {
                JOptionPane.showMessageDialog(dialog, "Please fill in all mandatory evidence fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Evidence ev = new Evidence();
            ev.setCaseId(selCase.getCaseId());
            ev.setCaseNumber(selCase.getCaseNumber());
            ev.setEvidenceCode(codeField.getText().trim());
            ev.setEvidenceName(name);
            ev.setEvidenceType((String) typeCb.getSelectedItem());
            ev.setDescription(name + " collected during " + selCase.getCaseNumber());
            ev.setFileReference(fileRefField.getText().trim());
            ev.setSha256Hash(hash);
            ev.setCollectedById(selInv.getUserId());
            ev.setCollectorName(selInv.getFullName());
            ev.setStorageLocation(vaultField.getText().trim());
            ev.setStatus("SECURED");

            boolean ok = evidenceDAO.addEvidence(ev);
            if (ok) {
                dialog.dispose();
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to register evidence record.", "Error", JOptionPane.ERROR_MESSAGE);
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
