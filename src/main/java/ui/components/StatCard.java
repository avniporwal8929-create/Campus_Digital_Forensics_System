package ui.components;

import ui.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class StatCard extends JPanel {

    public StatCard(String title, String value, String icon, Color accentColor, String footerText) {
        setLayout(new BorderLayout(8, 8));
        setBackground(Theme.BG_CARD);
        setBorder(new CompoundBorder(
            new LineBorder(Theme.BORDER_COLOR, 1, true),
            new EmptyBorder(14, 18, 14, 18)
        ));

        // Top bar: title and icon
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(Theme.FONT_BOLD);
        titleLbl.setForeground(Theme.TEXT_MUTED);

        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));

        topPanel.add(titleLbl, BorderLayout.WEST);
        topPanel.add(iconLbl, BorderLayout.EAST);

        // Center: big value
        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valLbl.setForeground(accentColor);

        // Bottom: footer
        JLabel footLbl = new JLabel(footerText);
        footLbl.setFont(Theme.FONT_SMALL);
        footLbl.setForeground(Theme.TEXT_SECONDARY);

        add(topPanel, BorderLayout.NORTH);
        add(valLbl, BorderLayout.CENTER);
        add(footLbl, BorderLayout.SOUTH);
    }
}
