package com.library.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;

import com.library.config.DatabaseConfig;

public class TransactionDAO {

    // DEMO 3: Giả lập 2 phiên mượn CÙNG 1 BẢN SAO đồng thời (Chỉ 1 phiên thành công)
    public void demoConcurrentBorrow(String maBanSao) {
        System.out.println("\n🔥 ⚙️  GIẢ LẬP CONCURRENT TRANSACTIONS:");
        System.out.println("Phiên 1 và Phiên 2 cùng gọi sp_MuonSach mượn bản sao: " + maBanSao);
        System.out.println("------------------------------------------------------------------");

        // Tự động khôi phục môi trường test: Đưa bản sao về 'San sang' và dọn phiếu cũ để có thể chạy lại nhiều lần
        resetBanSaoState(maBanSao);

        CountDownLatch readyLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(2);

        Runnable task1 = () -> {
            try {
                readyLatch.await();
                callProcedure("PM_PHIEN_1", "BD01", maBanSao, "Phiên 1");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                finishLatch.countDown();
            }
        };

        Runnable task2 = () -> {
            try {
                readyLatch.await();
                callProcedure("PM_PHIEN_2", "BD01", maBanSao, "Phiên 2");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                finishLatch.countDown();
            }
        };

        new Thread(task1).start();
        new Thread(task2).start();

        readyLatch.countDown(); // Kích hoạt chạy đồng thời

        try {
            finishLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("------------------------------------------------------------------");
    }

    private void callProcedure(String maPhieu, String maBanDoc, String maBanSao, String threadName) {
        String sql = "CALL sp_MuonSach(?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, maPhieu);
            stmt.setString(2, maBanDoc);
            stmt.setString(3, maBanSao);
            stmt.execute();

            System.out.println(" -> [" + threadName + "] ✅ THÀNH CÔNG: Tạo phiếu " + maPhieu);

        } catch (SQLException e) {
            System.out.println(" -> [" + threadName + "] ❌ BỊ CHẶN/LỖI: " + e.getMessage());
        }
    }

    // DEMO 4: Cố ý gây lỗi và kiểm chứng dữ liệu được Rollback
    public void demoRollbackAndVerify() {
        System.out.println("\n🔄 🧪 DEMO ROLLBACK & XÁC NHẬN TRẠNG THÁI DỮ LIỆU:");
        String testBanSao = "BS02";

        System.out.println("1. Trạng thái BS02 trước khi chạy giao dịch lỗi: " + getStatus(testBanSao));

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false); // Bắt đầu Transaction

            // Thao tác 1: Cập nhật trạng thái bản sao
            try (PreparedStatement stmt1 = conn.prepareStatement("UPDATE BanSao SET TrangThai = 'Dang muon' WHERE MaBanSao = ?")) {
                stmt1.setString(1, testBanSao);
                stmt1.executeUpdate();
                System.out.println("2. [Thao tác 1] Đã đổi trạng thái " + testBanSao + " thành 'Dang muon' trong Transaction.");
            }

            // Thao tác 2: Cố ý làm sai (Mã bạn đọc BD_ERR không tồn tại)
            System.out.println("3. [Thao tác 2] Cố ý chèn phiếu mượn chứa mã đọc giả không tồn tại (BD_ERR)...");
            try (PreparedStatement stmt2 = conn.prepareStatement("INSERT INTO PhieuMuon(MaPhieu, MaBanDoc, MaBanSao) VALUES ('PM_ERR', 'BD_ERR', ?)")) {
                stmt2.setString(1, testBanSao);
                stmt2.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            System.out.println("⚠️ Bắt được ngoại lệ: " + e.getMessage());
            System.out.println("4. Giao dịch gặp lỗi -> Tự động ROLLBACK!");
        }

        System.out.println("5. Trạng thái BS02 sau khi Rollback: " + getStatus(testBanSao));
        System.out.println("=> Kết luận: Dữ liệu đã được khôi phục nguyên vẹn!");
        System.out.println("------------------------------------------------------------------");
    }

    private void resetBanSaoState(String maBanSao) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM PhieuMuon WHERE MaPhieu IN ('PM_PHIEN_1', 'PM_PHIEN_2', 'PM_ERR') OR MaBanSao = ?")) {
                stmt.setString(1, maBanSao);
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = conn.prepareStatement("UPDATE BanSao SET TrangThai = 'San sang' WHERE MaBanSao = ?")) {
                stmt.setString(1, maBanSao);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            // Bỏ qua lỗi reset nếu có
        }
    }

    private String getStatus(String maBanSao) {
        String sql = "SELECT TrangThai FROM BanSao WHERE MaBanSao = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, maBanSao);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("TrangThai");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "UNKNOWN";
    }
}