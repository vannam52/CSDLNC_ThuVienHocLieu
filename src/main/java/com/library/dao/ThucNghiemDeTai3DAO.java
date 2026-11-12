package com.library.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.library.config.DatabaseConfig;

public class ThucNghiemDeTai3DAO {
    public void runDemo() {
        try (Connection connection = DatabaseConfig.getConnection();
             Statement statement = connection.createStatement()) {
            System.out.println("\nTHỰC NGHIỆM CHUẨN HÓA 4NF");
            printRows(statement, "Bảng phẳng", """
                    SELECT MaHocLieu, MaTacGia, MaChuDe
                    FROM HocLieu_ChuaChuanHoa_4NF ORDER BY MaTacGia, MaChuDe
                    """);
            printRows(statement, "Kết quả nối", """
                    SELECT MaHocLieu, MaTacGia, MaChuDe
                    FROM v_ThucNghiem4NF_KhoiPhuc ORDER BY MaTacGia, MaChuDe
                    """);
            try (ResultSet difference = statement.executeQuery("SELECT COUNT(*) FROM v_ThucNghiem4NF_ChenhLech")) {
                difference.next();
                System.out.printf("Số dòng chênh lệch (EXCEPT ALL hai chiều): %d%n", difference.getLong(1));
            }

            System.out.println("\nTHỰC NGHIỆM ÁNH XẠ KẾ THỪA");
            printRows(statement, "Ô NULL trong các thuộc tính lớp con của Single Table", """
                    SELECT MaHocLieu, LoaiHocLieu, SoCotLopConBiNull, TongCotLopCon
                    FROM v_ThucNghiemKeThua_Null ORDER BY MaHocLieu
                    """);
            try (ResultSet summary = statement.executeQuery("""
                    SELECT COALESCE(SUM(SoCotLopConBiNull), 0), COALESCE(SUM(TongCotLopCon), 0)
                    FROM v_ThucNghiemKeThua_Null
                    """)) {
                summary.next();
                long nulls = summary.getLong(1);
                long total = summary.getLong(2);
                if (total > 0) {
                    System.out.printf("Tổng: %d/%d ô NULL (%.2f%%).%n", nulls, total, 100.0 * nulls / total);
                }
            }
            System.out.println("Single Table kiểm tra ISBN theo loại. Joined Table đặt NOT NULL và UNIQUE ở bảng Sach.");
            System.out.println("Xem hai kế hoạch trên cùng dữ liệu mẫu trong 06_thuc_nghiem_de_tai_3.sql.");
        } catch (SQLException error) {
            System.err.println("Lỗi thực nghiệm Đề tài 3: " + error.getMessage());
        }
    }

    private void printRows(Statement statement, String heading, String sql) throws SQLException {
        System.out.println(heading);
        try (ResultSet rows = statement.executeQuery(sql)) {
            int columnCount = rows.getMetaData().getColumnCount();
            while (rows.next()) {
                for (int column = 1; column <= columnCount; column++) {
                    System.out.print((column == 1 ? "  " : " | ") + rows.getString(column));
                }
                System.out.println();
            }
        }
    }
}
