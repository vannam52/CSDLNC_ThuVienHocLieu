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
}