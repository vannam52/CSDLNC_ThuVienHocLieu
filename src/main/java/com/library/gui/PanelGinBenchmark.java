package com.library.gui;

import com.library.config.DatabaseConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;
import java.util.*;
import java.util.List;

/**
 * Tab 3 — Thực nghiệm hiệu năng GIN Trigram.
 * Hiển thị:
 *   - Thời gian thực thi trước/sau khi tạo chỉ mục GIN (5 lần đo)
 *   - Biểu đồ cột so sánh trung vị
 *   - Bảng số liệu chi tiết
 */
public class PanelGinBenchmark extends JPanel {

    private static final int SAMPLE_SIZE = 20_000;
    private static final int REPEATS = 5;
    private static final String QUERY =
            "SELECT MaHocLieu, TieuDe FROM demo_gin WHERE TieuDe ILIKE '%Database%'";

    // Dữ liệu đo được
    private double medianBefore = 0, medianAfter = 0;
    private List<Double> timesBefore = new ArrayList<>();
    private List<Double> timesAfter  = new ArrayList<>();
    private String scanTypeBefore = "", scanTypeAfter = "";

    private final JLabel lblStatus = new JLabel("Nhấn nút để bắt đầu benchmark.");
    private final JLabel lblResult = new JLabel(" ");
    private final BarChartPanel chartPanel = new BarChartPanel();
    private final JTextArea areaDetail = new JTextArea();
    private JButton btnRun;

    public JButton getBtnRun() { return btnRun; }

    public PanelGinBenchmark() {
        super(new BorderLayout(0, 12));
        setBackground(MainGui.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        buildUI();
    }

    private void buildUI() {
        // Toolbar
        btnRun = MainGui.makeButton("Chạy GIN Trigram Benchmark (20.000 dòng)", MainGui.ACCENT);
        btnRun.addActionListener(e -> runBenchmark());

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        toolbar.setOpaque(false);
        toolbar.add(btnRun);
        lblStatus.setFont(MainGui.FONT_BOLD);
        lblStatus.setForeground(new Color(0x64748B));
        toolbar.add(lblStatus);

        // ---- Phần giữa: chart + detail ----
        JPanel midPanel = new JPanel(new GridLayout(1, 2, 14, 0));
        midPanel.setOpaque(false);

        // Chart card
        chartPanel.setPreferredSize(new Dimension(400, 280));
        JPanel chartCard = new JPanel(new BorderLayout(0, 8));
        chartCard.setBackground(Color.WHITE);
        chartCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainGui.BORDER_COL),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        JLabel chartTitle = new JLabel("Biểu đồ so sánh thời gian thực thi (ms) — Trung vị qua " + REPEATS + " lần chạy");
        chartTitle.setFont(MainGui.FONT_BOLD);
        chartTitle.setForeground(MainGui.ACCENT);
        lblResult.setFont(MainGui.FONT_BOLD);
        lblResult.setForeground(MainGui.SUCCESS);
        chartCard.add(chartTitle,  BorderLayout.NORTH);
        chartCard.add(chartPanel,  BorderLayout.CENTER);
        chartCard.add(lblResult,   BorderLayout.SOUTH);

        // Detail log
        areaDetail.setFont(MainGui.FONT_MONO);
        areaDetail.setEditable(false);
        areaDetail.setBackground(new Color(0x0F172A));
        areaDetail.setForeground(new Color(0xE2E8F0));
        areaDetail.setMargin(new Insets(10, 12, 10, 12));
        JScrollPane scrollDetail = new JScrollPane(areaDetail);
        scrollDetail.setBorder(BorderFactory.createLineBorder(MainGui.BORDER_COL));

        JPanel detailCard = new JPanel(new BorderLayout(0, 8));
        detailCard.setBackground(Color.WHITE);
        detailCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainGui.BORDER_COL),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        JLabel detailTitle = new JLabel("Chi tiết kết quả EXPLAIN ANALYZE (lần chạy đầu tiên)");
        detailTitle.setFont(MainGui.FONT_BOLD);
        detailTitle.setForeground(MainGui.ACCENT);
        detailCard.add(detailTitle,  BorderLayout.NORTH);
        detailCard.add(scrollDetail, BorderLayout.CENTER);

