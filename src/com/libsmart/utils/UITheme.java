package com.libsmart.utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/**
 * Bảng màu, font chữ và các thành phần giao diện dùng chung cho toàn bộ ứng dụng Lib-Smart.
 */
public final class UITheme {

    public static final Color BG_APP = new Color(244, 246, 250);
    public static final Color BG_CARD = Color.WHITE;
    public static final Color BG_INPUT_READONLY = new Color(241, 245, 249);
    public static final Color BG_SIDEBAR = new Color(24, 34, 48);
    public static final Color BG_SIDEBAR_BTN = new Color(36, 50, 68);
    public static final Color BG_SIDEBAR_BTN_HOVER = new Color(49, 68, 92);

    public static final Color PRIMARY = new Color(37, 99, 235);
    public static final Color SUCCESS = new Color(22, 163, 74);
    public static final Color WARNING = new Color(217, 119, 6);
    public static final Color DANGER = new Color(220, 38, 38);
    public static final Color MUTED = new Color(100, 116, 139);

    public static final Color TEXT_PRIMARY = new Color(30, 41, 59);
    public static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    public static final Color BORDER_COLOR = new Color(203, 213, 225);
    public static final Color TABLE_GRID = new Color(226, 232, 240);
    public static final Color TABLE_ALT_ROW = new Color(248, 250, 252);
    public static final Color TABLE_HEADER_BG = new Color(241, 245, 249);
    public static final Color TABLE_SELECTION_BG = new Color(219, 234, 254);
    public static final Color TABLE_SELECTION_FG = new Color(30, 58, 138);

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_INPUT = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_TABLE_HEADER = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_TABLE_CELL = new Font("Segoe UI", Font.PLAIN, 13);

    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,##0");

    private UITheme() {}

    public static String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "0 đ";
        }
        return CURRENCY_FORMAT.format(amount) + " đ";
    }

    public static JButton createButton(String text, Color bgColor, int width, int height) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BUTTON);
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(width, height));

        Color hoverColor = adjustBrightness(bgColor, 0.88f);
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(hoverColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bgColor);
            }
        });
        return btn;
    }

    public static JButton createSidebarButton(String text, Color bgColor, Color hoverColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(hoverColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bgColor);
            }
        });
        return btn;
    }

    public static JTextField createTextField(boolean editable) {
        JTextField tf = new JTextField();
        styleTextField(tf, editable);
        return tf;
    }

    public static void styleTextField(JTextField tf, boolean editable) {
        tf.setFont(FONT_INPUT);
        tf.setPreferredSize(new Dimension(200, 32));
        tf.setEditable(editable);
        tf.setForeground(TEXT_PRIMARY);
        tf.setBackground(editable ? Color.WHITE : BG_INPUT_READONLY);

        Border normalBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(4, 10, 4, 10));
        Border focusBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY, 2),
                BorderFactory.createEmptyBorder(3, 9, 3, 9));
        tf.setBorder(normalBorder);

        if (editable) {
            tf.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    tf.setBorder(focusBorder);
                }

                @Override
                public void focusLost(FocusEvent e) {
                    tf.setBorder(normalBorder);
                }
            });
        }
    }

    public static void styleComboBox(JComboBox<String> cb) {
        cb.setFont(FONT_INPUT);
        cb.setPreferredSize(new Dimension(200, 32));
        cb.setBackground(Color.WHITE);
        cb.setForeground(TEXT_PRIMARY);
    }

    public static JPanel createTitledCard(String title) {
        JPanel panel = new JPanel();
        panel.setBackground(BG_CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR, 1),
                        " " + title + " ",
                        0, 0, FONT_SECTION, TEXT_PRIMARY),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        return panel;
    }

    public static void addFormCell(JPanel panel, GridBagConstraints gbc, String labelText, Component comp, int gridx, int gridy) {
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(TEXT_PRIMARY);

        gbc.gridx = gridx;
        gbc.gridy = gridy;
        gbc.weightx = 0.12;
        panel.add(lbl, gbc);

        gbc.gridx = gridx + 1;
        gbc.gridy = gridy;
        gbc.weightx = 0.38;
        panel.add(comp, gbc);
    }

    public static void styleTable(JTable table, JScrollPane scrollPane, int... centeredCols) {
        table.setRowHeight(32);
        table.setFont(FONT_TABLE_CELL);
        table.setForeground(TEXT_PRIMARY);
        table.setGridColor(TABLE_GRID);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(TABLE_SELECTION_BG);
        table.setSelectionForeground(TABLE_SELECTION_FG);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_TABLE_HEADER);
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(TEXT_PRIMARY);
        header.setPreferredSize(new Dimension(100, 36));
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer baseRenderer = createStripedRenderer(SwingConstants.LEFT);
        table.setDefaultRenderer(Object.class, baseRenderer);

        DefaultTableCellRenderer centerRenderer = createStripedRenderer(SwingConstants.CENTER);
        for (int col : centeredCols) {
            if (col >= 0 && col < table.getColumnModel().getColumnCount()) {
                table.getColumnModel().getColumn(col).setCellRenderer(centerRenderer);
            }
        }

        if (scrollPane != null) {
            scrollPane.setViewportView(table);
            scrollPane.getViewport().setBackground(Color.WHITE);
            scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        }
    }

    public static DefaultTableCellRenderer createStripedRenderer(int alignment) {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(alignment);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                if (isSelected) {
                    setBackground(TABLE_SELECTION_BG);
                    setForeground(TABLE_SELECTION_FG);
                } else {
                    setBackground(row % 2 == 0 ? Color.WHITE : TABLE_ALT_ROW);
                    setForeground(TEXT_PRIMARY);
                }
                return this;
            }
        };
    }

    public static DefaultTableCellRenderer createStatusRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(FONT_BUTTON);
                String text = value != null ? value.toString() : "";

                if (isSelected) {
                    setBackground(TABLE_SELECTION_BG);
                    setForeground(TABLE_SELECTION_FG);
                    return this;
                }

                setBackground(row % 2 == 0 ? Color.WHITE : TABLE_ALT_ROW);
                if ("Đã trả".equalsIgnoreCase(text) || "Hoạt động".equalsIgnoreCase(text)) {
                    setForeground(SUCCESS);
                } else if ("Đang mượn".equalsIgnoreCase(text)) {
                    setForeground(WARNING);
                } else if (text.contains("Quá hạn") || text.contains("Hết hạn") || text.contains("Khóa")) {
                    setForeground(DANGER);
                } else {
                    setForeground(TEXT_PRIMARY);
                }
                return this;
            }
        };
    }

    private static Color adjustBrightness(Color c, float factor) {
        int r = Math.max(0, Math.min(255, Math.round(c.getRed() * factor)));
        int g = Math.max(0, Math.min(255, Math.round(c.getGreen() * factor)));
        int b = Math.max(0, Math.min(255, Math.round(c.getBlue() * factor)));
        return new Color(r, g, b);
    }
}
