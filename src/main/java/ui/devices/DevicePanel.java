package ui.devices;

import dao.DeviceDAO;
import dao.UserDAO;
import models.Device;
import models.User;
import ui.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class DevicePanel extends JPanel {
    private final DeviceDAO deviceDAO;
    private final UserDAO userDAO;
    private JTable deviceTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public DevicePanel() {
        this.deviceDAO = new DeviceDAO();
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

        JLabel title = new JLabel("MODULE 2: CAMPUS DEVICE REGISTRY");
        title.setFont(Theme.FONT_HEADER_LARGE);
        title.setForeground(Theme.TEXT_PRIMARY);

        JPanel actionControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionControls.setOpaque(false);

        searchField = Theme.createStyledTextField();
        searchField.setPreferredSize(new Dimension(240, 34));
        searchField.putClientProperty("JTextField.placeholderText", "Search name, serial, MAC, IP, owner...");

        JButton searchBtn = Theme.createStyledButton("🔍 Search", false);
        searchBtn.addActionListener(e -> performSearch());

        JButton addBtn = Theme.createStyledButton("➕ Register Device", true);
        addBtn.addActionListener(e -> openDeviceDialog(null));

        JButton editBtn = Theme.createStyledButton("✏ Edit Device", false);
        editBtn.addActionListener(e -> editSelectedDevice());

        JButton delBtn = Theme.createDangerButton("🗑 Decommission");
        delBtn.addActionListener(e -> deleteSelectedDevice());

        JButton refreshBtn = Theme.createStyledButton("🔄 Refresh", false);
        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            refreshTable();
        });

        actionControls.add(searchField);
        actionControls.add(searchBtn);
        actionControls.add(addBtn);
        actionControls.add(editBtn);
        actionControls.add(delBtn);
        actionControls.add(refreshBtn);

        topBar.add(title, BorderLayout.WEST);
        topBar.add(actionControls, BorderLayout.EAST);

        // Table Card
        JPanel card = Theme.createCardPanel();
        card.setLayout(new BorderLayout());

        String[] cols = {"Device ID", "Device Name", "Type", "Serial Number", "MAC Address", "IP Address", "Operating System", "Assigned Owner", "Status", "Notes"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        deviceTable = new JTable(tableModel);
        Theme.styleTable(deviceTable);

        JScrollPane scrollPane = new JScrollPane(deviceTable);
        scrollPane.getViewport().setBackground(Theme.BG_PANEL);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR, 1));
        card.add(scrollPane, BorderLayout.CENTER);

        add(topBar, BorderLayout.NORTH);
        add(card, BorderLayout.CENTER);
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        List<Device> list = deviceDAO.getAllDevices();
        for (Device d : list) {
            tableModel.addRow(new Object[] {
                d.getDeviceId(),
                d.getDeviceName(),
                d.getDeviceType(),
                d.getSerialNumber(),
                d.getMacAddress(),
                (d.getIpAddress() != null) ? d.getIpAddress() : "Dynamic DHCP",
                d.getOperatingSystem(),
                (d.getOwnerName() != null) ? d.getOwnerName() : "User #" + d.getUserId(),
                d.getStatus(),
                (d.getNotes() != null) ? d.getNotes() : ""
            });
        }
    }

    private void performSearch() {
        String q = searchField.getText().trim();
        tableModel.setRowCount(0);
        List<Device> list = deviceDAO.searchDevices(q);
        for (Device d : list) {
            tableModel.addRow(new Object[] {
                d.getDeviceId(),
                d.getDeviceName(),
                d.getDeviceType(),
                d.getSerialNumber(),
                d.getMacAddress(),
                (d.getIpAddress() != null) ? d.getIpAddress() : "Dynamic DHCP",
                d.getOperatingSystem(),
                (d.getOwnerName() != null) ? d.getOwnerName() : "User #" + d.getUserId(),
                d.getStatus(),
                (d.getNotes() != null) ? d.getNotes() : ""
            });
        }
    }

    private void editSelectedDevice() {
        int selected = deviceTable.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Please select a device from the table to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int devId = (int) tableModel.getValueAt(selected, 0);
        for (Device d : deviceDAO.getAllDevices()) {
            if (d.getDeviceId() == devId) {
                openDeviceDialog(d);
                return;
            }
        }
    }

    private void deleteSelectedDevice() {
        int selected = deviceTable.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Please select a device to decommission/delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int devId = (int) tableModel.getValueAt(selected, 0);
        String devName = (String) tableModel.getValueAt(selected, 1);
        String serial = (String) tableModel.getValueAt(selected, 3);

        int confirm = JOptionPane.showConfirmDialog(
            this,
            String.format("Decommission/Delete device '%s' (Serial: %s)?\nEnsure associated evidence has been securely copied.", devName, serial),
            "Confirm Device Decommission",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = deviceDAO.deleteDevice(devId);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Device removed from registry.", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete device.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openDeviceDialog(Device existing) {
        boolean isEdit = (existing != null);
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), isEdit ? "Edit Device Information" : "Register New Campus Device", true);
        dialog.setSize(500, 540);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setBackground(Theme.BG_DARK);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel form = new JPanel(new GridLayout(9, 2, 10, 8));
        form.setOpaque(false);

        List<User> users = userDAO.getAllUsers();
        JComboBox<User> userCb = new JComboBox<>(users.toArray(new User[0]));
        userCb.setBackground(Theme.BG_INPUT);
        userCb.setForeground(Theme.TEXT_PRIMARY);

        JTextField nameField = Theme.createStyledTextField();
        JComboBox<String> typeCb = Theme.createStyledComboBox(new String[]{"LAPTOP", "DESKTOP", "SMARTPHONE", "TABLET", "SERVER", "IOT_DEVICE"});
        JTextField serialField = Theme.createStyledTextField();
        JTextField macField = Theme.createStyledTextField();
        JTextField osField = Theme.createStyledTextField();
        JTextField ipField = Theme.createStyledTextField();
        JComboBox<String> statCb = Theme.createStyledComboBox(new String[]{"ACTIVE", "SEIZED", "QUARANTINED", "DECOMMISSIONED"});
        JTextField notesField = Theme.createStyledTextField();

        if (isEdit) {
            for (int i = 0; i < users.size(); i++) {
                if (users.get(i).getUserId() == existing.getUserId()) {
                    userCb.setSelectedIndex(i);
                    break;
                }
            }
            nameField.setText(existing.getDeviceName());
            typeCb.setSelectedItem(existing.getDeviceType());
            serialField.setText(existing.getSerialNumber());
            macField.setText(existing.getMacAddress());
            osField.setText(existing.getOperatingSystem());
            ipField.setText((existing.getIpAddress() != null) ? existing.getIpAddress() : "");
            statCb.setSelectedItem(existing.getStatus());
            notesField.setText((existing.getNotes() != null) ? existing.getNotes() : "");
        } else {
            macField.setText("00:1A:2B:3C:4D:5E");
            osField.setText("Windows 11 Pro");
            ipField.setText("10.20.45.100");
        }

        form.add(createLabel("Assign to User:"));
        form.add(userCb);
        form.add(createLabel("Device Name / Model:"));
        form.add(nameField);
        form.add(createLabel("Device Type:"));
        form.add(typeCb);
        form.add(createLabel("Hardware Serial # (Unique):"));
        form.add(serialField);
        form.add(createLabel("MAC Address:"));
        form.add(macField);
        form.add(createLabel("Operating System:"));
        form.add(osField);
        form.add(createLabel("Assigned IP Address:"));
        form.add(ipField);
        form.add(createLabel("Device Status:"));
        form.add(statCb);
        form.add(createLabel("Investigation / Asset Notes:"));
        form.add(notesField);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBar.setOpaque(false);
        JButton saveBtn = Theme.createStyledButton(isEdit ? "Update Device" : "Register Device", true);
        JButton cancelBtn = Theme.createStyledButton("Cancel", false);
        cancelBtn.addActionListener(e -> dialog.dispose());

        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String serial = serialField.getText().trim();
            String mac = macField.getText().trim();
            String os = osField.getText().trim();
            String ip = ipField.getText().trim();
            String type = (String) typeCb.getSelectedItem();
            String status = (String) statCb.getSelectedItem();
            String notes = notesField.getText().trim();
            User selUser = (User) userCb.getSelectedItem();

            if (name.isEmpty() || serial.isEmpty() || mac.isEmpty() || os.isEmpty() || selUser == null) {
                JOptionPane.showMessageDialog(dialog, "Please fill in all mandatory device specifications.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Duplicate Serial Number Prevention Check
            int excludeId = isEdit ? existing.getDeviceId() : -1;
            if (deviceDAO.isSerialExists(serial, excludeId)) {
                JOptionPane.showMessageDialog(dialog, "Duplicate Serial Number Detected!\nA device with serial '" + serial + "' is already registered.", "Integrity Violation", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (isEdit) {
                existing.setUserId(selUser.getUserId());
                existing.setOwnerName(selUser.getFullName());
                existing.setDeviceName(name);
                existing.setDeviceType(type);
                existing.setSerialNumber(serial);
                existing.setMacAddress(mac);
                existing.setOperatingSystem(os);
                existing.setIpAddress(ip);
                existing.setStatus(status);
                existing.setNotes(notes);

                boolean ok = deviceDAO.updateDevice(existing);
                if (ok) {
                    dialog.dispose();
                    refreshTable();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to update device record.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                Device nd = new Device();
                nd.setUserId(selUser.getUserId());
                nd.setOwnerName(selUser.getFullName());
                nd.setDeviceName(name);
                nd.setDeviceType(type);
                nd.setSerialNumber(serial);
                nd.setMacAddress(mac);
                nd.setOperatingSystem(os);
                nd.setIpAddress(ip);
                nd.setStatus(status);
                nd.setNotes(notes);

                boolean ok = deviceDAO.addDevice(nd);
                if (ok) {
                    dialog.dispose();
                    refreshTable();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to register device.", "Error", JOptionPane.ERROR_MESSAGE);
                }
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