        midPanel.add(chartCard);
        midPanel.add(detailCard);

        // ---- Note ----
        JPanel notePanel = new JPanel(new GridLayout(1, 2, 14, 0));
        notePanel.setOpaque(false);
        notePanel.add(makeInfoBox("Trước khi tạo chỉ mục GIN",
                "Kiểu quét: Sequential Scan (Quét toàn bộ bảng)\n" +
                "• Duyệt tuần tự 20.000 dòng trong bộ nhớ/ổ đĩa\n" +
                "• Thời gian thực thi: ~15–25 ms\n" +
                "• Chi phí tăng tuyến tính theo kích thước bảng O(n)\n\n" +
                "Truy vấn ILIKE '%Database%' chứa ký tự đại diện ở đầu\n" +
                "nên chỉ mục B-Tree truyền thống không thể sử dụng.", new Color(0xFEF3C7), new Color(0xD97706), new Color(0xFDE68A)));
        notePanel.add(makeInfoBox("Sau khi tạo chỉ mục GIN Trigram",
                "Kiểu quét: Bitmap Index Scan / GIN\n" +
                "• Phân rã chuỗi thành các cụm 3 ký tự (trigrams)\n" +
                "• Thời gian thực thi: ~0.3–1.0 ms\n" +
                "• Tốc độ nhanh hơn 20–70 lần so với Seq Scan!\n\n" +
                "Lệnh: CREATE INDEX ... USING gin(TieuDe gin_trgm_ops)\n" +
                "Hỗ trợ tìm kiếm chuỗi con / toàn văn cực nhanh.", new Color(0xF0FDF4), MainGui.SUCCESS, new Color(0xBBF7D0)));

        // Layout
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.add(MainGui.sectionLabel("Thực nghiệm 3: Hiệu năng chỉ mục GIN Trigram — ILIKE '%Database%'"));
        content.add(Box.createVerticalStrut(8));
        content.add(midPanel);
        content.add(Box.createVerticalStrut(14));
        content.add(MainGui.sectionLabel("Phân tích cơ chế kỹ thuật"));
        content.add(Box.createVerticalStrut(8));
        content.add(notePanel);

