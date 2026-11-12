package com.library.gui;

import com.library.dao.ThucNghiemDeTai3DAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

import com.library.config.DatabaseConfig;

/**
 * Tab 1 — Thực nghiệm 4NF & Ánh xạ Kế thừa.
 * Hiển thị:
 *   - Bảng so sánh Bảng phẳng vs Kết quả nối (4NF)
 *   - Thống kê NULL ô Single Table vs Joined Table
 */
public class PanelNormalization extends JPanel {

    private final DefaultTableModel modelFlat     = new DefaultTableModel();
    private final DefaultTableModel modelJoined   = new DefaultTableModel();
    private final DefaultTableModel modelNullStat = new DefaultTableModel();
    private final JLabel lblDiff    = new JLabel(" ");
    private final JLabel lblNullStat= new JLabel(" ");
    private final JLabel lblStatus  = new JLabel("Nhấn nút để bắt đầu thực nghiệm.");
    private JButton btnRun;

    public JButton getBtnRun() { return btnRun; }

    public PanelNormalization() {
        super(new BorderLayout(0, 0));
        setBackground(MainGui.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        buildUI();
    }

    private void buildUI() {
        // ---- Toolbar ----
        btnRun = MainGui.makeButton("Chạy thực nghiệm 4NF & Kế thừa", MainGui.ACCENT);
        btnRun.addActionListener(e -> runExperiment());

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        toolbar.setOpaque(false);
        toolbar.add(btnRun);
        toolbar.add(lblStatus);
        lblStatus.setFont(MainGui.FONT_BOLD);
        lblStatus.setForeground(new Color(0x64748B));

        // ---- 4NF section ----
        JPanel panel4nf = new JPanel(new GridLayout(1, 2, 12, 0));
        panel4nf.setOpaque(false);

        // Bảng phẳng
        modelFlat.setColumnIdentifiers(new String[]{"MaHocLieu", "MaTacGia", "MaChuDe"});
        JTable tblFlat = MainGui.makeStyledTable(new String[0], new Object[0][0]);
        tblFlat.setModel(modelFlat);
        JPanel leftCard = makeCard("Bảng phẳng (Chưa chuẩn hoá 4NF)", tblFlat);

        // Kết quả nối
        modelJoined.setColumnIdentifiers(new String[]{"MaHocLieu", "MaTacGia", "MaChuDe"});
        JTable tblJoined = MainGui.makeStyledTable(new String[0], new Object[0][0]);
        tblJoined.setModel(modelJoined);
        JPanel rightCard = makeCard("Kết quả nối (Sau chuẩn hoá 4NF)", tblJoined);

        panel4nf.add(leftCard);
        panel4nf.add(rightCard);

        // Dòng chênh lệch
        lblDiff.setFont(MainGui.FONT_BOLD);
        lblDiff.setForeground(MainGui.SUCCESS);
        JPanel diffPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
        diffPanel.setOpaque(false);
        diffPanel.add(lblDiff);

        // ---- Kế thừa section ----
        modelNullStat.setColumnIdentifiers(new String[]{"MaHocLieu", "LoaiHocLieu", "SoCotLopConBiNull", "TongCotLopCon"});
        JTable tblNull = MainGui.makeStyledTable(new String[0], new Object[0][0]);
        tblNull.setModel(modelNullStat);
        JPanel nullCard = makeCard("Ô NULL trong Single Table (ánh xạ kế thừa)", tblNull);

        lblNullStat.setFont(MainGui.FONT_BOLD);
        lblNullStat.setForeground(MainGui.WARNING);
        JPanel nullSumPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
        nullSumPanel.setOpaque(false);
        nullSumPanel.add(lblNullStat);

        // ---- Note box ----
        JTextArea noteBox = new JTextArea(
                "[!] Giải thích kỹ thuật:\n" +
                "• 4NF: Tách bảng HocLieu_TacGia và HocLieu_ChuDe riêng biệt để loại bỏ phụ thuộc đa trị.\n" +
                "  Nối lại bằng NATURAL JOIN cho kết quả khớp 100% với bảng phẳng ban đầu (chênh lệch = 0 dòng).\n" +
                "• Single Table Inheritance: Gom tất cả lớp con vào 1 bảng duy nhất -> ô NULL xuất hiện ở các cột\n" +
                "  thuộc lớp con khác (Joined Table tách bảng riêng nên hoàn toàn không bị lãng phí bộ nhớ).");
        noteBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        noteBox.setEditable(false);
        noteBox.setBackground(new Color(0xEFF6FF));
        noteBox.setForeground(new Color(0x1E40AF));
        noteBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xBFDBFE)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        noteBox.setLineWrap(true);
        noteBox.setWrapStyleWord(true);

        // ---- Ghép lại ----
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.add(MainGui.sectionLabel("Thực nghiệm 1: Chuẩn hoá 4NF (Loại bỏ phụ thuộc đa trị)"));
        content.add(Box.createVerticalStrut(8));
        content.add(panel4nf);
        content.add(diffPanel);
        content.add(Box.createVerticalStrut(14));
        content.add(MainGui.sectionLabel("Thực nghiệm 2: Ánh xạ Kế thừa (Single Table vs Joined Table)"));
        content.add(Box.createVerticalStrut(8));
        content.add(nullCard);
        content.add(nullSumPanel);
        content.add(Box.createVerticalStrut(10));
        content.add(noteBox);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(toolbar, BorderLayout.NORTH);
        add(scroll,  BorderLayout.CENTER);
    }

