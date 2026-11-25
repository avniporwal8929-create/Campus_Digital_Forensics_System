package ui.components;

import ui.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SidebarPanel extends JPanel {
    private final Consumer<String> onSelectModule;
    private final List<JButton> navButtons = new ArrayList<>();
    private String currentModule = "DASHBOARD";

    public SidebarPanel(Consumer<String> onSelectModule) {
        this.onSelectModule = onSelectModule;
        setLayout(new BorderLayout());
        setBackground(Theme.BG_SIDEBAR);
        setPreferredSize(new Dimension(240, 0));
        setBorder(new MatteBorder(0, 0, 0, 1, Theme.BORDER_COLOR));

        // Top Navigation Menu
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setOpaque(false);
        menuPanel.setBorder(new EmptyBorder(16, 10, 16, 10));

        JLabel navTitle = new JLabel("CORE FORENSICS MODULES");
        navTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        navTitle.setForeground(Theme.TEXT_MUTED);
        navTitle.setBorder(new EmptyBorder(6, 12, 10, 12));
        menuPanel.add(navTitle);

        addNavButton(menuPanel, "📊 Dashboard", "DASHBOARD");
        addNavButton(menuPanel, "👥 1. User Management", "USERS");
        addNavButton(menuPanel, "💻 2. Device Registry", "DEVICES");
        addNavButton(menuPanel, "🚨 3. Incidents & Cases", "INCIDENTS");
        addNavButton(menuPanel, "📋 4. Event & Access Logs", "LOGS");
        addNavButton(menuPanel, "🔬 5. Evidence Locker", "EVIDENCE");
        addNavButton(menuPanel, "⛓️ 6. Chain of Custody", "CUSTODY");
        addNavButton(menuPanel, "🕵️ 7. Investigators", "INVESTIGATORS");
        addNavButton(menuPanel, "📈 8. Reports & Analytics", "REPORTS");
        addNavButton(menuPanel, "🔗 9. Investigation Correlation", "CORRELATION");

        JScrollPane scrollPane = new JScrollPane(menuPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        // Bottom system footer
        JPanel bottomPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(12, 16, 14, 16));

        JLabel verLbl = new JLabel("CDF Engine v1.0.0");
        verLbl.setFont(Theme.FONT_SMALL);
        verLbl.setForeground(Theme.TEXT_MUTED);

        JLabel envLbl = new JLabel("Java 21 • MySQL 8.0");
        envLbl.setFont(Theme.FONT_SMALL);
        envLbl.setForeground(Theme.ACCENT_CYAN);

        bottomPanel.add(verLbl);
        bottomPanel.add(envLbl);

        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        setActiveButton("DASHBOARD");
    }

    private void addNavButton(JPanel container, String text, String moduleKey) {
        JButton btn = new JButton(text);
        btn.setFont(Theme.FONT_BOLD);
        btn.setForeground(Theme.TEXT_SECONDARY);
        btn.setBackground(Theme.BG_SIDEBAR);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setPreferredSize(new Dimension(210, 40));
        btn.setBorder(new CompoundBorder(
            new MatteBorder(0, 3, 0, 0, new Color(0, 0, 0, 0)),
            new EmptyBorder(8, 12, 8, 12)
        ));

        btn.addActionListener(e -> {
            setActiveButton(moduleKey);
            onSelectModule.accept(moduleKey);
        });

        navButtons.add(btn);
        btn.putClientProperty("MODULE_KEY", moduleKey);
        container.add(btn);
        container.add(Box.createVerticalStrut(4));
    }

    public void setActiveButton(String moduleKey) {
        this.currentModule = moduleKey;
        for (JButton btn : navButtons) {
            String key = (String) btn.getClientProperty("MODULE_KEY");
            if (moduleKey.equalsIgnoreCase(key)) {
                btn.setBackground(new Color(23, 40, 63));
                btn.setForeground(Theme.ACCENT_CYAN);
                btn.setBorder(new CompoundBorder(
                    new MatteBorder(0, 4, 0, 0, Theme.ACCENT_CYAN),
                    new EmptyBorder(8, 11, 8, 12)
                ));
            } else {
                btn.setBackground(Theme.BG_SIDEBAR);
                btn.setForeground(Theme.TEXT_SECONDARY);
                btn.setBorder(new CompoundBorder(
                    new MatteBorder(0, 3, 0, 0, new Color(0, 0, 0, 0)),
                    new EmptyBorder(8, 12, 8, 12)
                ));
            }
        }
    }
}
