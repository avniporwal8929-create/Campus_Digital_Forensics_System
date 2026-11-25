package ui.components;

import config.AppConfig;
import config.DBConnection;
import ui.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DbConfigDialog extends JDialog {
    private final JTextField hostField;
    private final JTextField portField;
    private final JTextField dbField;
    private final JTextField userField;
    private final JPasswordField passField;
    private final JLabel resultLabel;
    private final Runnable onSuccessCallback;

    public DbConfigDialog(Frame owner, Runnable onSuccessCallback) {
        super(owner, "Database Connection Settings - MySQL 8.0", true);
        this.onSuccessCallback = onSuccessCallback;

        setSize(480, 440);
        setLocationRelativeTo(owner);
        setResizable(false);

        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setBackground(Theme.BG_DARK);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        headerPanel.setOpaque(false);
        JLabel title = new JLabel("MySQL Database Configuration");
        title.setFont(Theme.FONT_HEADER_MED);
        title.setForeground(Theme.ACCENT_CYAN);

        JLabel desc = new JLabel("Configure connection settings for 'campus_digital_forensics'");
        desc.setFont(Theme.FONT_SMALL);
        desc.setForeground(Theme.TEXT_MUTED);
        headerPanel.add(title);
        headerPanel.add(desc);

        // Form Fields
        JPanel form = new JPanel(new GridLayout(5, 2, 10, 12));
        form.setOpaque(false);

        form.add(createFieldLabel("Host:"));
        hostField = Theme.createStyledTextField();
        hostField.setText(AppConfig.getHost());
        form.add(hostField);

        form.add(createFieldLabel("Port:"));
        portField = Theme.createStyledTextField();
        portField.setText(String.valueOf(AppConfig.getPort()));
        form.add(portField);

        form.add(createFieldLabel("Database Name:"));
        dbField = Theme.createStyledTextField();
        dbField.setText(AppConfig.getDatabase());
        form.add(dbField);

        form.add(createFieldLabel("Username:"));
        userField = Theme.createStyledTextField();
        userField.setText(AppConfig.getUser());
        form.add(userField);

        form.add(createFieldLabel("Password:"));
        passField = Theme.createStyledPasswordField();
        passField.setText(AppConfig.getPassword());
        form.add(passField);

        // Result / Feedback label
        resultLabel = new JLabel(" ");
        resultLabel.setFont(Theme.FONT_SMALL);
        resultLabel.setForeground(Theme.TEXT_MUTED);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setOpaque(false);
        centerPanel.add(form, BorderLayout.CENTER);
        centerPanel.add(resultLabel, BorderLayout.SOUTH);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        JButton testBtn = Theme.createStyledButton("Test Connection", false);
        testBtn.addActionListener(e -> testConnection());

        JButton saveBtn = Theme.createStyledButton("Save & Connect", true);
        saveBtn.addActionListener(e -> saveAndConnect());

        JButton cancelBtn = Theme.createStyledButton("Cancel", false);
        cancelBtn.addActionListener(e -> dispose());

        btnPanel.add(testBtn);
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(panel);
    }

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.FONT_BOLD);
        lbl.setForeground(Theme.TEXT_SECONDARY);
        return lbl;
    }

    private void testConnection() {
        applyFields();
        boolean ok = DBConnection.testConnection();
        if (ok) {
            resultLabel.setText("✔ Connection successful to MySQL Server!");
            resultLabel.setForeground(Theme.STATUS_SUCCESS);
        } else {
            resultLabel.setText("✖ " + DBConnection.getLastErrorMessage());
            resultLabel.setForeground(Theme.STATUS_DANGER);
        }
    }

    private void saveAndConnect() {
        applyFields();
        boolean ok = DBConnection.testConnection();
        if (ok) {
            JOptionPane.showMessageDialog(this, "Connected successfully to live MySQL database!", "Database Connected", JOptionPane.INFORMATION_MESSAGE);
            if (onSuccessCallback != null) onSuccessCallback.run();
            dispose();
        } else {
            int choice = JOptionPane.showConfirmDialog(
                this,
                "Could not connect to MySQL: " + DBConnection.getLastErrorMessage() + "\n\nDo you want to save credentials anyway?",
                "Connection Notice",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            if (choice == JOptionPane.YES_OPTION) {
                if (onSuccessCallback != null) onSuccessCallback.run();
                dispose();
            }
        }
    }

    private void applyFields() {
        int port = 3306;
        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException ignored) {}

        AppConfig.updateCredentials(
            hostField.getText().trim(),
            port,
            dbField.getText().trim(),
            userField.getText().trim(),
            new String(passField.getPassword())
        );
    }
}
