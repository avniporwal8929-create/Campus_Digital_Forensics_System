package ui.components;

import ui.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.Map;

public class SimpleChartPanel extends JPanel {
    private final String chartTitle;
    private Map<String, Integer> data;
    private final Color[] barColors;

    public SimpleChartPanel(String chartTitle, Map<String, Integer> data, Color[] barColors) {
        this.chartTitle = chartTitle;
        this.data = data;
        this.barColors = (barColors != null && barColors.length > 0) ? barColors : new Color[] {
            Theme.ACCENT_CYAN, Theme.ACCENT_BLUE, Theme.STATUS_WARNING, Theme.STATUS_CRITICAL, Theme.STATUS_SUCCESS
        };

        setBackground(Theme.BG_CARD);
        setBorder(new CompoundBorder(
            new LineBorder(Theme.BORDER_COLOR, 1, true),
            new EmptyBorder(14, 16, 14, 16)
        ));
        setPreferredSize(new Dimension(360, 240));
    }

    public void updateData(Map<String, Integer> newData) {
        this.data = newData;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Title
        g2.setFont(Theme.FONT_BOLD);
        g2.setColor(Theme.TEXT_PRIMARY);
        g2.drawString(chartTitle, 16, 26);

        if (data == null || data.isEmpty()) {
            g2.setFont(Theme.FONT_REGULAR);
            g2.setColor(Theme.TEXT_MUTED);
            g2.drawString("No data available to display", 20, height / 2);
            g2.dispose();
            return;
        }

        // Calculate max value
        int maxVal = 1;
        int totalSum = 0;
        for (int v : data.values()) {
            if (v > maxVal) maxVal = v;
            totalSum += v;
        }

        int startY = 48;
        int usableHeight = height - startY - 14;
        int rowHeight = Math.max(28, usableHeight / Math.max(1, data.size()));
        int barStartX = 150;
        int maxBarWidth = width - barStartX - 70;

        int index = 0;
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            int y = startY + (index * rowHeight);
            int val = entry.getValue();
            int barW = (int) (((double) val / maxVal) * maxBarWidth);
            if (barW < 4 && val > 0) barW = 4;

            Color barColor = barColors[index % barColors.length];

            // Label
            g2.setFont(Theme.FONT_SMALL);
            g2.setColor(Theme.TEXT_SECONDARY);
            String label = entry.getKey();
            if (label.length() > 18) label = label.substring(0, 16) + "..";
            g2.drawString(label, 16, y + 16);

            // Bar Background Track
            g2.setColor(Theme.BG_INPUT);
            g2.fillRoundRect(barStartX, y + 4, maxBarWidth, 14, 6, 6);

            // Gradient Bar
            GradientPaint gp = new GradientPaint(
                barStartX, y + 4, barColor,
                barStartX + barW, y + 18, barColor.darker()
            );
            g2.setPaint(gp);
            g2.fillRoundRect(barStartX, y + 4, barW, 14, 6, 6);

            // Value text
            g2.setFont(Theme.FONT_SMALL);
            g2.setColor(Theme.TEXT_PRIMARY);
            double pct = (totalSum > 0) ? ((double) val / totalSum * 100.0) : 0;
            String valStr = String.format("%d (%.0f%%)", val, pct);
            g2.drawString(valStr, barStartX + barW + 8, y + 16);

            index++;
        }

        g2.dispose();
    }
}
