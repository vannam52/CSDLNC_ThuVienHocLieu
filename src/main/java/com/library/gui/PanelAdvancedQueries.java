package com.library.gui;

import com.library.config.DatabaseConfig;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Tab 6 — Bộ 12 Truy vấn SQL Nâng cao (Q01 - Q12).
 * Chia 2 bên:
 *   - Trái: Câu lệnh SQL đang dùng + mô tả kỹ thuật + tham số (nếu có)
 *   - Phải: Bảng kết quả trả về + thời gian thực thi (ms) & số dòng
 */
public class PanelAdvancedQueries extends JPanel {

    private record QuerySpec(
            String id,
            String title,
            String technique,
            String sql
    ) {
        @Override
        public String toString() {
            return id + " — " + title;
        }
    }

    private static final List<QuerySpec> QUERIES = List.of(
            new QuerySpec("Q01", "Cây chủ đề phân cấp (WITH RECURSIVE)",
                    "CTE đệ quy xử lý quan hệ tự tham chiếu 1:N",
                    """
                    WITH RECURSIVE CayChuDe AS (
                        SELECT MaChuDe, TenChuDe, ChuDeCha, 1 AS CapDo
                        FROM ChuDe
                        WHERE ChuDeCha IS NULL
                        UNION ALL
                        SELECT c.MaChuDe, c.TenChuDe, c.ChuDeCha, ccd.CapDo + 1
                        FROM ChuDe c
                        JOIN CayChuDe ccd ON c.ChuDeCha = ccd.MaChuDe
                    )
                    SELECT MaChuDe, TenChuDe, COALESCE(ChuDeCha, 'Gốc') AS ChuDeCha, CapDo
                    FROM CayChuDe
                    ORDER BY CapDo, TenChuDe;"""),

            new QuerySpec("Q02", "Truy vấn Đa hình 4 lớp con (Polymorphic Outer Join)",
                    "Class Table Inheritance — LEFT JOIN bảng cha với 4 bảng con",
                    """
                    SELECT
                        h.MaHocLieu, h.TieuDe, h.NamXuatBan, h.GiaTien,
                        CASE
                            WHEN s.MaHocLieu IS NOT NULL THEN 'Sách'
                            WHEN l.MaHocLieu IS NOT NULL THEN 'Luận văn'
                            WHEN v.MaHocLieu IS NOT NULL THEN 'Video'
                            WHEN t.MaHocLieu IS NOT NULL THEN 'Tài liệu điện tử'
                            ELSE 'Khác'
                        END AS LoaiHocLieu,
                        COALESCE(s.ISBN, 'N/A') AS ISBN,
                        COALESCE(s.SoTrang::TEXT, 'N/A') AS SoTrang,
                        COALESCE(l.DiemBaoVe::TEXT, 'N/A') AS DiemBaoVe,
                        COALESCE(v.ThoiLuong::TEXT || ' phút', 'N/A') AS ThoiLuongVideo
                    FROM HocLieu h
                    LEFT JOIN Sach s ON h.MaHocLieu = s.MaHocLieu
                    LEFT JOIN LuanVan l ON h.MaHocLieu = l.MaHocLieu
                    LEFT JOIN VideoBaiGiang v ON h.MaHocLieu = v.MaHocLieu
                    LEFT JOIN TaiLieuDienTu t ON h.MaHocLieu = t.MaHocLieu
                    ORDER BY h.MaHocLieu;"""),

            new QuerySpec("Q03", "Học liệu đa chủ đề chứng minh 4NF (GROUP BY ... HAVING)",
                    "Lọc nhóm các học liệu thuộc nhiều hơn 1 chủ đề (đa trị)",
                    """
                    SELECT
                        h.MaHocLieu,
                        h.TieuDe,
                        COUNT(hc.MaChuDe) AS SoLuongChuDe
                    FROM HocLieu h
                    JOIN HocLieu_ChuDe hc ON h.MaHocLieu = hc.MaHocLieu
                    GROUP BY h.MaHocLieu, h.TieuDe
                    HAVING COUNT(hc.MaChuDe) > 1
                    ORDER BY SoLuongChuDe DESC, h.TieuDe;"""),

            new QuerySpec("Q04", "Thống kê tần suất mượn kết nối 3 bảng",
                    "Multi-Table Aggregation (HocLieu -> BanSao -> PhieuMuon)",
                    """
                    SELECT
                        h.MaHocLieu,
                        h.TieuDe,
                        COUNT(p.MaPhieu) AS TongLuotMuon,
                        COUNT(DISTINCT b.MaBanSao) AS TongSoBanSaoHienCo
                    FROM HocLieu h
                    JOIN BanSao b ON h.MaHocLieu = b.MaHocLieu
                    LEFT JOIN PhieuMuon p ON b.MaBanSao = p.MaBanSao
                    GROUP BY h.MaHocLieu, h.TieuDe
                    ORDER BY TongLuotMuon DESC, h.TieuDe;"""),

            new QuerySpec("Q05", "Bạn đọc chưa từng mượn sách (NOT EXISTS)",
                    "Truy vấn con tương quan phủ định (Correlated Subquery)",
                    """
                    SELECT bd.MaBanDoc, bd.HoTen, bd.Email, bd.LoaiTaiKhoan
                    FROM BanDoc bd
                    WHERE NOT EXISTS (
                        SELECT 1
                        FROM PhieuMuon pm
                        WHERE pm.MaBanDoc = bd.MaBanDoc
                    )
                    ORDER BY bd.MaBanDoc;"""),

            new QuerySpec("Q06", "Học liệu giá cao hơn trung bình (Scalar Subquery)",
                    "Truy vấn con lồng nhau vô hướng với AVG()",
                    """
                    SELECT
                        h.MaHocLieu,
                        h.TieuDe,
                        h.GiaTien,
                        ROUND((SELECT AVG(GiaTien) FROM HocLieu WHERE GiaTien > 0), 2) AS GiaTienTrungBinh
                    FROM HocLieu h
                    WHERE h.GiaTien > (
                        SELECT AVG(GiaTien) FROM HocLieu WHERE GiaTien > 0
                    )
                    ORDER BY h.GiaTien DESC;"""),

            new QuerySpec("Q07", "Xếp hạng giá học liệu theo năm (DENSE_RANK)",
                    "Window Function phân vùng theo NamXuatBan",
                    """
                    SELECT
                        h.MaHocLieu,
                        h.TieuDe,
                        h.NamXuatBan,
                        h.GiaTien,
                        DENSE_RANK() OVER (
                            PARTITION BY h.NamXuatBan
                            ORDER BY h.GiaTien DESC
                        ) AS XepHangGiaTheoNam
                    FROM HocLieu h
                    WHERE h.GiaTien > 0
                    ORDER BY h.NamXuatBan DESC, XepHangGiaTheoNam;"""),

            new QuerySpec("Q08", "Thống kê theo chủ đề (TABLE Function)",
                    "PL/pgSQL Function trả về bảng fn_ThongKe_HocLieu_TheoChuDe()",
                    """
                    SELECT MaChuDe, TenChuDe, SoLuongHocLieu, TiLePhanTram, TongGiaTri
                    FROM fn_ThongKe_HocLieu_TheoChuDe();"""),

            new QuerySpec("Q09", "Kiểm tra tư cách mượn (Scalar Function)",
                    "Hàm fn_KiemTra_CoTheMuon(?) kiểm tra khóa, hạn thẻ, số cuốn đang mượn",
                    """
                    SELECT
                        ? AS MaBanDoc,
                        fn_KiemTra_CoTheMuon(?) AS DuDieuKienMuon;"""),

            new QuerySpec("Q10", "Tìm kiếm động (Dynamic SQL Function)",
                    "Stored Function dùng EXECUTE động có whitelist chống SQL Injection",
                    """
                    SELECT *
                    FROM fn_TimKiem_HocLieu_Dong(
                        p_TieuDe     := ?,
                        p_NamTu      := ?,
                        p_SapXepTheo := ?
                    );"""),

            new QuerySpec("Q11", "Báo cáo tổng hợp từ View (v_HocLieu_DayDu)",
                    "View đa hình kết hợp STRING_AGG gom danh sách tác giả và chủ đề",
                    """
                    SELECT
                        MaHocLieu, TieuDe, LoaiHocLieu,
                        DanhSachTacGia, DanhSachChuDe,
                        TongSoBanSao, SoBanSaoKhaDung
                    FROM v_HocLieu_DayDu
                    WHERE TongSoBanSao > 0;"""),

            new QuerySpec("Q12", "Phân tích kế hoạch thực thi (EXPLAIN ANALYZE)",
                    "Đo Cost, Buffers và Execution Time với chỉ mục GIN Trigram",
                    """
                    EXPLAIN (ANALYZE, BUFFERS)
                    SELECT MaHocLieu, TieuDe
                    FROM HocLieu
                    WHERE TieuDe ILIKE '%Database%';""")
    );

