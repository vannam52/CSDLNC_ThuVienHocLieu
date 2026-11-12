package com.library.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.library.config.DatabaseConfig;

public class PerformanceDAO {
    private static final int SAMPLE_SIZE = 20_000;
    private static final int REPEATS = 5;
    private static final String QUERY =
            "SELECT MaHocLieu, TieuDe FROM demo_gin WHERE TieuDe ILIKE '%Database%'";

    public void runPerformanceExperiment() {
        try (Connection connection = DatabaseConfig.getConnection();
             Statement statement = connection.createStatement()) {
            connection.setAutoCommit(false);
            try {
                statement.execute("""
                        CREATE TEMP TABLE demo_gin ON COMMIT DROP AS
                        SELECT id AS MaHocLieu,
                               (CASE WHEN id %% 100 = 0 THEN 'Database ' ELSE 'Hoc lieu ' END)
                               || md5(id::text) AS TieuDe
                        FROM generate_series(1, %d) AS rows(id)
                        """.formatted(SAMPLE_SIZE));
                statement.execute("ANALYZE demo_gin");
                System.out.println("\nTHỰC NGHIỆM GIN TRIGRAM");
                try (ResultSet counts = statement.executeQuery(
                        "SELECT (SELECT COUNT(*) FROM demo_gin), COUNT(*) FROM (" + QUERY + ") AS matches")) {
                    counts.next();
                    System.out.printf("Số bản ghi: %d. Số dòng khớp: %d.%n", counts.getLong(1), counts.getLong(2));
                }
                System.out.println("Truy vấn: " + QUERY);
                measure(statement, "Trước khi tạo chỉ mục");
                statement.execute("CREATE INDEX demo_gin_tieude_idx ON demo_gin USING gin (TieuDe gin_trgm_ops)");
                measure(statement, "Sau khi tạo chỉ mục GIN");
            } finally {
                connection.rollback();
            }
        } catch (SQLException error) {
            System.err.println("Lỗi thực nghiệm GIN: " + error.getMessage());
        }
    }

    private void measure(Statement statement, String label) throws SQLException {
        String explain = "EXPLAIN (ANALYZE, BUFFERS) " + QUERY;
        // Làm nóng dữ liệu một lượt trước mỗi nhóm đo.
        try (ResultSet warmup = statement.executeQuery(explain)) {
            while (warmup.next()) {
                // Chỉ dùng lượt này để làm nóng dữ liệu.
            }
        }
        List<Double> times = new ArrayList<>(REPEATS);
        System.out.println("\n" + label);
        for (int run = 0; run < REPEATS; run++) {
            try (ResultSet plan = statement.executeQuery(explain)) {
                while (plan.next()) {
                    String line = plan.getString(1);
                    if (run == 0) {
                        System.out.println(line);
                    }
                    if (line.startsWith("Execution Time: ")) {
                        times.add(Double.parseDouble(line.substring("Execution Time: ".length(), line.length() - 3)));
                    }
                }
            }
        }
        if (times.size() != REPEATS) {
            throw new SQLException("Không lấy được đủ thời gian thực thi từ EXPLAIN.");
        }
        List<Double> sorted = new ArrayList<>(times);
        Collections.sort(sorted);
        System.out.printf(Locale.ROOT, "Execution Time (ms), %d lần: %s. Trung vị: %.3f ms.%n",
                REPEATS, times, sorted.get(REPEATS / 2));
    }
}
