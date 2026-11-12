package com.library.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.library.config.DatabaseConfig;

public class TransactionDAO {
    private record Demo(String hocLieu, String banSao, String banDoc1, String banDoc2,
                        String phieu1, String phieu2) {
        Demo(String suffix) {
            this("HL_D" + suffix, "BS_D" + suffix, "BD_A" + suffix, "BD_B" + suffix,
                    "PM_A" + suffix, "PM_B" + suffix);
        }
    }

    public void demoConcurrentBorrow() {
        Demo demo = new Demo(UUID.randomUUID().toString().substring(0, 12));
        ExecutorService executor = Executors.newFixedThreadPool(2);
        boolean created = false;
        try {
            createDemo(demo);
            created = true;
            System.out.printf("%nMƯỢN BẢN SAO CUỐI CÙNG %s%n", demo.banSao());
            System.out.printf("Học liệu %s có %d bản sao sẵn sàng. Hai bạn đọc: %s và %s.%n",
                    demo.hocLieu(), countAvailable(demo), demo.banDoc1(), demo.banDoc2());

            CyclicBarrier start = new CyclicBarrier(2);
            Future<Boolean> first = executor.submit(
                    () -> borrow(start, demo.phieu1(), demo.banDoc1(), demo.banSao()));
            Future<Boolean> second = executor.submit(
                    () -> borrow(start, demo.phieu2(), demo.banDoc2(), demo.banSao()));
            boolean firstSucceeded = first.get();
            boolean secondSucceeded = second.get();
            int activeLoans = countActiveLoans(demo);
            if (firstSucceeded == secondSucceeded || activeLoans != 1) {
                throw new IllegalStateException("Kết quả bất thường: " + activeLoans + " phiếu đang hiệu lực.");
            }
            System.out.printf("Kết quả: 1 phiên thành công, 1 phiên bị chặn, %d phiếu đang hiệu lực.%n",
                    activeLoans);
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            System.err.println("Đã ngắt demo mượn đồng thời.");
        } catch (SQLException | ExecutionException | IllegalStateException error) {
            System.err.println("Lỗi demo mượn đồng thời: " + error.getMessage());
        } finally {
            executor.shutdownNow();
            if (created) {
                cleanup(demo);
            }
        }
    }

