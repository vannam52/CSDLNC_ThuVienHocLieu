package com.library.dao;

import com.library.config.DatabaseConfig;
import java.sql.*;

public class HocLieuDAO {

    // DEMO 1: Truy vấn đệ quy cây chủ đề (WITH RECURSIVE)
    public void getCayChuDe() {
        String sql = "WITH RECURSIVE CayChuDe AS (" +
                     "    SELECT MaChuDe, TenChuDe, ChuDeCha, 1 AS CapDo " +
                     "    FROM ChuDe WHERE ChuDeCha IS NULL " +
                     "    UNION ALL " +
                     "    SELECT c.MaChuDe, c.TenChuDe, c.ChuDeCha, ccd.CapDo + 1 " +
                     "    FROM ChuDe c JOIN CayChuDe ccd ON c.ChuDeCha = ccd.MaChuDe" +
                     ") " +
                     "SELECT * FROM CayChuDe ORDER BY CapDo, TenChuDe;";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("------------------------------------------------------------------");
            System.out.printf("%-10s | %-30s | %-10s | %-8s%n", "Mã CĐ", "Tên Chủ Đề", "Chủ Đề Cha", "Cấp Độ");
            System.out.println("------------------------------------------------------------------");

            boolean hasRows = false;
            while (rs.next()) {
                hasRows = true;
                String indent = "  ".repeat(rs.getInt("CapDo") - 1);
                System.out.printf("%-10s | %-30s | %-10s | %-8d%n",
                        rs.getString("MaChuDe"),
                        indent + "└─ " + rs.getString("TenChuDe"),
                        rs.getString("ChuDeCha") == null ? "Gốc" : rs.getString("ChuDeCha"),
                        rs.getInt("CapDo"));
            }
            if (!hasRows) {
                System.out.println("Không có chủ đề nào trong cơ sở dữ liệu.");
            }
            System.out.println("------------------------------------------------------------------");

        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn CSDL: " + e.getMessage());
        }
    }

    // DEMO 2: Thống kê học liệu đa chủ đề (Chuẩn 4NF)
    public void getHocLieuDaChuDe() {
        String sql = "SELECT h.MaHocLieu, h.TieuDe, COUNT(hc.MaChuDe) AS SoLuongChuDe " +
                     "FROM HocLieu h " +
                     "JOIN HocLieu_ChuDe hc ON h.MaHocLieu = hc.MaHocLieu " +
                     "GROUP BY h.MaHocLieu, h.TieuDe " +
                     "HAVING COUNT(hc.MaChuDe) > 1;";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("------------------------------------------------------------------");
            System.out.printf("%-12s | %-35s | %-15s%n", "Mã Học Liệu", "Tiêu Đề Học Liệu", "Số Chủ Đề");
            System.out.println("------------------------------------------------------------------");

            boolean hasRows = false;
            while (rs.next()) {
                hasRows = true;
                System.out.printf("%-12s | %-35s | %-15d%n",
                        rs.getString("MaHocLieu"),
                        rs.getString("TieuDe"),
                        rs.getInt("SoLuongChuDe"));
            }
            if (!hasRows) {
                System.out.println("Không có học liệu thuộc nhiều hơn một chủ đề.");
            }
            System.out.println("------------------------------------------------------------------");

        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn CSDL: " + e.getMessage());
        }
    }

    // DEMO 3: Thống kê lượt mượn (Yêu cầu đồ án 3)
    public void thongKeLuotMuon() {
        String sql = "SELECT h.MaHocLieu, h.TieuDe, COUNT(p.MaPhieu) AS LuotMuon " +
                     "FROM HocLieu h " +
                     "JOIN BanSao b ON h.MaHocLieu = b.MaHocLieu " +
                     "JOIN PhieuMuon p ON b.MaBanSao = p.MaBanSao " +
                     "GROUP BY h.MaHocLieu, h.TieuDe " +
                     "ORDER BY LuotMuon DESC;";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("------------------------------------------------------------------");
            System.out.printf("%-12s | %-35s | %-10s%n", "Mã Học Liệu", "Tiêu Đề", "Lượt Mượn");
            System.out.println("------------------------------------------------------------------");

            boolean hasRows = false;
            while (rs.next()) {
                hasRows = true;
                System.out.printf("%-12s | %-35s | %-10d%n",
                        rs.getString("MaHocLieu"),
                        rs.getString("TieuDe"),
                        rs.getInt("LuotMuon"));
            }
            if (!hasRows) {
                System.out.println("Chưa có lượt mượn nào.");
            }
            System.out.println("------------------------------------------------------------------");

        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn CSDL: " + e.getMessage());
        }
    }

    /**
     * Thực thi truy vấn và in kết quả dạng bảng căn lề tự động theo độ rộng dữ liệu.
     */
    private void executeAndPrintTable(String title, String sql, Object... params) {
        System.out.println("\n=== " + title + " ===");
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();
                String[] headers = new String[colCount];
                int[] widths = new int[colCount];

                for (int i = 0; i < colCount; i++) {
                    headers[i] = meta.getColumnLabel(i + 1);
                    widths[i] = headers[i].length();
                }

                java.util.List<String[]> rows = new java.util.ArrayList<>();
                while (rs.next()) {
                    String[] row = new String[colCount];
                    for (int i = 0; i < colCount; i++) {
                        String val = rs.getString(i + 1);
                        row[i] = (val == null) ? "NULL" : val;
                        if (row[i].length() > widths[i]) {
                            widths[i] = row[i].length();
                        }
                    }
                    rows.add(row);
                }

                int totalWidth = 0;
                for (int w : widths) totalWidth += w + 3;
                String line = "-".repeat(Math.max(40, totalWidth));

                System.out.println(line);
                StringBuilder headerSb = new StringBuilder();
                for (int i = 0; i < colCount; i++) {
                    headerSb.append(String.format("%-" + widths[i] + "s", headers[i]));
                    if (i < colCount - 1) headerSb.append(" | ");
                }
                System.out.println(headerSb);
                System.out.println(line);

                if (rows.isEmpty()) {
                    System.out.println("(Không có dữ liệu trả về)");
                } else {
                    for (String[] row : rows) {
                        StringBuilder rowSb = new StringBuilder();
                        for (int i = 0; i < colCount; i++) {
                            rowSb.append(String.format("%-" + widths[i] + "s", row[i]));
                            if (i < colCount - 1) rowSb.append(" | ");
                        }
                        System.out.println(rowSb);
                    }
                }
                System.out.println(line);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn CSDL: " + e.getMessage());
        }
    }

    // Q02: Truy vấn Đa hình kết nối nhiều bảng (Polymorphic Outer Join)
    public void getHocLieuDaHinh() {
        String sql = "SELECT h.MaHocLieu, h.TieuDe, h.NamXuatBan, h.GiaTien, " +
                "CASE " +
                "  WHEN s.MaHocLieu IS NOT NULL THEN 'Sách' " +
                "  WHEN l.MaHocLieu IS NOT NULL THEN 'Luận văn' " +
                "  WHEN v.MaHocLieu IS NOT NULL THEN 'Video' " +
                "  WHEN t.MaHocLieu IS NOT NULL THEN 'Tài liệu điện tử' " +
                "  ELSE 'Khác' " +
                "END AS LoaiHocLieu, " +
                "COALESCE(s.ISBN, 'N/A') AS ISBN, " +
                "COALESCE(s.SoTrang::TEXT, 'N/A') AS SoTrang, " +
                "COALESCE(l.DiemBaoVe::TEXT, 'N/A') AS DiemBaoVe, " +
                "COALESCE(v.ThoiLuong::TEXT || ' phút', 'N/A') AS ThoiLuongVideo " +
                "FROM HocLieu h " +
                "LEFT JOIN Sach s ON h.MaHocLieu = s.MaHocLieu " +
                "LEFT JOIN LuanVan l ON h.MaHocLieu = l.MaHocLieu " +
                "LEFT JOIN VideoBaiGiang v ON h.MaHocLieu = v.MaHocLieu " +
                "LEFT JOIN TaiLieuDienTu t ON h.MaHocLieu = t.MaHocLieu " +
                "ORDER BY h.MaHocLieu;";
        executeAndPrintTable("[Q02] Truy vấn Đa hình kết nối 4 lớp con (Polymorphic Outer Join)", sql);
    }

    // Q04: Thống kê tần suất mượn kết nối 3 bảng (kể cả học liệu chưa từng mượn)
    public void thongKeTanSuatMuonDayDu() {
        String sql = "SELECT h.MaHocLieu, h.TieuDe, COUNT(p.MaPhieu) AS TongLuotMuon, " +
                "COUNT(DISTINCT b.MaBanSao) AS TongSoBanSaoHienCo " +
                "FROM HocLieu h " +
                "JOIN BanSao b ON h.MaHocLieu = b.MaHocLieu " +
                "LEFT JOIN PhieuMuon p ON b.MaBanSao = p.MaBanSao " +
                "GROUP BY h.MaHocLieu, h.TieuDe " +
                "ORDER BY TongLuotMuon DESC, h.TieuDe;";
        executeAndPrintTable("[Q04] Thống kê tần suất mượn kết nối 3 bảng", sql);
    }

    // Q05: Truy vấn con tương quan phủ định (NOT EXISTS)
    public void getBanDocChuaMuon() {
        String sql = "SELECT bd.MaBanDoc, bd.HoTen, bd.Email, bd.LoaiTaiKhoan " +
                "FROM BanDoc bd " +
                "WHERE NOT EXISTS (SELECT 1 FROM PhieuMuon pm WHERE pm.MaBanDoc = bd.MaBanDoc) " +
                "ORDER BY bd.MaBanDoc;";
        executeAndPrintTable("[Q05] Bạn đọc chưa từng mượn học liệu (NOT EXISTS)", sql);
    }

    // Q06: Truy vấn con lồng nhau vô hướng (Scalar Subquery with AVG)
    public void getHocLieuGiaCaoHonTB() {
        String sql = "SELECT h.MaHocLieu, h.TieuDe, h.GiaTien, " +
                "ROUND((SELECT AVG(GiaTien) FROM HocLieu WHERE GiaTien > 0), 2) AS GiaTienTrungBinh " +
                "FROM HocLieu h " +
                "WHERE h.GiaTien > (SELECT AVG(GiaTien) FROM HocLieu WHERE GiaTien > 0) " +
                "ORDER BY h.GiaTien DESC;";
        executeAndPrintTable("[Q06] Học liệu có giá cao hơn trung bình (Scalar Subquery)", sql);
    }

    // Q07: Hàm cửa sổ phân tích và xếp hạng (Window Function: DENSE_RANK)
    public void getXepHangGiaTheoNam() {
        String sql = "SELECT h.MaHocLieu, h.TieuDe, h.NamXuatBan, h.GiaTien, " +
                "DENSE_RANK() OVER (PARTITION BY h.NamXuatBan ORDER BY h.GiaTien DESC) AS XepHangGiaTheoNam " +
                "FROM HocLieu h " +
                "WHERE h.GiaTien > 0 " +
                "ORDER BY h.NamXuatBan DESC, XepHangGiaTheoNam;";
        executeAndPrintTable("[Q07] Xếp hạng giá học liệu theo năm (DENSE_RANK)", sql);
    }

    // Q08: Gọi Function trả về bảng thống kê theo chủ đề
    public void getThongKeTheoChuDeFunction() {
        String sql = "SELECT MaChuDe, TenChuDe, SoLuongHocLieu, TiLePhanTram, TongGiaTri " +
                "FROM fn_ThongKe_HocLieu_TheoChuDe();";
        executeAndPrintTable("[Q08] Thống kê học liệu theo chủ đề (TABLE Function)", sql);
    }

    // Q09: Gọi Function kiểm tra tư cách bạn đọc
    public void kiemTraTuCachMuon(String maBanDoc) {
        String sql = "SELECT ? AS MaBanDoc, fn_KiemTra_CoTheMuon(?) AS DuDieuKienMuon;";
        executeAndPrintTable("[Q09] Kiểm tra tư cách mượn của bạn đọc (Scalar Function)", sql, maBanDoc, maBanDoc);
    }

    // Q10: Thực thi Stored Function chứa SQL Động
    public void timKiemHocLieuDong(String tieuDe, int namTu, String sapXepTheo) {
        String sql = "SELECT * FROM fn_TimKiem_HocLieu_Dong(p_TieuDe := ?, p_NamTu := ?, p_SapXepTheo := ?);";
        executeAndPrintTable("[Q10] Tìm kiếm học liệu động (Dynamic SQL Function)", sql, tieuDe, namTu, sapXepTheo);
    }

    // Q11: Truy vấn View đa hình tổng hợp kèm STRING_AGG
    public void getViewHocLieuDayDu() {
        String sql = "SELECT MaHocLieu, TieuDe, LoaiHocLieu, DanhSachTacGia, DanhSachChuDe, " +
                "TongSoBanSao, SoBanSaoKhaDung " +
                "FROM v_HocLieu_DayDu WHERE TongSoBanSao > 0;";
        executeAndPrintTable("[Q11] Báo cáo tổng hợp từ View v_HocLieu_DayDu", sql);
    }

    // Q12: Phân tích Kế hoạch Thực thi EXPLAIN (ANALYZE, BUFFERS)
    public void phanTichExplainGin() {
        String sql = "EXPLAIN (ANALYZE, BUFFERS) SELECT MaHocLieu, TieuDe FROM HocLieu WHERE TieuDe ILIKE '%Database%';";
        executeAndPrintTable("[Q12] Phân tích kế hoạch thực thi EXPLAIN (ANALYZE, BUFFERS)", sql);
    }
}