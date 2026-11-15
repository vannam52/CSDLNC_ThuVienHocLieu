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
    private final DefaultTableModel modelTacGia   = new DefaultTableModel();
    private final DefaultTableModel modelChuDe    = new DefaultTableModel();
    private final DefaultTableModel modelJoined   = new DefaultTableModel();
    private final DefaultTableModel modelSingle   = new DefaultTableModel();
    private final DefaultTableModel modelNormSub  = new DefaultTableModel();
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

        // ---- 4NF section (3 bước: Trước 4NF -> Tách 2 bảng 4NF -> JOIN kiểm chứng) ----
        JPanel panel4nf = new JPanel(new GridLayout(1, 3, 12, 0));
        panel4nf.setOpaque(false);

        // Cột 1: Bảng phẳng (Chưa chuẩn hoá 4NF)
        modelFlat.setColumnIdentifiers(new String[]{"MaHocLieu", "MaTacGia", "MaChuDe"});
        JTable tblFlat = MainGui.makeStyledTable(new String[0], new Object[0][0]);
        tblFlat.setModel(modelFlat);
        JPanel leftCard = makeCard("1. Chưa 4NF: Bảng phẳng (lặp chéo)", tblFlat);

        // Cột 2: Sau tách 4NF (2 bảng độc lập: HocLieu_TacGia & HocLieu_ChuDe)
        modelTacGia.setColumnIdentifiers(new String[]{"MaHocLieu", "MaTacGia"});
        JTable tblTacGia = MainGui.makeStyledTable(new String[0], new Object[0][0]);
        tblTacGia.setModel(modelTacGia);
        JScrollPane spTacGia = new JScrollPane(tblTacGia);
        spTacGia.setPreferredSize(new Dimension(260, 82));
        spTacGia.setBorder(BorderFactory.createLineBorder(MainGui.BORDER_COL));

        modelChuDe.setColumnIdentifiers(new String[]{"MaHocLieu", "MaChuDe"});
        JTable tblChuDe = MainGui.makeStyledTable(new String[0], new Object[0][0]);
        tblChuDe.setModel(modelChuDe);
        JScrollPane spChuDe = new JScrollPane(tblChuDe);
        spChuDe.setPreferredSize(new Dimension(260, 82));
        spChuDe.setBorder(BorderFactory.createLineBorder(MainGui.BORDER_COL));

        JPanel split4nfBody = new JPanel(new GridLayout(2, 1, 0, 6));
        split4nfBody.setOpaque(false);
        split4nfBody.add(wrapMiniTable("Bảng HocLieu_TacGia (riêng):", spTacGia));
        split4nfBody.add(wrapMiniTable("Bảng HocLieu_ChuDe (riêng):", spChuDe));

        JPanel midCard = new JPanel(new BorderLayout(0, 6));
        midCard.setBackground(Color.WHITE);
        midCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainGui.BORDER_COL),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        JLabel midTitle = new JLabel("2. Đã chuẩn 4NF: Tách 2 bảng độc lập");
        midTitle.setFont(MainGui.FONT_BOLD);
        midTitle.setForeground(MainGui.SUCCESS);
        midCard.add(midTitle, BorderLayout.NORTH);
        midCard.add(split4nfBody, BorderLayout.CENTER);

        // Cột 3: Kết quả nối lại từ 2 bảng 4NF
        modelJoined.setColumnIdentifiers(new String[]{"MaHocLieu", "MaTacGia", "MaChuDe"});
        JTable tblJoined = MainGui.makeStyledTable(new String[0], new Object[0][0]);
        tblJoined.setModel(modelJoined);
        JPanel rightCard = makeCard("3. JOIN 2 bảng 4NF -> Khớp 100% (1)", tblJoined);

        panel4nf.add(leftCard);
        panel4nf.add(midCard);
        panel4nf.add(rightCard);

        // Dòng chênh lệch
        lblDiff.setFont(MainGui.FONT_BOLD);
        lblDiff.setForeground(MainGui.SUCCESS);
        JPanel diffPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
        diffPanel.setOpaque(false);
        diffPanel.add(lblDiff);

        // ---- Kế thừa section: 2 bảng đối chiếu (Chưa tách có NULL vs Đã tách không NULL) + Thống kê phía dưới ----
        JPanel panelInheritCompare = new JPanel(new GridLayout(1, 2, 12, 0));
        panelInheritCompare.setOpaque(false);

        // Trái: Single Table (Chưa chuẩn hoá - có nhiều ô NULL)
        String[] singleCols = {"MaHocLieu", "LoaiHocLieu", "ISBN", "SoTrang", "Truong", "DiemBaoVe", "ThoiLuong", "DinhDang"};
        modelSingle.setColumnIdentifiers(singleCols);
        JTable tblSingle = MainGui.makeStyledTable(new String[0], new Object[0][0]);
        tblSingle.setModel(modelSingle);
        tblSingle.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tblSingle.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    if ("NULL".equals(value)) {
                        c.setBackground(new Color(0xFEE2E2));
                        c.setForeground(MainGui.DANGER);
                        setFont(MainGui.FONT_BOLD);
                    } else {
                        c.setBackground(row % 2 == 0 ? MainGui.ROW_ODD : MainGui.ROW_EVEN);
                        c.setForeground(new Color(0x1E293B));
                        setFont(MainGui.FONT_BODY);
                    }
                }
                return c;
            }
        });
        JPanel singleCard = makeCard("1. Chưa tách (Single Table — bị nhiều ô NULL)", tblSingle);

        // Phải: Joined Table (Đã tách chuẩn — bảng cha + các bảng con, 0 ô NULL)
        String[] normSubCols = {"Bảng lưu trữ", "MaHocLieu", "Dữ liệu riêng (Không NULL)"};
        modelNormSub.setColumnIdentifiers(normSubCols);
        JTable tblNormSub = MainGui.makeStyledTable(new String[0], new Object[0][0]);
        tblNormSub.setModel(modelNormSub);
        JPanel normSubCard = makeCard("2. Đã tách chuẩn (Joined Table — 0% ô NULL)", tblNormSub);
        ((JLabel) normSubCard.getComponent(0)).setForeground(MainGui.SUCCESS);

        panelInheritCompare.add(singleCard);
        panelInheritCompare.add(normSubCard);

        // Phía dưới: Bảng thống kê so sánh hiệu quả giữa 2 phiên bản
        modelNullStat.setColumnIdentifiers(new String[]{"MaHocLieu", "LoaiHocLieu", "SoCotLopConBiNull", "TongCotLopCon", "TyLeNull (%)"});
        JTable tblNull = MainGui.makeStyledTable(new String[0], new Object[0][0]);
        tblNull.setModel(modelNullStat);
        JPanel nullCard = makeCard("3. Thống kê so sánh hiệu quả giữa Single Table và Joined Table", tblNull);

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
        content.add(panelInheritCompare);
        content.add(Box.createVerticalStrut(10));
        content.add(nullCard);
        content.add(nullSumPanel);
        content.add(Box.createVerticalStrut(10));
        content.add(noteBox);

        MainGui.alignLeft(content);

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

    private JPanel wrapMiniTable(String caption, JScrollPane sp) {
        JPanel p = new JPanel(new BorderLayout(0, 3));
        p.setOpaque(false);
        JLabel lbl = new JLabel(caption);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(new Color(0x334155));
        p.add(lbl, BorderLayout.NORTH);
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    private void runExperiment() {
        btnRun.setEnabled(false);
        lblStatus.setText("Đang thực thi truy vấn...");
        lblStatus.setForeground(MainGui.WARNING);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            String[][] dataFlat, dataTacGia, dataChuDe, dataJoined, dataSingle, dataNormSub, dataNullStat;
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
                    // 2 bảng đã tách 4NF (cho HL01)
                    try (ResultSet rs = stmt.executeQuery(
                            "SELECT MaHocLieu, MaTacGia FROM HocLieu_TacGia WHERE MaHocLieu = 'HL01' ORDER BY MaTacGia")) {
                        dataTacGia = toArray(rs, 2);
                    }
                    try (ResultSet rs = stmt.executeQuery(
                            "SELECT MaHocLieu, MaChuDe FROM HocLieu_ChuDe WHERE MaHocLieu = 'HL01' ORDER BY MaChuDe")) {
                        dataChuDe = toArray(rs, 2);
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
                    // Single Table (chưa tách, nhiều ô NULL)
                    try (ResultSet rs = stmt.executeQuery(
                            "SELECT MaHocLieu, LoaiHocLieu, " +
                            "COALESCE(ISBN, 'NULL'), COALESCE(SoTrang::TEXT, 'NULL'), " +
                            "COALESCE(Truong, 'NULL'), COALESCE(DiemBaoVe::TEXT, 'NULL'), " +
                            "COALESCE(ThoiLuong::TEXT, 'NULL'), COALESCE(DinhDang, 'NULL') " +
                            "FROM HocLieu_SingleTable ORDER BY MaHocLieu")) {
                        dataSingle = toArray(rs, 8);
                    }
                    // Joined Table (đã tách bảng con riêng, 0 ô NULL)
                    try (ResultSet rs = stmt.executeQuery(
                            "SELECT 'Sach' AS Bang, MaHocLieu, 'ISBN=' || ISBN || ', SoTrang=' || SoTrang AS DuLieu FROM Sach WHERE MaHocLieu='HL01' " +
                            "UNION ALL " +
                            "SELECT 'LuanVan', MaHocLieu, 'Truong=' || Truong || ', Diem=' || DiemBaoVe FROM LuanVan WHERE MaHocLieu='HL02' " +
                            "UNION ALL " +
                            "SELECT 'VideoBaiGiang', MaHocLieu, 'ThoiLuong=' || ThoiLuong || 'p, ' || DoPhanGiai FROM VideoBaiGiang WHERE MaHocLieu='HL03' " +
                            "UNION ALL " +
                            "SELECT 'TaiLieuDienTu', MaHocLieu, 'DinhDang=' || DinhDang || ', ' || QuyenTruyCap FROM TaiLieuDienTu WHERE MaHocLieu='HL04'")) {
                        dataNormSub = toArray(rs, 3);
                    }
                    // NULL stats
                    try (ResultSet rs = stmt.executeQuery(
                            "SELECT MaHocLieu, LoaiHocLieu, SoCotLopConBiNull, TongCotLopCon, " +
                            "ROUND(SoCotLopConBiNull::numeric / TongCotLopCon * 100, 2)::TEXT || '%' AS TyLeNull " +
                            "FROM v_ThucNghiemKeThua_Null ORDER BY MaHocLieu")) {
                        dataNullStat = toArray(rs, 5);
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
                    fillTable(modelTacGia,   new String[]{"MaHocLieu","MaTacGia"}, dataTacGia);
                    fillTable(modelChuDe,    new String[]{"MaHocLieu","MaChuDe"}, dataChuDe);
                    fillTable(modelJoined,   new String[]{"MaHocLieu","MaTacGia","MaChuDe"}, dataJoined);
                    fillTable(modelSingle,   new String[]{"MaHocLieu","LoaiHocLieu","ISBN","SoTrang","Truong","DiemBaoVe","ThoiLuong","DinhDang"}, dataSingle);
                    fillTable(modelNormSub,  new String[]{"Bảng lưu trữ","MaHocLieu","Dữ liệu riêng (Không NULL)"}, dataNormSub);
                    fillTable(modelNullStat, new String[]{"MaHocLieu","LoaiHocLieu","SoCotLopConBiNull","TongCotLopCon","TyLeNull (%)"}, dataNullStat);
                    lblDiff.setText("[KẾT QUẢ] Số dòng chênh lệch (EXCEPT ALL hai chiều): " + diffCount +
                            (diffCount == 0 ? "  -> Bảng phẳng và bảng nối hoàn toàn trùng khớp!" : ""));
                    String pct = nullTotal > 0 ? String.format("%.2f%%", 100.0 * nullCount / nullTotal) : "N/A";
                    lblNullStat.setText(String.format("[SO SÁNH HIỆU QUẢ] Single Table: %d/%d ô NULL (%s lãng phí)  |  Joined Table: 0/%d ô NULL (0.00%% lãng phí)!", nullCount, nullTotal, pct, nullTotal));
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
