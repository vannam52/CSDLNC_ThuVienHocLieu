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

            System.out.println("✅ Kết quả: " + count + " dòng. Thời gian: " + durationMs + " ms.");

            // 2. Xuất kế hoạch thực thi EXPLAIN ANALYZE
            System.out.println("\n🔍 EXPLAIN ANALYZE (Chi tiết kế hoạch thực thi):");
            try (PreparedStatement stmt = conn.prepareStatement("EXPLAIN ANALYZE " + query)) {
                stmt.setString(1, searchPattern);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    System.out.println("   " + rs.getString(1));
                }
            }

            System.out.println("------------------------------------------------------------------");
        } catch (SQLException e) {
            System.err.println("Lỗi thực nghiệm: " + e.getMessage());
        }
    }
}