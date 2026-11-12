package com.library.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;

import com.library.config.DatabaseConfig;

public class PlPgSqlDAO {

    public void demoTriggersAndFunctions() {
        System.out.println("\n==================================================================");
        System.out.println("⚡ DEMO PL/pgSQL: TRIGGERS, STORED FUNCTIONS, VIEWS & PROCEDURES");
        System.out.println("==================================================================");

        try (Connection conn = DatabaseConfig.getConnection()) {

            // 1. DEMO FUNCTION 1: Thống kê học liệu theo chủ đề (TABLE function)
            System.out.println("\n📊 1. GỌI FUNCTION: fn_ThongKe_HocLieu_TheoChuDe()");
            System.out.println("------------------------------------------------------------------");
            System.out.printf("%-10s | %-25s | %-12s | %-10s | %-15s%n", 
                    "Mã CĐ", "Tên Chủ Đề", "Số Học Liệu", "Tỷ Lệ (%)", "Tổng Giá Trị");
            System.out.println("------------------------------------------------------------------");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM fn_ThongKe_HocLieu_TheoChuDe();")) {
                while (rs.next()) {
                    System.out.printf("%-10s | %-25s | %-12d | %-9.2f%% | %,-15.0f VNĐ%n",
                            rs.getString("MaChuDe"),
                            rs.getString("TenChuDe"),
                            rs.getLong("SoLuongHocLieu"),
                            rs.getDouble("TiLePhanTram"),
                            rs.getDouble("TongGiaTri"));
                }
            }

            // 2. DEMO FUNCTION 2: Kiểm tra bạn đọc đủ điều kiện mượn
            System.out.println("\n🔍 2. GỌI FUNCTION: fn_KiemTra_CoTheMuon('BD01')");
            try (PreparedStatement stmt = conn.prepareStatement("SELECT fn_KiemTra_CoTheMuon(?) AS CoTheMuon;")) {
                stmt.setString(1, "BD01");
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        boolean coTheMuon = rs.getBoolean("CoTheMuon");
                        System.out.println("   -> Kết quả kiểm tra bạn đọc BD01: " + (coTheMuon ? "✅ HỢP LỆ (Đủ điều kiện mượn)" : "❌ KHÔNG ĐƯỢC MƯỢN"));
                    }
                }
            }

            // 3. DEMO FUNCTION 3 & 4: Tìm kiếm toàn văn GIN Trigram & Dynamic SQL
            System.out.println("\n🔎 3. GỌI FUNCTION TÌM KIẾM TOÀN VĂN GIN TRIGRAM: fn_TimKiem_HocLieu('Database')");
            try (PreparedStatement stmt = conn.prepareStatement("SELECT MaHocLieu, TieuDe, PhanLoai, DoTuongDong FROM fn_TimKiem_HocLieu(?);")) {
                stmt.setString(1, "Database");
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        System.out.printf("   -> [%s] %-28s | Phân loại: %-10s | Độ tương đồng: %.2f%n",
                                rs.getString("MaHocLieu"),
                                rs.getString("TieuDe"),
                                rs.getString("PhanLoai"),
                                rs.getDouble("DoTuongDong"));
                    }
                }
            }

            // 4. DEMO TRIGGER 1 & 2: Tự động cập nhật BanSao khi mượn & Chặn khi bản sao không sẵn sàng
            System.out.println("\n⚙️ 4. DEMO TRIGGER 1 & 2: TỰ ĐỘNG ĐỒNG BỘ TRẠNG THÁI & CHẶN BẢN SAO BẬN");
            String maPhieuDemo = "PM_DEMO_TRG";
            String maBanDoc = "BD01";
            String maBanSao = "BS_DEMO_TRG";
            conn.setAutoCommit(false);
            try (PreparedStatement insertCopy = conn.prepareStatement(
                    "INSERT INTO BanSao(MaBanSao, MaHocLieu) VALUES (?, 'HL01')")) {
                insertCopy.setString(1, maBanSao);
                insertCopy.executeUpdate();
            }

            System.out.println("   (a) Trạng thái ban đầu của " + maBanSao + ": " + getTrangThaiBanSao(conn, maBanSao));
            System.out.println("   (b) Thực hiện INSERT phiếu mượn " + maPhieuDemo + " (KHÔNG gọi lệnh UPDATE BanSao thủ công)...");
            
            try (PreparedStatement ins = conn.prepareStatement("INSERT INTO PhieuMuon(MaPhieu, MaBanDoc, MaBanSao) VALUES (?, ?, ?)")) {
                ins.setString(1, maPhieuDemo);
                ins.setString(2, maBanDoc);
                ins.setString(3, maBanSao);
                ins.executeUpdate();
                System.out.println("   -> Tạo phiếu mượn thành công!");
            }

            System.out.println("   (c) Kiểm tra trạng thái " + maBanSao + " sau khi INSERT: " + getTrangThaiBanSao(conn, maBanSao) + " (✅ Trigger tự động cập nhật!)");

            // Thử tạo phiếu thứ 2 với cùng bản sao -> Trigger chặn
            System.out.println("   (d) Thử chèn tiếp phiếu thứ 2 với cùng bản sao đang bận " + maBanSao + "...");
            Savepoint beforeConflict = conn.setSavepoint();
            try (PreparedStatement insConflict = conn.prepareStatement("INSERT INTO PhieuMuon(MaPhieu, MaBanDoc, MaBanSao) VALUES ('PM_CONFLICT', ?, ?)")) {
                insConflict.setString(1, maBanDoc);
                insConflict.setString(2, maBanSao);
                insConflict.executeUpdate();
                throw new IllegalStateException("Lẽ ra phiếu mượn trùng phải bị chặn.");
            } catch (SQLException e) {
                if (!"55000".equals(e.getSQLState())) {
                    throw e;
                }
                conn.rollback(beforeConflict);
                System.out.println("   -> ✅ TRIGGER ĐÃ CHẶN THÀNH CÔNG: " + e.getMessage().trim());
            }

            // 5. DEMO TRIGGER 3 & PROCEDURE sp_TraSach: Tự động tính tiền phạt khi trả trễ
            System.out.println("\n💰 5. DEMO TRIGGER 3: TỰ ĐỘNG TÍNH TIỀN PHẠT TRỄ HẠN KHI TRẢ SÁCH");
            System.out.println("   (a) Giả lập độc giả trả trễ 7 ngày (lùi NgayHanTra về 7 ngày trước)...");
            try (PreparedStatement stmtLate = conn.prepareStatement(
                    "UPDATE PhieuMuon SET NgayHanTra = CURRENT_TIMESTAMP - INTERVAL '7 days' WHERE MaPhieu = ?")) {
                stmtLate.setString(1, maPhieuDemo);
                stmtLate.executeUpdate();
            }

            System.out.println("   (b) Gọi thủ tục: CALL sp_TraSach('" + maPhieuDemo + "', 'Trả trễ hạn')...");
            try (CallableStatement callTra = conn.prepareCall("CALL sp_TraSach(?, ?)")) {
                callTra.setString(1, maPhieuDemo);
                callTra.setString(2, "Trả trễ hạn do bận công tác");
                callTra.execute();
            }

            try (PreparedStatement q = conn.prepareStatement(
                    "SELECT MaPhieu, TrangThai, NgayTraThucTe, TienPhat, GhiChu FROM PhieuMuon WHERE MaPhieu = ?")) {
                q.setString(1, maPhieuDemo);
                try (ResultSet rs = q.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("   -> Trạng thái phiếu: " + rs.getString("TrangThai"));
                        System.out.println("   -> Ngày trả thực tế: " + rs.getTimestamp("NgayTraThucTe"));
                        System.out.printf("   -> Tiền phạt tính tự động bởi Trigger: %,.0f VNĐ%n", rs.getDouble("TienPhat"));
                        System.out.println("   -> Ghi chú tự động: " + rs.getString("GhiChu"));
                    }
                }
            }
            System.out.println("   (c) Trạng thái " + maBanSao + " sau khi trả: " + getTrangThaiBanSao(conn, maBanSao) + " (✅ Trigger tự đưa về 'San sang')");

            // 6. DEMO VIEW v_BaoCao_MuonTra
            System.out.println("\n📋 6. TRUY VẤN VIEW TỔNG HỢP: v_BaoCao_MuonTra");
            System.out.println("------------------------------------------------------------------");
            System.out.printf("%-12s | %-15s | %-25s | %-10s | %-10s | %-10s%n",
                    "Mã Phiếu", "Bạn Đọc", "Học Liệu", "Trạng Thái", "Trễ (Ngày)", "Tiền Phạt");
            System.out.println("------------------------------------------------------------------");
            try (Statement stmtView = conn.createStatement();
                 ResultSet rs = stmtView.executeQuery("SELECT MaPhieu, TenBanDoc, TenHocLieu, TrangThai, SoNgayTre, TienPhat FROM v_BaoCao_MuonTra LIMIT 5;")) {
                while (rs.next()) {
                    System.out.printf("%-12s | %-15s | %-25s | %-10s | %-10d | %,-10.0f%n",
                            rs.getString("MaPhieu"),
                            rs.getString("TenBanDoc"),
                            rs.getString("TenHocLieu"),
                            rs.getString("TrangThai"),
                            rs.getInt("SoNgayTre"),
                            rs.getDouble("TienPhat"));
                }
            }

            conn.rollback();

            System.out.println("\n✅ HOÀN THÀNH DEMO BƯỚC 2: PL/pgSQL TRIGGERS & FUNCTIONS!");
            System.out.println("==================================================================");

        } catch (SQLException e) {
            System.err.println("Lỗi thực thi PL/pgSQL Demo: " + e.getMessage());
        }
    }

    private String getTrangThaiBanSao(Connection conn, String maBanSao) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT TrangThai FROM BanSao WHERE MaBanSao = ?")) {
            stmt.setString(1, maBanSao);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getString("TrangThai");
            }
        }
        return "UNKNOWN";
    }
}
