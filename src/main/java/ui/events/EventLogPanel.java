package ui.events;

import dao.DeviceDAO;
import dao.LogDAO;
import dao.UserDAO;
import models.AccessLog;
import models.Device;
import models.EventLog;
import models.User;
import ui.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class EventLogPanel extends JPanel {
    private final LogDAO logDAO;
    private final UserDAO userDAO;
    private final DeviceDAO deviceDAO;

    private JTable eventTable;
    private DefaultTableModel eventTableModel;
    private JTable accessTable;
    private DefaultTableModel accessTableModel;
    private JTextField searchField;
    private JComboBox<String> eventTypeFilterCb;

    public EventLogPanel() {
        this.logDAO = new LogDAO();
        this.userDAO = new UserDAO();
        this.deviceDAO = new DeviceDAO();

        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.BG_PANEL);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
        refreshLogs();
    }

    private void initUI() {
        // Top Action Bar
        JPanel topBar = new JPanel(new BorderLayout(14, 0));
        topBar.setOpaque(false);

        JLabel title = new JLabel("MODULE 4: AUDIT, NETWORK & ACCESS LOGS");
        title.setFont(Theme.FONT_HEADER_LARGE);
        title.setForeground(Theme.TEXT_PRIMARY);

        JPanel actionControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionControls.setOpaque(false);

        searchField = Theme.createStyledTextField();
        searchField.setPreferredSize(new Dimension(200, 34));
        searchField.putClientProperty("JTextField.placeholderText", "Search IP, user, device...");

        eventTypeFilterCb = Theme.createStyledComboBox(new String[]{
            "ALL_EVENTS", "USER_LOGIN", "USER_LOGOUT", "AUTH_FAILURE", "DEVICE_CONNECTED",
            "DEVICE_DISCONNECTED", "FILE_ACCESSED", "PORT_SCAN_DETECTED", "MALWARE_TRIGGER", "PRIVILEGE_ESCALATION"
        });

        JButton searchBtn = Theme.createStyledButton("Filter Logs", false);
        searchBtn.addActionListener(e -> filterEventLogs());

        JButton addEventBtn = Theme.createStyledButton("➕ Simulate Event", true);
        addEventBtn.addActionListener(e -> openSimulateEventDialog());

        JButton addCardBtn = Theme.createStyledButton("💳 Simulate Card Swipe", false);
        addCardBtn.addActionListener(e -> openSimulateCardDialog());

        JButton refreshBtn = Theme.createStyledButton("🔄 Refresh", false);
        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            eventTypeFilterCb.setSelectedIndex(0);
            refreshLogs();
        });

        actionControls.add(searchField);
        actionControls.add(eventTypeFilterCb);
        actionControls.add(searchBtn);
        actionControls.add(addEventBtn);
        actionControls.add(addCardBtn);
        actionControls.add(refreshBtn);

        topBar.add(title, BorderLayout.WEST);
        topBar.add(actionControls, BorderLayout.EAST);

        // Tabbed View for System Events vs RFID Access Card Logs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(Theme.BG_CARD);
        tabbedPane.setForeground(Theme.TEXT_PRIMARY);
        tabbedPane.setFont(Theme.FONT_BOLD);

        // Tab 1: System & Network Events
        JPanel eventCard = Theme.createCardPanel();
        eventCard.setLayout(new BorderLayout());

        String[] eventCols = {"Log ID", "Timestamp", "User Identity", "Source Device", "Location", "Event Classification", "Source IP", "Event Payload / Narrative"};
        eventTableModel = new DefaultTableModel(eventCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        eventTable = new JTable(eventTableModel);
        Theme.styleTable(eventTable);

        // Highlight security triggers
        eventTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String val = (value != null) ? value.toString() : "";
                if (val.contains("FAIL") || val.contains("MALWARE") || val.contains("SCAN") || val.contains("PRIVILEGE")) {
                    setForeground(Theme.STATUS_CRITICAL);
                    setFont(Theme.FONT_BOLD);
                } else if (val.contains("LOGIN") || val.contains("CONNECTED")) {
                    setForeground(Theme.ACCENT_CYAN);
                } else {
                    setForeground(Theme.TEXT_PRIMARY);
                }
                return c;
            }
        });

        JScrollPane sp1 = new JScrollPane(eventTable);
        sp1.getViewport().setBackground(Theme.BG_PANEL);
        sp1.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR, 1));
        eventCard.add(sp1, BorderLayout.CENTER);

        // Tab 2: Access Card Swipes
        JPanel accessCard = Theme.createCardPanel();
        accessCard.setLayout(new BorderLayout());

        String[] accessCols = {"Access ID", "Timestamp", "Cardholder User", "Facility Location", "Card Number", "Access Decision"};
        accessTableModel = new DefaultTableModel(accessCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        accessTable = new JTable(accessTableModel);
        Theme.styleTable(accessTable);

        accessTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String val = (value != null) ? value.toString() : "";
                if (val.startsWith("GRANTED")) {
                    setForeground(Theme.STATUS_SUCCESS);
                } else {
                    setForeground(Theme.STATUS_DANGER);
                    setFont(Theme.FONT_BOLD);
                }
                return c;
            }
        });

        JScrollPane sp2 = new JScrollPane(accessTable);
        sp2.getViewport().setBackground(Theme.BG_PANEL);
        sp2.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR, 1));
        accessCard.add(sp2, BorderLayout.CENTER);

        tabbedPane.addTab("🖥️ Network & System Event Logs", eventCard);
        tabbedPane.addTab("💳 Physical Facility RFID Access Swipes", accessCard);

        add(topBar, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }

    public void refreshLogs() {
        eventTableModel.setRowCount(0);
        List<EventLog> events = logDAO.getAllEventLogs();
        for (EventLog el : events) {
            eventTableModel.addRow(new Object[] {
                el.getLogId(),
                el.getEventTimestamp(),
                (el.getUserName() != null) ? el.getUserName() : "System / Unauthenticated",
                (el.getDeviceName() != null) ? el.getDeviceName() : "Network Sensor",
                (el.getLocationName() != null) ? el.getLocationName() : "VLAN Core",
                el.getEventType(),
                el.getIpAddress(),
                el.getEventDetails()
            });
        }

        accessTableModel.setRowCount(0);
        List<AccessLog> swipes = logDAO.getAllAccessLogs();
        for (AccessLog al : swipes) {
            accessTableModel.addRow(new Object[] {
                al.getAccessId(),
                al.getAccessTimestamp(),
                al.getUserName(),
                al.getLocationName(),
                al.getCardNumber(),
                al.getAccessResult()
            });
        }
    }

    private void filterEventLogs() {
        String q = searchField.getText().trim().toLowerCase();
        String selectedType = (String) eventTypeFilterCb.getSelectedItem();

        eventTableModel.setRowCount(0);
        List<EventLog> events = logDAO.getAllEventLogs();
        for (EventLog el : events) {
            boolean matchType = "ALL_EVENTS".equalsIgnoreCase(selectedType) || selectedType.equalsIgnoreCase(el.getEventType());
            boolean matchQ = q.isEmpty() ||
                (el.getUserName() != null && el.getUserName().toLowerCase().contains(q)) ||
                (el.getIpAddress() != null && el.getIpAddress().contains(q)) ||
                (el.getDeviceName() != null && el.getDeviceName().toLowerCase().contains(q)) ||
                (el.getEventDetails() != null && el.getEventDetails().toLowerCase().contains(q));

            if (matchType && matchQ) {
                eventTableModel.addRow(new Object[] {
                    el.getLogId(),
                    el.getEventTimestamp(),
                    (el.getUserName() != null) ? el.getUserName() : "System / Unauthenticated",
                    (el.getDeviceName() != null) ? el.getDeviceName() : "Network Sensor",
                    (el.getLocationName() != null) ? el.getLocationName() : "VLAN Core",
                    el.getEventType(),
                    el.getIpAddress(),
                    el.getEventDetails()
                });
            }
        }
    }

    private void openSimulateEventDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Simulate New Event Log Entry", true);
        dialog.setSize(520, 440);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setBackground(Theme.BG_DARK);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 10));
        form.setOpaque(false);

        List<User> users = userDAO.getAllUsers();
        JComboBox<User> userCb = new JComboBox<>(users.toArray(new User[0]));
        userCb.setBackground(Theme.BG_INPUT);
        userCb.setForeground(Theme.TEXT_PRIMARY);

        List<Device> devs = deviceDAO.getAllDevices();
        JComboBox<Device> devCb = new JComboBox<>(devs.toArray(new Device[0]));
        devCb.setBackground(Theme.BG_INPUT);
        devCb.setForeground(Theme.TEXT_PRIMARY);

        JComboBox<String> typeCb = Theme.createStyledComboBox(new String[]{
            "USER_LOGIN", "USER_LOGOUT", "AUTH_FAILURE", "DEVICE_CONNECTED",
            "DEVICE_DISCONNECTED", "FILE_ACCESSED", "PORT_SCAN_DETECTED", "MALWARE_TRIGGER", "PRIVILEGE_ESCALATION"
        });

        JTextField ipField = Theme.createStyledTextField();
        ipField.setText("10.20.45.150");

        JComboBox<String> locCb = Theme.createStyledComboBox(new String[]{
            "1 - Turing Tech Center Lab-301",
            "2 - Aryabhata Complex Server Room DC-01",
            "3 - Main Academic Block Exam Cell 105",
            "4 - Ramanujan Library Reading Hall 2",
            "5 - Newton Hostel Block-C",
            "6 - Admin Tower Chambers"
        });

        JTextField detailsField = Theme.createStyledTextField();
        detailsField.setText("Simulated security telemetry injection from testing suite");

        form.add(createLabel("Attributed User:"));
        form.add(userCb);
        form.add(createLabel("Source Device:"));
        form.add(devCb);
        form.add(createLabel("Event Type:"));
        form.add(typeCb);
        form.add(createLabel("IP Address:"));
        form.add(ipField);
        form.add(createLabel("Campus Location:"));
        form.add(locCb);
        form.add(createLabel("Event Details:"));
        form.add(detailsField);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBar.setOpaque(false);
        JButton saveBtn = Theme.createStyledButton("Record Event", true);
        JButton cancelBtn = Theme.createStyledButton("Cancel", false);
        cancelBtn.addActionListener(e -> dialog.dispose());

        saveBtn.addActionListener(e -> {
            User u = (User) userCb.getSelectedItem();
            Device d = (Device) devCb.getSelectedItem();
            EventLog el = new EventLog();
            if (u != null) {
                el.setUserId(u.getUserId());
                el.setUserName(u.getFullName());
            }
            if (d != null) {
                el.setDeviceId(d.getDeviceId());
                el.setDeviceName(d.getDeviceName());
            }
            el.setLocationId(locCb.getSelectedIndex() + 1);
            el.setEventType((String) typeCb.getSelectedItem());
            el.setIpAddress(ipField.getText().trim());
            el.setEventDetails(detailsField.getText().trim());

            boolean ok = logDAO.addEventLog(el);
            if (ok) {
                dialog.dispose();
                refreshLogs();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to record event log.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnBar.add(saveBtn);
        btnBar.add(cancelBtn);

        panel.add(form, BorderLayout.CENTER);
        panel.add(btnBar, BorderLayout.SOUTH);
        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void openSimulateCardDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Simulate Facility Access Card Swipe", true);
        dialog.setSize(480, 360);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setBackground(Theme.BG_DARK);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel form = new JPanel(new GridLayout(4, 2, 10, 10));
        form.setOpaque(false);

        List<User> users = userDAO.getAllUsers();
        JComboBox<User> userCb = new JComboBox<>(users.toArray(new User[0]));
        userCb.setBackground(Theme.BG_INPUT);
        userCb.setForeground(Theme.TEXT_PRIMARY);

        JComboBox<String> locCb = Theme.createStyledComboBox(new String[]{
            "1 - Turing Tech Center Lab-301",
            "2 - Aryabhata Complex Server Room DC-01",
            "3 - Main Academic Block Exam Cell 105",
            "4 - Ramanujan Library Reading Hall 2",
            "5 - Newton Hostel Block-C",
            "6 - Admin Tower Chambers"
        });

        JTextField cardField = Theme.createStyledTextField();
        cardField.setText("CRD-STU-" + (10000 + (int)(Math.random() * 89999)));

        JComboBox<String> resCb = Theme.createStyledComboBox(new String[]{
            "GRANTED", "DENIED_INVALID_CARD", "DENIED_PERMISSION", "DENIED_OFF_HOURS"
        });

        form.add(createLabel("User / Cardholder:"));
        form.add(userCb);
        form.add(createLabel("Checkpoint Location:"));
        form.add(locCb);
        form.add(createLabel("Card RFID Number:"));
        form.add(cardField);
        form.add(createLabel("Access Decision:"));
        form.add(resCb);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBar.setOpaque(false);
        JButton saveBtn = Theme.createStyledButton("Log Swipe", true);
        JButton cancelBtn = Theme.createStyledButton("Cancel", false);
        cancelBtn.addActionListener(e -> dialog.dispose());

        saveBtn.addActionListener(e -> {
            User u = (User) userCb.getSelectedItem();
            if (u == null) return;

            AccessLog al = new AccessLog();
            al.setUserId(u.getUserId());
            al.setUserName(u.getFullName());
            al.setLocationId(locCb.getSelectedIndex() + 1);
            al.setCardNumber(cardField.getText().trim());
            al.setAccessResult((String) resCb.getSelectedItem());

            boolean ok = logDAO.addAccessLog(al);
            if (ok) {
                dialog.dispose();
                refreshLogs();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to record access swipe.", "Error", JOptionPane.ERROR_MESSAGE);
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