    private boolean borrow(CyclicBarrier start, String phieu, String banDoc, String banSao) throws Exception {
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement("CALL sp_MuonSach(?, ?, ?)")) {
            statement.setString(1, phieu);
            statement.setString(2, banDoc);
            statement.setString(3, banSao);
            statement.setQueryTimeout(10);
            start.await(10, TimeUnit.SECONDS);
            statement.execute();
            System.out.printf("%s mượn thành công, phiếu %s.%n", banDoc, phieu);
            return true;
        } catch (SQLException error) {
            if (!"55000".equals(error.getSQLState())) {
                throw error;
            }
            System.out.printf("%s bị chặn vì bản sao đã được mượn.%n", banDoc);
            return false;
        }
    }

    public void demoRollbackAndVerify() {
        Demo demo = new Demo(UUID.randomUUID().toString().substring(0, 12));
        boolean created = false;
        try {
            createDemo(demo);
            created = true;
            System.out.println("\nKIỂM THỬ ROLLBACK KHI MƯỢN SÁCH");
            try (Connection connection = DatabaseConfig.getConnection()) {
                connection.setAutoCommit(false);
                try {
                    execute(connection, "UPDATE BanSao SET TinhTrangVatLy = 'Thay đổi tạm' WHERE MaBanSao = ?",
                            demo.banSao());
                    System.out.println("Đã cập nhật tình trạng vật lý trong giao dịch.");
                    execute(connection, "INSERT INTO PhieuMuon(MaPhieu, MaBanDoc, MaBanSao) VALUES (?, ?, ?)",
                            demo.phieu1(), "MISSING_" + demo.banDoc1().substring(4), demo.banSao());
                    throw new IllegalStateException("Giao dịch thử nghiệm phải từ chối bạn đọc không tồn tại.");
                } catch (SQLException error) {
                    if (!"23503".equals(error.getSQLState())) {
                        throw error;
                    }
                    System.out.println("Lỗi dự kiến: " + error.getMessage());
                } finally {
                    connection.rollback();
                }
            }
            boolean restored = count(demo.banSao(), """
                    SELECT COUNT(*) FROM BanSao
                    WHERE MaBanSao = ? AND TrangThai = 'San sang' AND TinhTrangVatLy = 'Mới'
                    """) == 1 && countActiveLoans(demo) == 0;
            if (!restored) {
                throw new IllegalStateException("Dữ liệu sau rollback khác dữ liệu ban đầu.");
            }
            System.out.println("Rollback thành công: tình trạng vật lý ban đầu được khôi phục, 0 phiếu được tạo.");
        } catch (SQLException | IllegalStateException error) {
            System.err.println("Lỗi demo rollback: " + error.getMessage());
        } finally {
            if (created) {
                cleanup(demo);
            }
        }
    }

    private void createDemo(Demo demo) throws SQLException {
        try (Connection connection = DatabaseConfig.getConnection()) {
            connection.setAutoCommit(false);
            try {
                execute(connection, "INSERT INTO HocLieu(MaHocLieu, TieuDe, LoaiHocLieu) VALUES (?, 'Sách demo', 'Sach')",
                        demo.hocLieu());
                execute(connection, "INSERT INTO Sach(MaHocLieu, ISBN) VALUES (?, ?)",
                        demo.hocLieu(), "ISBN-" + demo.hocLieu());
                execute(connection, "INSERT INTO BanSao(MaBanSao, MaHocLieu) VALUES (?, ?)",
                        demo.banSao(), demo.hocLieu());
                for (String maBanDoc : new String[] {demo.banDoc1(), demo.banDoc2()}) {
                    execute(connection, """
                            INSERT INTO BanDoc(MaBanDoc, HoTen, Email, MatKhau, NgayHetHan)
                            VALUES (?, 'Bạn đọc demo', ?, 'DEMO', CURRENT_DATE + 30)
                            """, maBanDoc, maBanDoc + "@example.test");
                }
                connection.commit();
            } catch (SQLException error) {
                connection.rollback();
                throw error;
            }
        }
    }

    private void cleanup(Demo demo) {
        try (Connection connection = DatabaseConfig.getConnection()) {
            connection.setAutoCommit(false);
            try {
                execute(connection, "DELETE FROM PhieuMuon WHERE MaPhieu IN (?, ?)", demo.phieu1(), demo.phieu2());
                execute(connection, "DELETE FROM HocLieu WHERE MaHocLieu = ?", demo.hocLieu());
                execute(connection, "DELETE FROM BanDoc WHERE MaBanDoc IN (?, ?)", demo.banDoc1(), demo.banDoc2());
                connection.commit();
            } catch (SQLException error) {
                connection.rollback();
                throw error;
            }
        } catch (SQLException error) {
            System.err.println("Lỗi dọn dữ liệu demo: " + error.getMessage());
        }
    }

    private int countAvailable(Demo demo) throws SQLException {
        return count(demo.hocLieu(), "SELECT COUNT(*) FROM BanSao WHERE MaHocLieu = ? AND TrangThai = 'San sang'");
    }

    private int countActiveLoans(Demo demo) throws SQLException {
        return count(demo.banSao(), """
                SELECT COUNT(*) FROM PhieuMuon WHERE MaBanSao = ? AND TrangThai IN ('Dang muon', 'Qua han')
                """);
    }

    private int count(String id, String sql) throws SQLException {
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1);
            }
        }
    }

    private void execute(Connection connection, String sql, String... values) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 0; index < values.length; index++) {
                statement.setString(index + 1, values[index]);
            }
            statement.executeUpdate();
        }
    }
}
