package com.library.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.library.config.DatabaseConfig;

public class PerformanceDAO {

    public void runPerformanceExperiment() {
        String searchPattern = "%Database%";
        String query = "SELECT MaHocLieu, TieuDe FROM HocLieu WHERE TieuDe LIKE ?";

        System.out.println("\n------------------------------------------------------------------");
        System.out.println("📊 ĐO HIỆU NĂNG TRUY VẤN VỚI PATTERN: " + searchPattern);
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Đảm bảo extension pg_trgm và index GIN tồn tại
            try (PreparedStatement stmtInit = conn.prepareStatement(
                    "CREATE EXTENSION IF NOT EXISTS pg_trgm; " +
                    "CREATE INDEX IF NOT EXISTS idx_hoclieu_tieude_trgm ON HocLieu USING gin (TieuDe gin_trgm_ops);")) {
                stmtInit.execute();
            }

            // 1. Đo thời gian thực thi thực tế
            long start = System.nanoTime();
            int count = 0;
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, searchPattern);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) count++;
            }
            long end = System.nanoTime();
            double durationMs = (end - start) / 1000000.0;

            System.out.println("✅ Tìm thấy: " + count + " học liệu phù hợp. Tổng thời gian ứng dụng: " + String.format("%.2f", durationMs) + " ms.");

            // 2. Kế hoạch thực thi 1: Quét tuần tự (Sequential Scan - Khi tắt Index)
            System.out.println("\n------------------------------------------------------------------");
            System.out.println("1️⃣  TRƯỜNG HỢP 1: QUÉT TUẦN TỰ (Sequential Scan)");
            try (PreparedStatement stmtSet = conn.prepareStatement("SET enable_bitmapscan = off;")) {
                stmtSet.execute();
            }
            try (PreparedStatement stmt = conn.prepareStatement("EXPLAIN ANALYZE " + query)) {
                stmt.setString(1, searchPattern);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    System.out.println("   " + rs.getString(1));
                }
            } finally {
                try (PreparedStatement stmtReset = conn.prepareStatement("RESET enable_bitmapscan;")) {
                    stmtReset.execute();
                }
            }

            // 3. Kế hoạch thực thi 2: Quét bằng Chỉ mục (Bitmap Index Scan - Dùng GIN Trigram)
            System.out.println("\n------------------------------------------------------------------");
            System.out.println("2️⃣  TRƯỜNG HỢP 2: TỐI ƯU VỚI CHỈ MỤC GIN TRIGRAM (Bitmap Index Scan)");
            try (PreparedStatement stmtSet = conn.prepareStatement("SET enable_seqscan = off;")) {
                stmtSet.execute();
            }
            try (PreparedStatement stmt = conn.prepareStatement("EXPLAIN ANALYZE " + query)) {
                stmt.setString(1, searchPattern);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    System.out.println("   " + rs.getString(1));
                }
            } finally {
                try (PreparedStatement stmtReset = conn.prepareStatement("RESET enable_seqscan;")) {
                    stmtReset.execute();
                }
            }

            System.out.println("------------------------------------------------------------------");
            System.out.println("💡 Kết luận: Chỉ mục GIN Trigram (`idx_hoclieu_tieude_trgm`) cho phép tìm kiếm chuỗi '%...%' ");
            System.out.println("   nhanh chóng thông qua cấu trúc cây n-gram thay vì phải đọc toàn bộ bảng vật lý.");
            System.out.println("------------------------------------------------------------------");
        } catch (SQLException e) {
            System.err.println("Lỗi thực nghiệm: " + e.getMessage());
        }
    }
}