    private JPanel makeCard(String title, JTable table) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainGui.BORDER_COL),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        JLabel lbl = new JLabel(title);
        lbl.setFont(MainGui.FONT_BOLD);
        lbl.setForeground(MainGui.ACCENT);
        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(400, 180));
        sp.setBorder(BorderFactory.createLineBorder(MainGui.BORDER_COL));
        card.add(lbl, BorderLayout.NORTH);
        card.add(sp,  BorderLayout.CENTER);
        return card;
    }

    private void runExperiment() {
        btnRun.setEnabled(false);
        lblStatus.setText("Đang thực thi truy vấn...");
        lblStatus.setForeground(MainGui.WARNING);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            String[][] dataFlat, dataJoined, dataNullStat;
            long diffCount;
            long nullTotal, nullCount;
            String error;

            @Override protected Void doInBackground() {
                try (Connection conn = DatabaseConfig.getConnection();
                     Statement stmt = conn.createStatement()) {

                    // Bảng phẳng
                    try (ResultSet rs = stmt.executeQuery(
                            "SELECT MaHocLieu, MaTacGia, MaChuDe FROM HocLieu_ChuaChuanHoa_4NF ORDER BY MaTacGia, MaChuDe")) {
                        dataFlat = toArray(rs, 3);
                    }
                    // Kết quả nối
                    try (ResultSet rs = stmt.executeQuery(
                            "SELECT MaHocLieu, MaTacGia, MaChuDe FROM v_ThucNghiem4NF_KhoiPhuc ORDER BY MaTacGia, MaChuDe")) {
                        dataJoined = toArray(rs, 3);
                    }
                    // Chênh lệch
                    try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM v_ThucNghiem4NF_ChenhLech")) {
                        rs.next(); diffCount = rs.getLong(1);
                    }
                    // NULL stats
                    try (ResultSet rs = stmt.executeQuery(
                            "SELECT MaHocLieu, LoaiHocLieu, SoCotLopConBiNull, TongCotLopCon FROM v_ThucNghiemKeThua_Null ORDER BY MaHocLieu")) {
                        dataNullStat = toArray(rs, 4);
                    }
                    // Tổng NULL
                    try (ResultSet rs = stmt.executeQuery(
                            "SELECT COALESCE(SUM(SoCotLopConBiNull),0), COALESCE(SUM(TongCotLopCon),0) FROM v_ThucNghiemKeThua_Null")) {
                        rs.next(); nullCount = rs.getLong(1); nullTotal = rs.getLong(2);
                    }
                } catch (SQLException ex) {
                    error = ex.getMessage();
                }
                return null;
            }

            @Override protected void done() {
                if (error != null) {
                    lblStatus.setText("[LỖI] " + error);
                    lblStatus.setForeground(MainGui.DANGER);
                } else {
                    fillTable(modelFlat,     new String[]{"MaHocLieu","MaTacGia","MaChuDe"}, dataFlat);
                    fillTable(modelJoined,   new String[]{"MaHocLieu","MaTacGia","MaChuDe"}, dataJoined);
                    fillTable(modelNullStat, new String[]{"MaHocLieu","LoaiHocLieu","SoCotLopConBiNull","TongCotLopCon"}, dataNullStat);
                    lblDiff.setText("[KẾT QUẢ] Số dòng chênh lệch (EXCEPT ALL hai chiều): " + diffCount +
                            (diffCount == 0 ? "  -> Bảng phẳng và bảng nối hoàn toàn trùng khớp!" : ""));
                    String pct = nullTotal > 0 ? String.format("%.2f%%", 100.0 * nullCount / nullTotal) : "N/A";
                    lblNullStat.setText(String.format("[KẾT LUẬN] Tổng: %d/%d ô NULL (%s) — Single Table lãng phí bộ nhớ lưu trữ!", nullCount, nullTotal, pct));
                    lblStatus.setText("[ĐÃ HOÀN THÀNH]");
                    lblStatus.setForeground(MainGui.SUCCESS);
                }
                btnRun.setEnabled(true);
            }
        };
        worker.execute();
    }

    private static String[][] toArray(ResultSet rs, int cols) throws SQLException {
        java.util.List<String[]> list = new java.util.ArrayList<>();
        while (rs.next()) {
            String[] row = new String[cols];
            for (int i = 0; i < cols; i++) row[i] = rs.getString(i + 1);
            list.add(row);
        }
        return list.toArray(new String[0][]);
    }

    private static void fillTable(DefaultTableModel model, String[] cols, String[][] data) {
        model.setRowCount(0);
        model.setColumnIdentifiers(cols);
        if (data != null) for (String[] row : data) model.addRow(row);
    }
}