    private final JComboBox<QuerySpec> cboQuery = new JComboBox<>(QUERIES.toArray(new QuerySpec[0]));
    private final JButton btnRun = MainGui.makeButton("Thực thi truy vấn", MainGui.ACCENT);
    private final JLabel lblMetrics = new JLabel("⏱ Thời gian: -- ms  |  📊 Số dòng: --");
    private final JLabel lblTechnique = new JLabel(" ");
    private final JTextArea areaSql = new JTextArea();
    private final DefaultTableModel tableModel = new DefaultTableModel();
    private final JTable tblResult = MainGui.makeStyledTable(new String[0], new Object[0][0]);

    // Tham số cho Q09 & Q10
    private final JPanel paramBar = new JPanel(new CardLayout());
    private final JTextField txtMaBanDoc = new JTextField("BD01", 8);
    private final JTextField txtTuKhoa = new JTextField("Database", 14);
    private final JTextField txtNamTu = new JTextField("2018", 6);
    private final JComboBox<String> cboSapXep = new JComboBox<>(new String[]{"GiaTien", "TieuDe", "NamXuatBan"});

    public PanelAdvancedQueries() {
        super(new BorderLayout(0, 12));
        setBackground(MainGui.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        buildUI();
        onQueryChanged();
    }

    private void buildUI() {
        // ---- Top Toolbar ----
        cboQuery.setFont(MainGui.FONT_BODY);
        cboQuery.setPreferredSize(new Dimension(440, 32));
        cboQuery.addActionListener(e -> onQueryChanged());

        btnRun.addActionListener(e -> executeSelectedQuery());

        lblMetrics.setFont(MainGui.FONT_BOLD);
        lblMetrics.setForeground(MainGui.ACCENT);

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        topRow.setOpaque(false);
        JLabel lblPick = new JLabel("Chọn truy vấn:");
        lblPick.setFont(MainGui.FONT_BOLD);
        topRow.add(lblPick);
        topRow.add(cboQuery);
        topRow.add(btnRun);
        topRow.add(lblMetrics);

        // ---- Param Bar (hiện khi chọn Q09 hoặc Q10) ----
        paramBar.setOpaque(false);
        paramBar.add(new JPanel() {{ setOpaque(false); }}, "NONE");

        JPanel pQ09 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        pQ09.setOpaque(false);
        pQ09.add(boldLabel("Tham số Q09 — Mã bạn đọc:"));
        pQ09.add(txtMaBanDoc);
        paramBar.add(pQ09, "Q09");

        JPanel pQ10 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        pQ10.setOpaque(false);
        pQ10.add(boldLabel("Tham số Q10 — Từ khóa:"));
        pQ10.add(txtTuKhoa);
        pQ10.add(boldLabel("Năm từ:"));
        pQ10.add(txtNamTu);
        pQ10.add(boldLabel("Sắp xếp theo:"));
        pQ10.add(cboSapXep);
        paramBar.add(pQ10, "Q10");

        JPanel northContainer = new JPanel(new BorderLayout(0, 6));
        northContainer.setOpaque(false);
        northContainer.add(topRow, BorderLayout.NORTH);
        northContainer.add(paramBar, BorderLayout.CENTER);

        // ---- Left Card: SQL Query đang dùng ----
        areaSql.setEditable(false);
        areaSql.setFont(MainGui.FONT_MONO);
        areaSql.setBackground(new Color(0x0F172A));
        areaSql.setForeground(new Color(0xE2E8F0));
        areaSql.setCaretColor(Color.WHITE);
        areaSql.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        areaSql.setLineWrap(false);

        JScrollPane sqlScroll = new JScrollPane(
                areaSql,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sqlScroll.setBorder(BorderFactory.createLineBorder(MainGui.BORDER_COL));

        JPanel sqlHeader = new JPanel(new GridLayout(2, 1, 0, 2));
        sqlHeader.setOpaque(false);
        JLabel sqlTitle = new JLabel("Câu lệnh SQL đang sử dụng");
        sqlTitle.setFont(MainGui.FONT_BOLD);
        sqlTitle.setForeground(MainGui.ACCENT);
        lblTechnique.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblTechnique.setForeground(new Color(0x475569));
        sqlHeader.add(sqlTitle);
        sqlHeader.add(lblTechnique);

        JPanel sqlCard = new JPanel(new BorderLayout(0, 8));
        sqlCard.setBackground(Color.WHITE);
        sqlCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainGui.BORDER_COL),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        sqlCard.add(sqlHeader, BorderLayout.NORTH);
        sqlCard.add(sqlScroll, BorderLayout.CENTER);

        // ---- Right Card: Kết quả trả về ----
        tblResult.setModel(tableModel);
        tblResult.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tblResult.setFillsViewportHeight(true);
        JScrollPane resultScroll = new JScrollPane(
                tblResult,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        resultScroll.setBorder(BorderFactory.createLineBorder(MainGui.BORDER_COL));

        JPanel resultCard = new JPanel(new BorderLayout(0, 8));
        resultCard.setBackground(Color.WHITE);
        resultCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainGui.BORDER_COL),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        JLabel resTitle = new JLabel("Kết quả truy vấn từ PostgreSQL");
        resTitle.setFont(MainGui.FONT_BOLD);
        resTitle.setForeground(MainGui.ACCENT);
        resultCard.add(resTitle, BorderLayout.NORTH);
        resultCard.add(resultScroll, BorderLayout.CENTER);

        // ---- Split Pane (Trái: SQL | Phải: Kết quả) ----
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sqlCard, resultCard);
        split.setResizeWeight(0.42);
        split.setDividerSize(6);
        split.setBorder(null);

        add(northContainer, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
    }

    private JLabel boldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(MainGui.FONT_BOLD);
        return lbl;
    }

