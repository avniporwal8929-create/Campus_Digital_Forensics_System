package ui;

import config.DBConnection;
import services.AuthService;
import ui.components.DbConfigDialog;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class LoginFrame extends JFrame {
    private final JTextField userField;
    private final JPasswordField passField;
    private final JLabel errorLabel;
    private final JLabel dbStatusLabel;

    public LoginFrame() {
        super("Campus Digital Forensics System - Secure Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 560);
        setResizable(false);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Theme.BG_DARK);
        mainPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        JLabel iconLabel = new JLabel("🛡️");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 46));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("CAMPUS DIGITAL FORENSICS");
        titleLabel.setFont(Theme.FONT_HEADER_MED);
        titleLabel.setForeground(Theme.ACCENT_CYAN);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLabel = new JLabel("Digital Security Incident & Evidence Vault");
        subLabel.setFont(Theme.FONT_SMALL);
        subLabel.setForeground(Theme.TEXT_MUTED);
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(iconLabel);
        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(subLabel);
        headerPanel.add(Box.createVerticalStrut(24));

        // Form Card Panel
        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBackground(Theme.BG_CARD);
        cardPanel.setBorder(new CompoundBorder(
            new LineBorder(Theme.BORDER_COLOR, 1, true),
            new EmptyBorder(22, 22, 22, 22)
        ));

        JLabel userLbl = new JLabel("Username or Campus Email");
        userLbl.setFont(Theme.FONT_BOLD);
        userLbl.setForeground(Theme.TEXT_SECONDARY);
        userLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        userField = Theme.createStyledTextField();
        userField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        userField.setText("admin"); // convenient demo default

        JLabel passLbl = new JLabel("Password");
        passLbl.setFont(Theme.FONT_BOLD);
        passLbl.setForeground(Theme.TEXT_SECONDARY);
        passLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        passField = Theme.createStyledPasswordField();
        passField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        passField.setText("admin123"); // convenient demo default

        errorLabel = new JLabel(" ");
        errorLabel.setFont(Theme.FONT_SMALL);
        errorLabel.setForeground(Theme.STATUS_DANGER);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton loginBtn = Theme.createStyledButton("AUTHENTICATE & ENTER", true);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.addActionListener(e -> performLogin());

        // Enter key listeners
        KeyAdapter enterKey = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) performLogin();
            }
        };
        userField.addKeyListener(enterKey);
        passField.addKeyListener(enterKey);

        cardPanel.add(userLbl);
        cardPanel.add(Box.createVerticalStrut(6));
        cardPanel.add(userField);
        cardPanel.add(Box.createVerticalStrut(14));
        cardPanel.add(passLbl);
        cardPanel.add(Box.createVerticalStrut(6));
        cardPanel.add(passField);
        cardPanel.add(Box.createVerticalStrut(8));
        cardPanel.add(errorLabel);
        cardPanel.add(Box.createVerticalStrut(12));
        cardPanel.add(loginBtn);

        // Footer & Quick Credentials Hint
        JPanel footerPanel = new JPanel();
        footerPanel.setLayout(new BoxLayout(footerPanel, BoxLayout.Y_AXIS));
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(16, 0, 0, 0));

        dbStatusLabel = new JLabel();
        dbStatusLabel.setFont(Theme.FONT_SMALL);
        dbStatusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        updateDbLabel();

        JButton dbBtn = new JButton("⚙ Configure MySQL Connection");
        dbBtn.setFont(Theme.FONT_SMALL);
        dbBtn.setForeground(Theme.ACCENT_CYAN);
        dbBtn.setContentAreaFilled(false);
        dbBtn.setBorderPainted(false);
        dbBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        dbBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        dbBtn.addActionListener(e -> new DbConfigDialog(this, this::updateDbLabel).setVisible(true));

        JLabel hintLbl = new JLabel("Demo Logins: admin (admin123) • inv_marcus (password123)");
        hintLbl.setFont(Theme.FONT_SMALL);
        hintLbl.setForeground(Theme.TEXT_MUTED);
        hintLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        footerPanel.add(dbStatusLabel);
        footerPanel.add(Box.createVerticalStrut(4));
        footerPanel.add(dbBtn);
        footerPanel.add(Box.createVerticalStrut(6));
        footerPanel.add(hintLbl);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(cardPanel, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void updateDbLabel() {
        boolean live = DBConnection.testConnection();
        if (live) {
            dbStatusLabel.setText("● MySQL 8.0 Connected (Live DB Active)");
            dbStatusLabel.setForeground(Theme.STATUS_SUCCESS);
        } else {
            dbStatusLabel.setText("▲ MySQL Offline - Fallback Demonstration Mode Active");
            dbStatusLabel.setForeground(Theme.STATUS_WARNING);
        }
    }

    private void performLogin() {
        String username = userField.getText().trim();
        String password = new String(passField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter both username and password.");
            return;
        }

        boolean ok = AuthService.getInstance().login(username, password);
        if (ok) {
            errorLabel.setText(" ");
            dispose();
            SwingUtilities.invokeLater(() -> new DashboardFrame().setVisible(true));
        } else {
            errorLabel.setText("Authentication failed. Invalid credentials or inactive user.");
        }
    }
}
