package ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Modern Cybersecurity Dark Navy & Electric Blue UI Theme.
 */
public class Theme {
    // Color Palette
    public static final Color BG_DARK        = new Color(13, 27, 42);   // #0D1B2A Main Frame Background
    public static final Color BG_SIDEBAR     = new Color(10, 19, 30);   // #0A131E Sidebar Background
    public static final Color BG_PANEL       = new Color(19, 34, 53);   // #132235 Main Content Background
    public static final Color BG_CARD        = new Color(26, 44, 68);   // #1A2C44 Card Surfaces
    public static final Color BG_CARD_HOVER  = new Color(34, 57, 88);
    public static final Color BG_INPUT       = new Color(15, 26, 40);   // #0F1A28 Fields
    public static final Color BORDER_COLOR   = new Color(42, 65, 96);   // #2A4160 Subtle Dark Border
    public static final Color BORDER_ACTIVE  = new Color(0, 229, 255);  // Electric Accent Border

    public static final Color ACCENT_CYAN    = new Color(0, 229, 255);  // #00E5FF Electric Accent
    public static final Color ACCENT_BLUE    = new Color(37, 99, 235);  // #2563EB Primary Action Blue
    public static final Color ACCENT_HOVER   = new Color(59, 130, 246);

    public static final Color TEXT_PRIMARY   = new Color(248, 250, 252);// White
    public static final Color TEXT_SECONDARY = new Color(203, 213, 225);// Soft Light Grey
    public static final Color TEXT_MUTED     = new Color(148, 163, 184);// Slate Grey

    // Status Indicator Colors
    public static final Color STATUS_SUCCESS = new Color(16, 185, 129); // Green
    public static final Color STATUS_WARNING = new Color(245, 158, 11); // Amber
    public static final Color STATUS_DANGER  = new Color(239, 68, 68);  // Red
    public static final Color STATUS_CRITICAL= new Color(225, 29, 72);  // Crimson
    public static final Color STATUS_INFO    = new Color(14, 165, 233); // Sky Blue

    // Fonts
    public static final Font FONT_HEADER_LARGE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADER_MED   = new Font("Segoe UI", Font.BOLD, 17);
    public static final Font FONT_TITLE        = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_REGULAR      = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD         = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL        = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO         = new Font("Consolas", Font.PLAIN, 12);

    public static void applyGlobalTheme() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        UIManager.put("Panel.background", BG_PANEL);
        UIManager.put("OptionPane.background", BG_DARK);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
        UIManager.put("OptionPane.messageFont", FONT_REGULAR);
        UIManager.put("ToolTip.background", BG_CARD);
        UIManager.put("ToolTip.foreground", TEXT_PRIMARY);
        UIManager.put("ToolTip.border", new LineBorder(ACCENT_CYAN, 1));
        UIManager.put("ScrollBar.thumb", BORDER_COLOR);
        UIManager.put("ScrollBar.track", BG_DARK);
    }

    public static JButton createStyledButton(String text, boolean primary) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        Color normalBg = primary ? ACCENT_BLUE : BG_CARD;
        Color hoverBg = primary ? ACCENT_HOVER : BG_CARD_HOVER;
        Color fg = primary ? Color.WHITE : TEXT_PRIMARY;

        btn.setBackground(normalBg);
        btn.setForeground(fg);
        btn.setBorder(new CompoundBorder(
            new LineBorder(primary ? ACCENT_CYAN : BORDER_COLOR, 1, true),
            new EmptyBorder(8, 16, 8, 16)
        ));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(hoverBg);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(normalBg);
            }
        });

        return btn;
    }

    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBackground(new Color(153, 27, 27));
        btn.setForeground(Color.WHITE);
        btn.setBorder(new CompoundBorder(
            new LineBorder(STATUS_DANGER, 1, true),
            new EmptyBorder(8, 16, 8, 16)
        ));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(STATUS_DANGER);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(153, 27, 27));
            }
        });
        return btn;
    }

    public static JTextField createStyledTextField() {
        JTextField tf = new JTextField();
        tf.setFont(FONT_REGULAR);
        tf.setBackground(BG_INPUT);
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(ACCENT_CYAN);
        tf.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(6, 10, 6, 10)
        ));
        return tf;
    }

    public static JPasswordField createStyledPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(FONT_REGULAR);
        pf.setBackground(BG_INPUT);
        pf.setForeground(TEXT_PRIMARY);
        pf.setCaretColor(ACCENT_CYAN);
        pf.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(6, 10, 6, 10)
        ));
        return pf;
    }

    public static <T> JComboBox<T> createStyledComboBox(T[] items) {
        JComboBox<T> cb = new JComboBox<>(items);
        cb.setFont(FONT_REGULAR);
        cb.setBackground(BG_INPUT);
        cb.setForeground(TEXT_PRIMARY);
        cb.setBorder(new LineBorder(BORDER_COLOR, 1));
        return cb;
    }

    public static JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setBackground(BG_CARD);
        card.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(14, 16, 14, 16)
        ));
        return card;
    }

    public static void styleTable(JTable table) {
        table.setBackground(BG_PANEL);
        table.setForeground(TEXT_PRIMARY);
        table.setFont(FONT_REGULAR);
        table.setRowHeight(32);
        table.setGridColor(new Color(30, 48, 72));
        table.setShowVerticalLines(false);
        table.setSelectionBackground(new Color(30, 64, 105));
        table.setSelectionForeground(ACCENT_CYAN);

        JTableHeader header = table.getTableHeader();
        header.setBackground(BG_DARK);
        header.setForeground(TEXT_SECONDARY);
        header.setFont(FONT_BOLD);
        header.setBorder(new LineBorder(BORDER_COLOR, 1));
        header.setPreferredSize(new Dimension(header.getWidth(), 38));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    c.setBackground((row % 2 == 0) ? BG_PANEL : new Color(24, 40, 62));
                    c.setForeground(TEXT_PRIMARY);
                }
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return c;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(leftRenderer);
        }
    }

    public static JLabel createBadge(String text, Color bg, Color fg) {
        JLabel badge = new JLabel(text, SwingConstants.CENTER);
        badge.setFont(FONT_SMALL);
        badge.setOpaque(true);
        badge.setBackground(bg);
        badge.setForeground(fg);
        badge.setBorder(new EmptyBorder(3, 8, 3, 8));
        return badge;
    }
}