    private void onQueryChanged() {
        QuerySpec spec = (QuerySpec) cboQuery.getSelectedItem();
        if (spec == null) return;

        areaSql.setText(spec.sql());
        areaSql.setCaretPosition(0);
        lblTechnique.setText("Kỹ thuật: " + spec.technique());

        CardLayout cl = (CardLayout) paramBar.getLayout();
        if ("Q09".equals(spec.id())) {
            cl.show(paramBar, "Q09");
        } else if ("Q10".equals(spec.id())) {
            cl.show(paramBar, "Q10");
        } else {
            cl.show(paramBar, "NONE");
        }
        paramBar.revalidate();
        paramBar.repaint();
    }

    private record QueryResult(String[] columns, List<Object[]> rows, double elapsedMs, String error) {}

    private void executeSelectedQuery() {
        QuerySpec spec = (QuerySpec) cboQuery.getSelectedItem();
        if (spec == null) return;

        btnRun.setEnabled(false);
        lblMetrics.setForeground(new Color(0xD97706));
        lblMetrics.setText("⏳ Đang thực thi " + spec.id() + "...");

        String maBD = txtMaBanDoc.getText().trim().isEmpty() ? "BD01" : txtMaBanDoc.getText().trim();
        String kw = txtTuKhoa.getText().trim().isEmpty() ? "Database" : txtTuKhoa.getText().trim();
        int namTu = 2018;
        try {
            namTu = Integer.parseInt(txtNamTu.getText().trim());
        } catch (NumberFormatException ignored) { }
        String sort = String.valueOf(cboSapXep.getSelectedItem());
        final int finalNamTu = namTu;

        new SwingWorker<QueryResult, Void>() {
            @Override
            protected QueryResult doInBackground() {
                long startNs = System.nanoTime();
                try (Connection conn = DatabaseConfig.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(spec.sql())) {

                    if ("Q09".equals(spec.id())) {
                        stmt.setString(1, maBD);
                        stmt.setString(2, maBD);
                    } else if ("Q10".equals(spec.id())) {
                        stmt.setString(1, kw);
                        stmt.setInt(2, finalNamTu);
                        stmt.setString(3, sort);
                    }

                    try (ResultSet rs = stmt.executeQuery()) {
                        ResultSetMetaData meta = rs.getMetaData();
                        int colCount = meta.getColumnCount();
                        String[] cols = new String[colCount];
                        for (int i = 0; i < colCount; i++) {
                            cols[i] = meta.getColumnLabel(i + 1);
                        }
                        List<Object[]> rows = new ArrayList<>();
                        while (rs.next()) {
                            Object[] row = new Object[colCount];
                            for (int i = 0; i < colCount; i++) {
                                Object val = rs.getObject(i + 1);
                                row[i] = (val == null) ? "NULL" : val.toString();
                            }
                            rows.add(row);
                        }
                        double elapsedMs = (System.nanoTime() - startNs) / 1_000_000.0;
                        return new QueryResult(cols, rows, elapsedMs, null);
                    }
                } catch (SQLException ex) {
                    double elapsedMs = (System.nanoTime() - startNs) / 1_000_000.0;
                    return new QueryResult(new String[0], List.of(), elapsedMs, ex.getMessage());
                }
            }

            @Override
            protected void done() {
                btnRun.setEnabled(true);
                try {
                    QueryResult res = get();
                    if (res.error() != null) {
                        lblMetrics.setForeground(MainGui.DANGER);
                        lblMetrics.setText(String.format("❌ Lỗi (%.2f ms)", res.elapsedMs()));
                        tableModel.setDataVector(
                                new Object[][]{{res.error()}},
                                new String[]{"Thông báo lỗi SQL"}
                        );
                        tblResult.getColumnModel().getColumn(0).setPreferredWidth(520);
                        return;
                    }

                    Object[][] data = res.rows().toArray(new Object[0][]);
                    tableModel.setDataVector(data, res.columns());
                    autoSizeColumns(res.columns(), data);

                    lblMetrics.setForeground(MainGui.SUCCESS);
                    lblMetrics.setText(String.format(
                            "⏱ Thời gian: %.2f ms  |  📊 Số dòng: %d",
                            res.elapsedMs(), res.rows().size()));
                } catch (Exception ex) {
                    lblMetrics.setForeground(MainGui.DANGER);
                    lblMetrics.setText("❌ Lỗi: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void autoSizeColumns(String[] cols, Object[][] data) {
        for (int c = 0; c < cols.length; c++) {
            int maxLen = cols[c].length();
            for (Object[] row : data) {
                if (row[c] != null) {
                    maxLen = Math.max(maxLen, row[c].toString().length());
                }
            }
            int width = Math.min(480, Math.max(100, maxLen * 8 + 24));
            tblResult.getColumnModel().getColumn(c).setPreferredWidth(width);
        }
    }
}
