package com.library.gui;

import javax.swing.*;
import java.awt.*;

/**
 * MainGui — Cửa sổ chính của ứng dụng Dashboard Thư viện Học liệu.
 * Khởi động bằng: .\run_gui.bat hoặc .\mvn.cmd exec:java -Pgui
 */
public class MainGui extends JFrame {

    // ===== Màu sắc chủ đạo =====
    static final Color BG_MAIN    = new Color(0xF8FAFC);
    static final Color ACCENT     = new Color(0x1E40AF);
    static final Color SUCCESS    = new Color(0x16A34A);
    static final Color DANGER     = new Color(0xDC2626);
    static final Color WARNING    = new Color(0xD97706);
    static final Color ROW_ODD    = Color.WHITE;
    static final Color ROW_EVEN   = new Color(0xF1F5F9);
    static final Color HEADER_BG  = new Color(0xDBEAFE);
    static final Color HEADER_FG  = new Color(0x1E3A8A);
    static final Color BORDER_COL = new Color(0xBFDBFE);

    // ===== Font =====
    static final Font FONT_BODY   = new Font("Segoe UI", Font.PLAIN,  13);
    static final Font FONT_BOLD   = new Font("Segoe UI", Font.BOLD,   13);
    static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD,   15);
    static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD,   18);
    static final Font FONT_MONO   = new Font("Consolas",  Font.PLAIN,  12);

    private JTabbedPane tabs;

    public JTabbedPane getTabs() { return tabs; }

    public MainGui() {
        super("Dashboard Thư viện Học liệu — CSDL Nâng cao Đề tài 3");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1150, 730);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setBackground(BG_MAIN);

        // Áp dụng Look & Feel hệ thống
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }

        buildUI();
    }

    private void buildUI() {
        // ---- Header ----
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setBackground(ACCENT);
        header.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel("CSDL Nâng cao — Đề tài 3: Thư viện Học liệu Đa phương tiện");
        title.setFont(FONT_TITLE);
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Bảng điều khiển thực nghiệm (GUI Dashboard)");
        subtitle.setFont(FONT_BODY);
        subtitle.setForeground(new Color(0xBFDBFE));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        titlePanel.setOpaque(false);
        titlePanel.add(title);
        titlePanel.add(subtitle);
        header.add(titlePanel, BorderLayout.WEST);

        // ---- Tabs ----
        tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(FONT_BOLD);
        tabs.setBackground(BG_MAIN);

        tabs.addTab("1. 4NF & Kế thừa",          new PanelNormalization());
        tabs.addTab("2. Giao dịch Đồng thời",      new PanelConcurrency());
        tabs.addTab("3. GIN Trigram Benchmark",   new PanelGinBenchmark());
        tabs.addTab("4. ObjectDB & OQL/JPQL",     new PanelObjectDB());
        tabs.addTab("5. Cây Chủ đề (Đệ quy)",     new PanelSubjectTree());

        // ---- Footer ----
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 6));
        footer.setBackground(new Color(0xE2E8F0));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COL));
        JLabel footerLabel = new JLabel("CSDL Nâng cao — Nhóm Đề tài 3 · Java " +
                System.getProperty("java.version") + " · PostgreSQL 16 · ObjectDB 2.9");
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footerLabel.setForeground(new Color(0x64748B));
        footer.add(footerLabel);

        // ---- Layout tổng thể ----
        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        add(tabs,   BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    // ===== Tiện ích dùng chung =====

    /** Tạo nút bấm custom luôn giữ màu nền đậm và chữ trắng rõ nét trên mọi Look & Feel */
    public static JButton makeButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (!isEnabled()) {
                    g2.setColor(new Color(0x94A3B8));
                } else if (getModel().isPressed()) {
                    g2.setColor(bg.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bg.brighter());
                } else {
                    g2.setColor(bg);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setFont(FONT_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg.darker(), 1, true),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /** Tạo JScrollPane bọc JTextArea output */
    public static JScrollPane makeOutputArea(JTextArea area) {
        area.setFont(FONT_MONO);
        area.setEditable(false);
        area.setBackground(new Color(0x0F172A));
        area.setForeground(new Color(0xE2E8F0));
        area.setCaretColor(Color.WHITE);
        area.setLineWrap(true);
        area.setWrapStyleWord(false);
        area.setMargin(new Insets(10, 12, 10, 12));
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_COL));
        return scroll;
    }

    /** Tạo label tiêu đề section */
    public static JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_HEADER);
        lbl.setForeground(ACCENT);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        return lbl;
    }

    /** Tạo JTable với style đồng nhất */
    public static JTable makeStyledTable(String[] columns, Object[][] data) {
        JTable table = new JTable(data, columns) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                c.setBackground(row % 2 == 0 ? ROW_ODD : ROW_EVEN);
                c.setFont(FONT_BODY);
                if (isRowSelected(row)) c.setBackground(new Color(0xDDEEFF));
                return c;
            }
        };
        table.setFont(FONT_BODY);
        table.setRowHeight(26);
        table.setShowGrid(true);
        table.setGridColor(BORDER_COL);
        table.setSelectionBackground(new Color(0xDDEEFF));

        // Header style
        table.getTableHeader().setFont(FONT_BOLD);
        table.getTableHeader().setBackground(HEADER_BG);
        table.getTableHeader().setForeground(HEADER_FG);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER_COL));

        return table;
    }

    // ===== Entry point =====
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainGui().setVisible(true);
        });
    }
}
