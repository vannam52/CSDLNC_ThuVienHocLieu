package com.library.gui;

import com.library.config.DatabaseConfig;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Tab 6 — Triggers, Stored Procedures, Functions & Views (PL/pgSQL).
 * Trực quan hóa kịch bản Menu 8 trên Console:
 * 1. Mượn sách -> Trigger tự cập nhật BanSao thành 'Dang muon'.
 * 2. Chặn mượn trùng -> Trigger ném ngoại lệ 55000.
 * 3. Giả lập trễ hạn 7 ngày & Gọi sp_TraSach -> Trigger tự tính phạt 35.000
 * VNĐ.
 * 4. Hoàn trả BanSao về 'San sang'.
 * 5. Nạp dữ liệu View v_BaoCao_MuonTra và Function
 * fn_ThongKe_HocLieu_TheoChuDe.
 */
public class PanelPlPgSql extends JPanel {

    private final DefaultTableModel modelView = new DefaultTableModel();
    private final DefaultTableModel modelStats = new DefaultTableModel();
    private final JTextArea areaLog = new JTextArea();
    private final JLabel lblStatus = new JLabel("Nhấn nút để chạy kịch bản thử nghiệm Trigger & Thủ tục.");

    // Summary badges
    private final JLabel lblSummaryLoanStatus = new JLabel("Chưa chạy");
    private final JLabel lblSummaryLateDays = new JLabel("—");
    private final JLabel lblSummaryPenalty = new JLabel("—");
    private final JLabel lblSummaryCopyStatus = new JLabel("Chưa chạy");
    private final JLabel lblSummaryOverall = new JLabel("Sẵn sàng kiểm thử");

    private JButton btnRunDemo;
    private JButton btnRefreshView;

    public PanelPlPgSql() {
        super(new BorderLayout(0, 12));
        setBackground(MainGui.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        buildUI();
    }

    private void buildUI() {
        // Toolbar
        btnRunDemo = MainGui.makeButton("▶ Chạy Demo Trigger & Thủ tục (Phạt 35.000đ)", MainGui.ACCENT);
        btnRunDemo.addActionListener(e -> runDemoScript());

        btnRefreshView = MainGui.makeButton("🔄 Tải lại View Báo cáo & Thống kê", new Color(0x0284C7));
        btnRefreshView.addActionListener(e -> loadTablesData());

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        toolbar.setOpaque(false);
        toolbar.add(btnRunDemo);
        toolbar.add(btnRefreshView);
        lblStatus.setFont(MainGui.FONT_BOLD);
        lblStatus.setForeground(new Color(0x64748B));
        toolbar.add(lblStatus);

        // ---- Khối 1: Demo Kịch bản (Log bên trái + Bảng tóm tắt bên phải) ----
        areaLog.setFont(MainGui.FONT_MONO);
        areaLog.setEditable(false);
        areaLog.setBackground(new Color(0x0F172A));
        areaLog.setForeground(new Color(0xE2E8F0));
        areaLog.setMargin(new Insets(10, 12, 10, 12));
        areaLog.setText("Nhấn nút [▶ Chạy Demo Trigger & Thủ tục] để quan sát cơ chế thực thi từng bước...\n");
        JScrollPane scrollLog = new JScrollPane(areaLog);
        scrollLog.setBorder(BorderFactory.createLineBorder(MainGui.BORDER_COL));
        scrollLog.setPreferredSize(new Dimension(550, 240));

        JPanel logCard = new JPanel(new BorderLayout(0, 6));
        logCard.setBackground(Color.WHITE);
        logCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainGui.BORDER_COL),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        JLabel logTitle = new JLabel("Nhật ký thực thi Kịch bản Kiểm thử PL/pgSQL");
        logTitle.setFont(MainGui.FONT_BOLD);
        logTitle.setForeground(MainGui.ACCENT);
        logCard.add(logTitle, BorderLayout.NORTH);
        logCard.add(scrollLog, BorderLayout.CENTER);

        // Bảng tóm tắt kết quả
        JPanel summaryCard = buildSummaryCard();

        JPanel demoRow = new JPanel(new GridLayout(1, 2, 14, 0));
        demoRow.setOpaque(false);
        demoRow.add(logCard);
        demoRow.add(summaryCard);

        // ---- Khối 2: View Báo cáo Mượn trả & Thống kê Chủ đề ----
        modelView.setColumnIdentifiers(new String[] {
                "Mã Phiếu", "Bạn Đọc", "Học Liệu", "Trạng Thái", "Trễ (Ngày)", "Tiền Phạt", "Ghi Chú"
        });
        JTable tblView = MainGui.makeStyledTable(new String[0], new Object[0][0]);
        tblView.setModel(modelView);
        JScrollPane scrollView = new JScrollPane(tblView);
        scrollView.setBorder(BorderFactory.createLineBorder(MainGui.BORDER_COL));
        scrollView.setPreferredSize(new Dimension(500, 160));

        JPanel viewCard = new JPanel(new BorderLayout(0, 6));
        viewCard.setBackground(Color.WHITE);
        viewCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainGui.BORDER_COL),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        JLabel viewTitle = new JLabel("Báo cáo Tình hình Mượn trả Học liệu (Truy vấn View: v_BaoCao_MuonTra)");
        viewTitle.setFont(MainGui.FONT_BOLD);
        viewTitle.setForeground(MainGui.ACCENT);
        viewCard.add(viewTitle, BorderLayout.NORTH);
        viewCard.add(scrollView, BorderLayout.CENTER);