        JScrollPane mainScroll = new JScrollPane(content);
        mainScroll.setBorder(null);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);

        add(toolbar, BorderLayout.NORTH);
        add(mainScroll, BorderLayout.CENTER);
    }

    private JPanel makeInfoBox(String title, String content, Color bg, Color fg, Color border) {
        JPanel box = new JPanel(new BorderLayout(0, 6));
        box.setBackground(bg);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        JLabel t = new JLabel(title);
        t.setFont(MainGui.FONT_BOLD);
        t.setForeground(fg);
        JTextArea txt = new JTextArea(content);
        txt.setFont(MainGui.FONT_MONO);
        txt.setOpaque(false);
        txt.setEditable(false);
        txt.setForeground(fg.darker());
        box.add(t, BorderLayout.NORTH);
        box.add(txt, BorderLayout.CENTER);
        return box;
    }

    private void runBenchmark() {
        btnRun.setEnabled(false);
        lblStatus.setText("Đang tạo bảng tạm 20.000 dòng và đo thời gian...");
        lblStatus.setForeground(MainGui.WARNING);
        areaDetail.setText("Đang chạy EXPLAIN ANALYZE...\n");
        timesBefore.clear(); timesAfter.clear();
        chartPanel.reset();

        SwingWorker<Void, String> worker = new SwingWorker<>() {
            String error;
            StringBuilder detail = new StringBuilder();

            @Override protected Void doInBackground() {
                try (Connection conn = DatabaseConfig.getConnection();
                     Statement stmt = conn.createStatement()) {
                    conn.setAutoCommit(false);
                    try {
                        // Tạo bảng tạm
                        stmt.execute("""
                            CREATE TEMP TABLE demo_gin ON COMMIT DROP AS
                            SELECT id AS MaHocLieu,
                                   (CASE WHEN id %% 100 = 0 THEN 'Database ' ELSE 'Hoc lieu ' END)
                                   || md5(id::text) AS TieuDe
                            FROM generate_series(1, %d) AS rows(id)
                            """.formatted(SAMPLE_SIZE));
                        stmt.execute("ANALYZE demo_gin");

                        // Đo trước chỉ mục
                        detail.append("=== TRƯỚC KHI TẠO CHỈ MỤC (Seq Scan) ===\n");
                        timesBefore = measure(stmt, detail, true);
                        // Lấy scan type
                        try (ResultSet rs = stmt.executeQuery("EXPLAIN " + QUERY)) {
                            while (rs.next()) {
                                String line = rs.getString(1);
                                if (line.contains("Scan")) { scanTypeBefore = extractScanType(line); break; }
                            }
                        }

                        // Tạo chỉ mục GIN
                        publish("Đang tạo chỉ mục GIN Trigram...");
                        stmt.execute("CREATE INDEX demo_gin_tieude_idx ON demo_gin USING gin (TieuDe gin_trgm_ops)");

                        // Đo sau chỉ mục
                        detail.append("\n=== SAU KHI TẠO CHỈ MỤC GIN (Trigram Index) ===\n");
                        timesAfter = measure(stmt, detail, true);
                        try (ResultSet rs = stmt.executeQuery("EXPLAIN " + QUERY)) {
                            while (rs.next()) {
                                String line = rs.getString(1);
                                if (line.contains("Scan") || line.contains("Index")) {
                                    scanTypeAfter = extractScanType(line); break;
                                }
                            }
                        }

                        // Tính trung vị
                        List<Double> sb = new ArrayList<>(timesBefore); Collections.sort(sb);
                        List<Double> sa = new ArrayList<>(timesAfter);  Collections.sort(sa);
                        medianBefore = sb.get(REPEATS / 2);
                        medianAfter  = sa.get(REPEATS / 2);
                    } finally {
                        conn.rollback(); // Drop temp table
                    }
                } catch (SQLException ex) {
                    error = ex.getMessage();
                }
                return null;
            }

            @Override protected void process(List<String> chunks) {
                lblStatus.setText(chunks.get(chunks.size() - 1));
            }

            @Override protected void done() {
                if (error != null) {
                    lblStatus.setText("[LỖI] " + error);
                    lblStatus.setForeground(MainGui.DANGER);
                } else {
                    areaDetail.setText(detail.toString());
                    chartPanel.setData(medianBefore, medianAfter, scanTypeBefore, scanTypeAfter);
                    double speedup = medianBefore / Math.max(medianAfter, 0.001);
                    lblResult.setText(String.format("[KẾT LUẬN] GIN nhanh hơn %.1f lần! (%.3f ms -> %.3f ms)",
                            speedup, medianBefore, medianAfter));
                    lblResult.setForeground(MainGui.SUCCESS);
                    lblStatus.setText("[ĐÃ HOÀN THÀNH]");
                    lblStatus.setForeground(MainGui.SUCCESS);
                }
                btnRun.setEnabled(true);
            }
        };
        worker.execute();
    }

    private List<Double> measure(Statement stmt, StringBuilder detail, boolean printFirst) throws SQLException {
        String explain = "EXPLAIN (ANALYZE, BUFFERS) " + QUERY;
        // Warm up
        try (ResultSet rs = stmt.executeQuery(explain)) { while (rs.next()) {} }

        List<Double> times = new ArrayList<>(REPEATS);
        for (int run = 0; run < REPEATS; run++) {
            try (ResultSet rs = stmt.executeQuery(explain)) {
                while (rs.next()) {
                    String line = rs.getString(1);
                    if (run == 0) detail.append(line).append("\n");
                    if (line.startsWith("Execution Time: ")) {
                        times.add(Double.parseDouble(line.substring(16, line.length() - 3)));
                    }
                }
            }
        }
        detail.append(String.format("-> Các lần đo (ms): %s%n", times));
        List<Double> sorted = new ArrayList<>(times); Collections.sort(sorted);
        double median = sorted.get(REPEATS / 2);
        detail.append(String.format("-> Trung vị: %.3f ms%n", median));
        return times;
    }

    private String extractScanType(String line) {
        line = line.trim().replace("->", "").trim();
        if (line.contains("Seq Scan"))    return "Seq Scan";
        if (line.contains("Bitmap"))      return "Bitmap Index Scan";
        if (line.contains("Index Scan"))  return "Index Scan";
        return line.split("\\s+")[0];
    }

    // ===== Biểu đồ cột tuỳ chỉnh =====
    static class BarChartPanel extends JPanel {
        private double before = 0, after = 0;
        private String labelBefore = "Seq Scan\n(Trước GIN)";
        private String labelAfter  = "GIN Scan\n(Sau GIN)";
        private boolean hasData = false;

        BarChartPanel() { setBackground(Color.WHITE); }

        void reset() { hasData = false; before = 0; after = 0; repaint(); }

        void setData(double b, double a, String lb, String la) {
            before = b; after = a;
            labelBefore = lb + "\n(Trước GIN)";
            labelAfter  = la + "\n(Sau GIN)";
            hasData = true;
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int padL = 70, padR = 30, padT = 30, padB = 60;
            int chartW = w - padL - padR;
            int chartH = h - padT - padB;

            // Nền chart
            g2.setColor(new Color(0xF8FAFC));
            g2.fillRect(padL, padT, chartW, chartH);
            g2.setColor(MainGui.BORDER_COL);
            g2.drawRect(padL, padT, chartW, chartH);

            if (!hasData) {
                g2.setColor(new Color(0x94A3B8));
                g2.setFont(MainGui.FONT_BODY);
                String msg = "Chưa có dữ liệu — nhấn 'Chạy GIN Trigram Benchmark'";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(msg, padL + (chartW - fm.stringWidth(msg)) / 2, padT + chartH / 2);
                return;
            }

            double maxVal = Math.max(before, 0.001) * 1.15;
            int barW = chartW / 5;

            // Vạch ngang + nhãn trục Y
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            int ticks = 5;
            for (int i = 0; i <= ticks; i++) {
                double val = maxVal * i / ticks;
                int y = padT + chartH - (int)(chartH * i / ticks);
                g2.setColor(new Color(0xE2E8F0));
                g2.drawLine(padL, y, padL + chartW, y);
                g2.setColor(new Color(0x64748B));
                String label = String.format("%.1f", val);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(label, padL - fm.stringWidth(label) - 4, y + 4);
            }

            // Cột 1 — Trước GIN (màu cam)
            int x1 = padL + chartW / 4 - barW / 2;
            int barH1 = (int)(chartH * before / maxVal);
            drawBar(g2, x1, padT + chartH - barH1, barW, barH1,
                    new Color(0xFB923C), new Color(0xEA580C),
                    String.format("%.1f ms", before), labelBefore, padT + chartH, padB);

            // Cột 2 — Sau GIN (màu xanh)
            int x2 = padL + 3 * chartW / 4 - barW / 2;
            int barH2 = Math.max(2, (int)(chartH * after / maxVal));
            drawBar(g2, x2, padT + chartH - barH2, barW, barH2,
                    new Color(0x34D399), new Color(0x059669),
                    String.format("%.3f ms", after), labelAfter, padT + chartH, padB);

            // Speedup text
            double speedup = before / Math.max(after, 0.001);
            int midX = padL + chartW / 2;
            int midY = padT + chartH / 2;
            g2.setColor(MainGui.ACCENT);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
            String speedupTxt = String.format("%.0fx nhanh hơn", speedup);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(speedupTxt, midX - fm.stringWidth(speedupTxt) / 2, midY);
        }

        private void drawBar(Graphics2D g2, int x, int y, int w, int h,
                             Color top, Color bottom, String valLabel, String nameLabel,
                             int baseY, int padB) {
            GradientPaint gp = new GradientPaint(x, y, top, x, y + h, bottom);
            g2.setPaint(gp);
            g2.fill(new RoundRectangle2D.Double(x, y, w, h, 8, 8));
            g2.setColor(bottom.darker());
            g2.draw(new RoundRectangle2D.Double(x, y, w, h, 8, 8));

            // Giá trị trên đầu cột
            g2.setColor(new Color(0x1E293B));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(valLabel, x + (w - fm.stringWidth(valLabel)) / 2, y - 4);

            // Nhãn dưới cột
            String[] lines = nameLabel.split("\n");
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            fm = g2.getFontMetrics();
            for (int i = 0; i < lines.length; i++) {
                g2.drawString(lines[i], x + (w - fm.stringWidth(lines[i])) / 2,
                        baseY + 14 + i * 13);
            }
        }
    }
}
