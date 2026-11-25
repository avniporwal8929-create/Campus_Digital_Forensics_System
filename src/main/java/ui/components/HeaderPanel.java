package ui.components;

import config.DBConnection;
import services.AuthService;
import ui.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

public class HeaderPanel extends JPanel {
    private final JLabel statusLabel;
    private final JLabel userLabel;
    private final JButton dbConfigBtn;
    private final JButton logoutBtn;

    public HeaderPanel(Runnable onLogout, Runnable onOpenDbConfig) {
        setLayout(new BorderLayout());
        setBackground(Theme.BG_DARK);
        setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, Theme.BORDER_COLOR),
            new EmptyBorder(12, 24, 12, 24)
        ));

        // Left Branding
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        brandPanel.setOpaque(false);

        JLabel logoIcon = new JLabel("🛡️");
        logoIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBlock.setOpaque(false);

        JLabel titleLabel = new JLabel("CAMPUS DIGITAL FORENSICS SYSTEM (CDF)");
        titleLabel.setFont(Theme.FONT_TITLE);
        titleLabel.setForeground(Theme.ACCENT_CYAN);

        JLabel subLabel = new JLabel("Incident Response & Evidence Chain-of-Custody Management");
        subLabel.setFont(Theme.FONT_SMALL);
        subLabel.setForeground(Theme.TEXT_MUTED);

        titleBlock.add(titleLabel);
        titleBlock.add(subLabel);

        brandPanel.add(logoIcon);
        brandPanel.add(titleBlock);

        // Right User & Status Area
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightPanel.setOpaque(false);

        // Database status pill
        statusLabel = new JLabel();
        statusLabel.setFont(Theme.FONT_SMALL);
        statusLabel.setOpaque(true);
        statusLabel.setBorder(new CompoundBorder(
            new LineBorder(Theme.BORDER_COLOR, 1, true),
            new EmptyBorder(4, 10, 4, 10)
        ));
        updateDbStatus();

        // User badge
        userLabel = new JLabel();
        userLabel.setFont(Theme.FONT_BOLD);
        userLabel.setForeground(Theme.TEXT_PRIMARY);
        updateUserInfo();

        dbConfigBtn = Theme.createStyledButton("⚙ Database", false);
        dbConfigBtn.addActionListener(e -> onOpenDbConfig.run());

        logoutBtn = Theme.createStyledButton("🚪 Logout", false);
        logoutBtn.addActionListener(e -> onLogout.run());

        rightPanel.add(statusLabel);
        rightPanel.add(userLabel);
        rightPanel.add(dbConfigBtn);
        rightPanel.add(logoutBtn);

        add(brandPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);
    }

    public void updateDbStatus() {
        boolean live = DBConnection.isLiveDatabaseConnected();
        if (live) {
            statusLabel.setText("● MySQL 8.0: LIVE");
            statusLabel.setBackground(new Color(6, 78, 59));
            statusLabel.setForeground(new Color(52, 211, 153));
            statusLabel.setToolTipText("Connected directly to live MySQL relational database");
        } else {
            statusLabel.setText("▲ MySQL 8.0: OFFLINE (Demo Store)");
            statusLabel.setBackground(new Color(120, 53, 15));
            statusLabel.setForeground(new Color(251, 191, 36));
            statusLabel.setToolTipText("MySQL offline. Using simulated relational dataset. Click 'Database' to connect.");
        }
    }

    public void updateUserInfo() {
        if (AuthService.getInstance().isLoggedIn()) {
            userLabel.setText("👤 " + AuthService.getInstance().getCurrentUser().getFullName() + 
                              " [" + AuthService.getInstance().getCurrentUser().getRole() + "]");
        } else {
            userLabel.setText("👤 Not Authenticated");
        }
    }
}