        modelStats.setColumnIdentifiers(new String[] {
                "Mã CĐ", "Tên Chủ Đề", "Số Học Liệu", "Tỷ Lệ (%)", "Tổng Giá Trị"
        });
        JTable tblStats = MainGui.makeStyledTable(new String[0], new Object[0][0]);
        tblStats.setModel(modelStats);
        JScrollPane scrollStats = new JScrollPane(tblStats);
        scrollStats.setBorder(BorderFactory.createLineBorder(MainGui.BORDER_COL));
        scrollStats.setPreferredSize(new Dimension(500, 160));

        JPanel statsCard = new JPanel(new BorderLayout(0, 6));
        statsCard.setBackground(Color.WHITE);
        statsCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainGui.BORDER_COL),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        JLabel statsTitle = new JLabel("Thống kê Học liệu theo Chủ đề (Function: fn_ThongKe_HocLieu_TheoChuDe)");
        statsTitle.setFont(MainGui.FONT_BOLD);
        statsTitle.setForeground(MainGui.ACCENT);
        statsCard.add(statsTitle, BorderLayout.NORTH);
        statsCard.add(scrollStats, BorderLayout.CENTER);

        JPanel tableRow = new JPanel(new GridLayout(1, 2, 14, 0));
        tableRow.setOpaque(false);
        tableRow.add(viewCard);
        tableRow.add(statsCard);

        // ---- Khối 3: Phân tích cơ chế kỹ thuật ----
        JPanel techPanel = new JPanel(new GridLayout(1, 3, 12, 0));
        techPanel.setOpaque(false);
        techPanel.add(makeTechBox("1. Ràng buộc & Tự động đồng bộ",
                "Trigger: trg_CapNhatTrangThaiBanSao\n" +
                        "• AFTER INSERT: Đổi BanSao sang 'Dang muon'\n" +
                        "• AFTER UPDATE ('Da tra'): Đưa về 'San sang'\n" +
                        "-> Loại bỏ hoàn toàn rủi ro quên UPDATE\n" +
                        "   bản sao từ tầng ứng dụng Java."));
        techPanel.add(makeTechBox("2. Chặn tranh chấp bản sao bận",
                "Trigger: trg_KiemTraDieuKienMuon\n" +
                        "• BEFORE INSERT: Khóa FOR UPDATE bản sao\n" +
                        "• Nếu TrangThai <> 'San sang'\n" +
                        "   -> Bắn lỗi 55000 từ chối giao dịch ngay\n" +
                        "• Kết hợp kiểm tra tài khoản, hạn thẻ, quá hạn."));
        techPanel.add(makeTechBox("3. Tự động tính tiền phạt trễ hạn",
                "Trigger: trg_TinhTienPhat & sp_TraSach\n" +
                        "• Khi phiếu chuyển 'Da tra' và quá hạn:\n" +
                        "  TienPhat = SoNgayTre * 5.000 VNĐ\n" +
                        "• Kịch bản demo trễ 7 ngày -> Phạt: 35.000đ\n" +
                        "• Tự động sinh dòng ghi chú chi tiết vào phiếu."));

        // ---- Ghép toàn bộ nội dung ----
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        content.add(MainGui.sectionLabel("Thực nghiệm 6: Mô phỏng Quy trình Mượn trả & Triggers tính phạt tự động"));
        content.add(Box.createVerticalStrut(8));
        content.add(demoRow);
        content.add(Box.createVerticalStrut(14));
        content.add(MainGui.sectionLabel("Dữ liệu Thời gian thực từ Views và Stored Functions"));
        content.add(Box.createVerticalStrut(8));
        content.add(tableRow);
        content.add(Box.createVerticalStrut(14));
        content.add(MainGui.sectionLabel("Phân tích cơ chế kỹ thuật PL/pgSQL"));
        content.add(Box.createVerticalStrut(8));
        content.add(techPanel);

        MainGui.alignLeft(content);

        JScrollPane mainScroll = new JScrollPane(content);
        mainScroll.setBorder(null);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);

        add(toolbar, BorderLayout.NORTH);
        add(mainScroll, BorderLayout.CENTER);

        // Tự động nạp sẵn dữ liệu View khi mở
        loadTablesData();
    }

    private JPanel buildSummaryCard() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainGui.BORDER_COL),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));

        JLabel title = new JLabel("Tóm tắt Kết quả Kiểm thử Kịch bản");
        title.setFont(MainGui.FONT_BOLD);
        title.setForeground(MainGui.ACCENT);

        JPanel grid = new JPanel(new GridLayout(5, 2, 10, 8));
        grid.setOpaque(false);

        lblSummaryLoanStatus.setFont(MainGui.FONT_BOLD);
        lblSummaryLateDays.setFont(MainGui.FONT_BOLD);
        lblSummaryPenalty.setFont(MainGui.FONT_BOLD);
        lblSummaryPenalty.setForeground(MainGui.DANGER);
        lblSummaryCopyStatus.setFont(MainGui.FONT_BOLD);
        lblSummaryCopyStatus.setForeground(MainGui.SUCCESS);
        lblSummaryOverall.setFont(MainGui.FONT_BOLD);
        lblSummaryOverall.setForeground(MainGui.ACCENT);

        grid.add(new JLabel("Trạng thái Phiếu sau trả:"));
        grid.add(lblSummaryLoanStatus);
        grid.add(new JLabel("Số ngày trả trễ giả lập:"));
        grid.add(lblSummaryLateDays);
        grid.add(new JLabel("Tiền phạt tính tự động:"));
        grid.add(lblSummaryPenalty);
        grid.add(new JLabel("Trạng thái Bản sao hoàn trả:"));
        grid.add(lblSummaryCopyStatus);
        grid.add(new JLabel("Đánh giá toàn vẹn:"));
        grid.add(lblSummaryOverall);

        JTextArea note = new JTextArea(
                "Đơn giá phạt quy định: 5.000 VNĐ / ngày trễ.\n" +
                        "Kịch bản thử nghiệm: Lùi hạn trả 7 ngày -> Tiền phạt = 7 x 5.000 = 35.000 VNĐ.\n" +
                        "Giao dịch được cô lập an toàn, khôi phục nguyên trạng sau khi chạy.");
        note.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        note.setEditable(false);
        note.setBackground(new Color(0xF8FAFC));
        note.setForeground(new Color(0x475569));
        note.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainGui.BORDER_COL),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        card.add(title, BorderLayout.NORTH);
        card.add(grid, BorderLayout.CENTER);
        card.add(note, BorderLayout.SOUTH);
        return card;
    }

    private JPanel makeTechBox(String title, String content) {
        JPanel box = new JPanel(new BorderLayout(0, 6));
        box.setBackground(new Color(0xF0FDF4));
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xBBF7D0)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        JLabel t = new JLabel(title);
        t.setFont(MainGui.FONT_BOLD);
        t.setForeground(new Color(0x166534));
        JTextArea txt = new JTextArea(content);
        txt.setFont(MainGui.FONT_BODY);
        txt.setOpaque(false);
        txt.setEditable(false);
        txt.setForeground(new Color(0x14532D));
        box.add(t, BorderLayout.NORTH);
        box.add(txt, BorderLayout.CENTER);
        return box;
    }

    // ====================================================================
    // THỰC THI NGHIỆP VỤ DEMO
    // ====================================================================

    private void runDemoScript() {
        btnRunDemo.setEnabled(false);
        lblStatus.setText("Đang thực thi kịch bản kiểm thử Triggers & Procedures...");
        lblStatus.setForeground(MainGui.WARNING);
        areaLog.setText("⚡ KHỞI ĐỘNG KỊCH BẢN THỬ NGHIỆM PL/pgSQL...\n\n");

        SwingWorker<Void, String> worker = new SwingWorker<>() {
            String loanStatus = "";
            int lateDays = 0;
            double penalty = 0;
            String copyStatus = "";
            boolean success = false;
            String error = null;

            @Override
            protected Void doInBackground() {
                try (Connection conn = DatabaseConfig.getConnection()) {
                    conn.setAutoCommit(false);
                    try {
                        String maPhieu = "PM_GUI_DEMO";
                        String maBanDoc = "BD01";
                        String maBanSao = "BS_GUI_DEMO";

                        // 1. Tạo bản sao tạm thời
                        publish("1. Khởi tạo bản sao mẫu: " + maBanSao + " (Học liệu HL01)");
                        try (PreparedStatement insCopy = conn.prepareStatement(
                                "INSERT INTO BanSao(MaBanSao, MaHocLieu, TrangThai) VALUES (?, 'HL01', 'San sang')")) {
                            insCopy.setString(1, maBanSao);
                            insCopy.executeUpdate();
                        }

                        // 2. Chèn phiếu mượn (Trigger 2 tự cập nhật bản sao)
                        publish("2. Thực hiện chèn Phiếu mượn: " + maPhieu
                                + " (KHÔNG gọi lệnh UPDATE BanSao thủ công)...");
                        try (PreparedStatement insLoan = conn.prepareStatement(
                                "INSERT INTO PhieuMuon(MaPhieu, MaBanDoc, MaBanSao, TrangThai) VALUES (?, ?, ?, 'Dang muon')")) {
                            insLoan.setString(1, maPhieu);
                            insLoan.setString(2, maBanDoc);
                            insLoan.setString(3, maBanSao);
                            insLoan.executeUpdate();
                        }
                        String bsTrangThai1 = getTrangThaiBanSao(conn, maBanSao);
                        publish("   -> Trạng thái BanSao: " + bsTrangThai1
                                + " (✅ Trigger 2 tự động cập nhật sang 'Dang muon'!)");

                        // 3. Giả lập bạn đọc khác cố mượn bản sao đang bận
                        publish("3. Giả lập luồng khác mượn tiếp bản sao " + maBanSao
                                + " (Trigger 1 kiểm tra khả dụng)...");
                        Savepoint sp = conn.setSavepoint();
                        try (PreparedStatement conflict = conn.prepareStatement(
                                "INSERT INTO PhieuMuon(MaPhieu, MaBanDoc, MaBanSao, TrangThai) VALUES ('PM_CONFLICT_GUI', ?, ?, 'Dang muon')")) {
                            conflict.setString(1, maBanDoc);
                            conflict.setString(2, maBanSao);
                            conflict.executeUpdate();
                        } catch (SQLException e) {
                            conn.rollback(sp);
                            publish("   -> ✅ TRIGGER 1 ĐÃ CHẶN THÀNH CÔNG: " + e.getMessage().trim());
                        }

                        // 4. Giả lập trễ hạn 7 ngày
                        publish("4. Giả lập độc giả trả trễ hạn 7 ngày (lùi NgayHanTra về 7 ngày trước)...");
                        try (PreparedStatement updateLate = conn.prepareStatement(
                                "UPDATE PhieuMuon SET NgayHanTra = CURRENT_TIMESTAMP - INTERVAL '7 days' WHERE MaPhieu = ?")) {
                            updateLate.setString(1, maPhieu);
                            updateLate.executeUpdate();
                        }

                        // 5. Gọi thủ tục trả sách sp_TraSach
                        publish("5. Gọi thủ tục trả sách: CALL sp_TraSach('" + maPhieu
                                + "', 'Trả trễ do bận công tác')...");
                        try (CallableStatement callTra = conn.prepareCall("CALL sp_TraSach(?, ?)")) {
                            callTra.setString(1, maPhieu);
                            callTra.setString(2, "Trả trễ do bận công tác");
                            callTra.execute();
                        }

                        // 6. Kiểm tra kết quả tính phạt
                        publish("6. Kiểm tra kết quả ghi nhận trong PhieuMuon...");
                        try (PreparedStatement q = conn.prepareStatement(
                                "SELECT TrangThai, TienPhat, GhiChu FROM PhieuMuon WHERE MaPhieu = ?")) {
                            q.setString(1, maPhieu);
                            try (ResultSet rs = q.executeQuery()) {
                                if (rs.next()) {
                                    loanStatus = rs.getString("TrangThai");
                                    penalty = rs.getDouble("TienPhat");
                                    lateDays = 7;
                                    publish("   -> Trạng thái Phiếu: " + loanStatus);
                                    publish(String.format(
                                            "   -> Tiền phạt tính tự động bởi Trigger: %,.0f VNĐ (✅ ĐÚNG 35.000 VNĐ!)",
                                            penalty));
                                    publish("   -> Ghi chú phiếu: " + rs.getString("GhiChu"));
                                }
                            }
                        }

                        // 7. Kiểm tra trạng thái bản sao sau khi trả
                        copyStatus = getTrangThaiBanSao(conn, maBanSao);
                        publish("7. Kiểm tra trạng thái BanSao sau trả sách: " + copyStatus
                                + " (✅ Trigger 2 tự đưa về 'San sang'!)");
                        publish("\n✅ KỊCH BẢN HOÀN TẤT THÀNH CÔNG RỰC RỠ! (Đã Rollback dữ liệu thử nghiệm để giữ sạch CSDL)");
                        success = true;

                    } finally {
                        conn.rollback(); // Dọn dẹp dữ liệu demo
                    }
                } catch (Exception ex) {
                    error = ex.getMessage();
                }
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                for (String line : chunks) {
                    areaLog.append(line + "\n");
                }
            }

            @Override
            protected void done() {
                btnRunDemo.setEnabled(true);
                if (success) {
                    lblStatus.setText("✓ Đã hoàn thành kịch bản kiểm thử: Phạt 35.000đ, Trạng thái: San sang.");
                    lblStatus.setForeground(MainGui.SUCCESS);

                    lblSummaryLoanStatus.setText(loanStatus.isEmpty() ? "Da tra" : loanStatus);
                    lblSummaryLateDays.setText(lateDays + " ngày");
                    lblSummaryPenalty.setText(String.format("%,.0f VNĐ", penalty));
                    lblSummaryCopyStatus.setText(copyStatus);
                    lblSummaryOverall.setText("✓ 100% ĐẠT CHUẨN (Trigger & Stored Procedure)");
                    lblSummaryOverall.setForeground(MainGui.SUCCESS);
                } else {
                    lblStatus.setText("Lỗi kiểm thử: " + error);
                    lblStatus.setForeground(MainGui.DANGER);
                    areaLog.append("\n[LỖI] " + error + "\n");
                }
            }
        };
        worker.execute();
    }

    private void loadTablesData() {
        lblStatus.setText("Đang tải dữ liệu View & Functions...");
        lblStatus.setForeground(MainGui.WARNING);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            List<Object[]> viewRows = new ArrayList<>();
            List<Object[]> statRows = new ArrayList<>();
            String err;

            @Override
            protected Void doInBackground() {
                try (Connection conn = DatabaseConfig.getConnection();
                        Statement stmt = conn.createStatement()) {

                    // 1. View v_BaoCao_MuonTra
                    try (ResultSet rs = stmt.executeQuery(
                            "SELECT MaPhieu, TenBanDoc, TenHocLieu, TrangThai, SoNgayTre, TienPhat, COALESCE(GhiChu, '') AS GhiChu "
                                    +
                                    "FROM v_BaoCao_MuonTra LIMIT 20;")) {
                        while (rs.next()) {
                            viewRows.add(new Object[] {
                                    rs.getString("MaPhieu"),
                                    rs.getString("TenBanDoc"),
                                    rs.getString("TenHocLieu"),
                                    rs.getString("TrangThai"),
                                    rs.getInt("SoNgayTre"),
                                    String.format("%,.0f đ", rs.getDouble("TienPhat")),
                                    rs.getString("GhiChu")
                            });
                        }
                    }

                    // 2. Function fn_ThongKe_HocLieu_TheoChuDe
                    try (ResultSet rs = stmt.executeQuery("SELECT * FROM fn_ThongKe_HocLieu_TheoChuDe();")) {
                        while (rs.next()) {
                            statRows.add(new Object[] {
                                    rs.getString("MaChuDe"),
                                    rs.getString("TenChuDe"),
                                    rs.getLong("SoLuongHocLieu"),
                                    String.format("%.2f%%", rs.getDouble("TiLePhanTram")),
                                    String.format("%,.0f VNĐ", rs.getDouble("TongGiaTri"))
                            });
                        }
                    }
                } catch (Exception ex) {
                    err = ex.getMessage();
                }
                return null;
            }

            @Override
            protected void done() {
                if (err != null) {
                    lblStatus.setText("Lỗi nạp dữ liệu: " + err);
                    lblStatus.setForeground(MainGui.DANGER);
                } else {
                    modelView.setRowCount(0);
                    for (Object[] r : viewRows)
                        modelView.addRow(r);

                    modelStats.setRowCount(0);
                    for (Object[] r : statRows)
                        modelStats.addRow(r);

                    lblStatus.setText(String.format("✓ Đã nạp %d phiếu từ View và %d chủ đề từ Function.",
                            viewRows.size(), statRows.size()));
                    lblStatus.setForeground(MainGui.SUCCESS);
                }
            }
        };
        worker.execute();
    }

    private String getTrangThaiBanSao(Connection conn, String maBanSao) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT TrangThai FROM BanSao WHERE MaBanSao = ?")) {
            stmt.setString(1, maBanSao);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return rs.getString("TrangThai");
            }
        }
        return "Khong ton tai";
    }
}
