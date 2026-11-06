package com.library.dao;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.library.config.ObjectDBConfig;
import com.library.model.BanDoc;
import com.library.model.BanSao;
import com.library.model.ChuDe;
import com.library.model.HocLieu;
import com.library.model.LuanVan;
import com.library.model.PhieuMuon;
import com.library.model.Sach;
import com.library.model.TacGia;
import com.library.model.TaiLieuDienTu;

/**
 * OqlQueryDAO — thực thi các truy vấn OQL/JPQL thực sự trên ObjectDB.
 *
 * Bài toán minh hoạ:
 *  1. Kế thừa đối tượng: SELECT s FROM Sach s WHERE s.soTrang > 500
 *  2. Dữ liệu đa trị (SIZE):  SELECT h FROM HocLieu h WHERE SIZE(h.danhSachTacGia) > 1
 *  3. Path Expression:  SELECT p.banDoc.hoTen, p.banSao.hocLieu.tieuDe FROM PhieuMuon p
 *  4. Thống kê theo chủ đề:  SELECT c.tenChuDe, COUNT(h) FROM HocLieu h JOIN h.danhSachChuDe c GROUP BY c.tenChuDe
 */
public class OqlQueryDAO {

    // ===================================================================
    //  SEED DATA — chỉ tạo nếu DB chưa có dữ liệu
    // ===================================================================
    private void seedDataIfEmpty(EntityManager em) {
        Long count = em.createQuery("SELECT COUNT(h) FROM HocLieu h", Long.class).getSingleResult();
        if (count > 0) return; // đã có dữ liệu

        System.out.println("   [ObjectDB] Database rỗng — đang nạp dữ liệu mẫu...");
        em.getTransaction().begin();

        // Tác giả
        TacGia silberschatz = new TacGia("Abraham Silberschatz");
        TacGia elmasri       = new TacGia("Ramez Elmasri");
        TacGia codd          = new TacGia("E. F. Codd");
        TacGia nguyen        = new TacGia("Nguyễn Văn An");
        em.persist(silberschatz); em.persist(elmasri);
        em.persist(codd);         em.persist(nguyen);

        // Chủ đề
        ChuDe csdl   = new ChuDe("Cơ sở dữ liệu");
        ChuDe ai     = new ChuDe("Trí tuệ nhân tạo");
        ChuDe cnpm   = new ChuDe("Công nghệ phần mềm");
        em.persist(csdl); em.persist(ai); em.persist(cnpm);

        // HL01 - Sách nhiều tác giả, nhiều chủ đề
        Sach sach1 = new Sach("HL01", "Database System Concepts",
                "Giáo trình CSDL kinh điển", 2020, "Tiếng Anh", 850000.0,
                date(2023, 1, 15), "img/hl01.jpg",
                "978-0078022159", "McGraw-Hill", 1376, 7);
        sach1.getDanhSachTacGia().add(silberschatz);
        sach1.getDanhSachTacGia().add(elmasri);
        sach1.getDanhSachChuDe().add(csdl);
        em.persist(sach1);

        // HL02 - Sách 1 tác giả, 500 trang (ranh giới)
        Sach sach2 = new Sach("HL02", "Fundamentals of Database Systems",
                "Nền tảng hệ thống CSDL", 2016, "Tiếng Anh", 720000.0,
                date(2023, 3, 10), "img/hl02.jpg",
                "978-0133970777", "Pearson", 1280, 7);
        sach2.getDanhSachTacGia().add(elmasri);
        sach2.getDanhSachChuDe().add(csdl);
        em.persist(sach2);

        // HL03 - Sách < 500 trang
        Sach sach3 = new Sach("HL03", "SQL in 10 Minutes",
                "Học SQL nhanh", 2019, "Tiếng Anh", 250000.0,
                date(2023, 5, 1), "img/hl03.jpg",
                "978-0135182796", "Sams", 288, 5);
        sach3.getDanhSachTacGia().add(codd);
        sach3.getDanhSachChuDe().add(csdl);
        em.persist(sach3);

        // HL04 - Tài liệu điện tử
        TaiLieuDienTu tl = new TaiLieuDienTu("HL04", "Machine Learning Yearning",
                "Hướng dẫn xây dựng dự án AI", 2018, "Tiếng Anh", 0.0,
                date(2023, 6, 1), "img/hl04.jpg",
                "PDF", 2.5, "Công khai", "https://deeplearning.ai/mly");
        tl.getDanhSachTacGia().add(nguyen);
        tl.getDanhSachChuDe().add(ai);
        em.persist(tl);

        // HL05 - Luận văn
        LuanVan lv = new LuanVan("HL05", "Nghiên cứu CSDL Hướng Đối Tượng",
                "So sánh mô hình CSDL quan hệ và hướng đối tượng", 2024,
                "Tiếng Việt", 0.0, date(2024, 6, 15), "img/hl05.jpg",
                "PGS. TS. Trần Bình", "Đại học Bách Khoa",
                "Đại học", "Công nghệ thông tin", 9.5);
        lv.getDanhSachTacGia().add(nguyen);
        lv.getDanhSachChuDe().add(csdl); lv.getDanhSachChuDe().add(cnpm);
        em.persist(lv);

        // Bản đọc
        BanDoc bd1 = new BanDoc("BD01", "Lê Thị Hoa", "hoa@email.com",
                "0901234567", "hashed_pw", "Sinh viên", date(2025, 12, 31), false);
        BanDoc bd2 = new BanDoc("BD02", "Phạm Văn Đức", "duc@email.com",
                "0912345678", "hashed_pw", "Giảng viên", date(2026, 12, 31), false);
        em.persist(bd1); em.persist(bd2);

        // Bản sao
        BanSao bs1 = new BanSao("BS01", sach1, "Đang mượn", "Tốt", "A1-01");
        BanSao bs2 = new BanSao("BS02", sach2, "Rảnh",      "Tốt", "A1-02");
        em.persist(bs1); em.persist(bs2);

        // Phiếu mượn
        PhieuMuon pm1 = new PhieuMuon(bd1, bs1, date(2024, 10, 1), "Đang mượn");
        PhieuMuon pm2 = new PhieuMuon(bd2, bs2, date(2024, 9, 15), "Đã trả");
        em.persist(pm1); em.persist(pm2);

        em.getTransaction().commit();
        System.out.println("   [ObjectDB] ✅ Đã nạp " + 5 + " học liệu mẫu thành công!\n");
    }

    private static Date date(int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(year, month - 1, day, 0, 0, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    // ===================================================================
    //  MAIN ENTRY — chạy tất cả bài toán demo
    // ===================================================================
    public void executeOqlQueries() {
        EntityManager em = ObjectDBConfig.createEntityManager();
        try {
            seedDataIfEmpty(em);
            demoBaiToan1_Ke_Thua(em);
            demoBaiToan2_DaTri_Size(em);
            demoBaiToan3_PathExpression(em);
            demoBaiToan4_ThongKe_ChuDe(em);
            demoBaiToan5_LuanVan_DiemCao(em);
            inKetLuan();
        } catch (Exception e) {
            System.err.println("   [LỖI ObjectDB] " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    // ===================================================================
    //  BÀI TOÁN 1: KẾ THỪA ĐỐI TƯỢNG
    // ===================================================================
    private void demoBaiToan1_Ke_Thua(EntityManager em) {
        System.out.println("==========================================================================================");
        System.out.println("   📊 BẢNG SO SÁNH: CSDL QUAN HỆ (SQL) VS CSDL HƯỚNG ĐỐI TƯỢNG (OQL/OBJECTDB)");
        System.out.println("==========================================================================================");

        System.out.println("\n🔥 BÀI TOÁN 1: TRUY VẤN KẾ THỪA — Tìm Sách có số trang > 500");
        System.out.println("   --------------------------------------------------------------------------------------");
        System.out.println("   🔴 SQL (PostgreSQL) — BẮT BUỘC JOIN 2 bảng:");
        System.out.println("      SELECT h.MaHocLieu, h.TieuDe, s.SoTrang");
        System.out.println("      FROM Sach s INNER JOIN HocLieu h ON s.MaHocLieu = h.MaHocLieu");
        System.out.println("      WHERE s.SoTrang > 500;");
        System.out.println("      → Nhược điểm: JOIN đắt, dễ sai cú pháp khi kế thừa nhiều cấp.");
        System.out.println();
        System.out.println("   🟢 OQL (ObjectDB) — KHÔNG cần JOIN (kế thừa tự nhiên):");
        System.out.println("      SELECT s FROM Sach s WHERE s.soTrang > 500;");

        // === THỰC THI THỰC SỰ ===
        TypedQuery<Sach> query = em.createQuery(
                "SELECT s FROM com.library.model.Sach s WHERE s.soTrang > 500", Sach.class);
        List<Sach> result = query.getResultList();

        System.out.println("      → Kết quả thực tế (" + result.size() + " cuốn tìm thấy):");
        for (Sach s : result) {
            System.out.printf("        ✔ [%s] \"%s\" — %d trang, NXB: %s%n",
                    s.getMaHocLieu(), s.getTieuDe(), s.getSoTrang(), s.getNhaXuatBan());
        }
    }

    // ===================================================================
    //  BÀI TOÁN 2: DỮ LIỆU ĐA TRỊ — SIZE()
    // ===================================================================
    private void demoBaiToan2_DaTri_Size(EntityManager em) {
        System.out.println("\n🔥 BÀI TOÁN 2: DỮ LIỆU ĐA TRỊ (4NF) — Học liệu có > 1 Tác giả");
        System.out.println("   --------------------------------------------------------------------------------------");
        System.out.println("   🔴 SQL — BẮT BUỘC JOIN 3 bảng + GROUP BY + HAVING:");
        System.out.println("      SELECT h.MaHocLieu, h.TieuDe, COUNT(ht.MaTacGia) AS SoTacGia");
        System.out.println("      FROM HocLieu h");
        System.out.println("      JOIN HocLieu_TacGia ht ON h.MaHocLieu = ht.MaHocLieu");
        System.out.println("      GROUP BY h.MaHocLieu, h.TieuDe HAVING COUNT(ht.MaTacGia) > 1;");
        System.out.println("      → Nhược điểm: Phức tạp, cần bảng liên kết trung gian.");
        System.out.println();
        System.out.println("   🟢 OQL — Dùng hàm SIZE() trên Collection, không cần JOIN:");
        System.out.println("      SELECT h FROM HocLieu h WHERE SIZE(h.danhSachTacGia) > 1;");

        // === THỰC THI THỰC SỰ ===
        TypedQuery<HocLieu> query = em.createQuery(
                "SELECT h FROM com.library.model.HocLieu h WHERE SIZE(h.danhSachTacGia) > 1", HocLieu.class);
        List<HocLieu> result = query.getResultList();

        System.out.println("      → Kết quả thực tế (" + result.size() + " học liệu):");
        for (HocLieu h : result) {
            StringBuilder authors = new StringBuilder();
            h.getDanhSachTacGia().forEach(t -> {
                if (authors.length() > 0) authors.append(", ");
                authors.append(t.getTen());
            });
            System.out.printf("        ✔ [%s] \"%s\" — Tác giả: [%s]%n",
                    h.getMaHocLieu(), h.getTieuDe(), authors);
        }
    }

    // ===================================================================
    //  BÀI TOÁN 3: PATH EXPRESSION — Điều hướng qua quan hệ
    // ===================================================================
    private void demoBaiToan3_PathExpression(EntityManager em) {
        System.out.println("\n🔥 BÀI TOÁN 3: PATH EXPRESSION — Duyệt quan hệ mà KHÔNG dùng JOIN");
        System.out.println("   --------------------------------------------------------------------------------------");
        System.out.println("   🔴 SQL — JOIN 3 bảng chỉ để lấy tên bạn đọc + tên tài liệu:");
        System.out.println("      SELECT bd.HoTen, h.TieuDe FROM PhieuMuon pm");
        System.out.println("      JOIN BanDoc bd ON pm.MaBanDoc = bd.MaBanDoc");
        System.out.println("      JOIN BanSao bs ON pm.MaBanSao = bs.MaBanSao");
        System.out.println("      JOIN HocLieu h ON bs.MaHocLieu = h.MaHocLieu;");
        System.out.println();
        System.out.println("   🟢 OQL — Dùng toán tử dấu chấm (.) xuyên 3 thực thể, 0 từ JOIN:");
        System.out.println("      SELECT p FROM PhieuMuon p  [→ p.banDoc.hoTen / p.banSao.hocLieu.tieuDe]");

        // === THỰC THI THỰC SỰ ===
        TypedQuery<PhieuMuon> query = em.createQuery(
                "SELECT p FROM com.library.model.PhieuMuon p", PhieuMuon.class);
        List<PhieuMuon> result = query.getResultList();

        System.out.println("      → Kết quả thực tế (" + result.size() + " phiếu mượn):");
        for (PhieuMuon p : result) {
            String tenBanDoc  = p.getBanDoc() != null ? p.getBanDoc().getHoTen() : "N/A";
            String tenHocLieu = (p.getBanSao() != null && p.getBanSao().getHocLieu() != null)
                    ? p.getBanSao().getHocLieu().getTieuDe() : "N/A";
            System.out.printf("        ✔ Bạn đọc: %-20s | Tài liệu: \"%s\" | Trạng thái: %s%n",
                    tenBanDoc, tenHocLieu, p.getTrangThai());
        }
    }

    // ===================================================================
    //  BÀI TOÁN 4: THỐNG KÊ GROUP BY chủ đề
    // ===================================================================
    private void demoBaiToan4_ThongKe_ChuDe(EntityManager em) {
        System.out.println("\n🔥 BÀI TOÁN 4: THỐNG KÊ — Số học liệu theo từng Chủ đề");
        System.out.println("   --------------------------------------------------------------------------------------");
        System.out.println("   🟢 OQL — JOIN + GROUP BY trên collection nhúng trong object:");
        System.out.println("      SELECT c.tenChuDe, COUNT(h)");
        System.out.println("      FROM HocLieu h JOIN h.danhSachChuDe c");
        System.out.println("      GROUP BY c.tenChuDe ORDER BY COUNT(h) DESC;");

        // === THỰC THI THỰC SỰ ===
        List<Object[]> result = em.createQuery(
                "SELECT c.tenChuDe, COUNT(h) FROM com.library.model.HocLieu h JOIN h.danhSachChuDe c " +
                "GROUP BY c.tenChuDe ORDER BY COUNT(h) DESC", Object[].class)
                .getResultList();

        System.out.println("      → Kết quả thực tế:");
        for (Object[] row : result) {
            System.out.printf("        ✔ Chủ đề: %-35s | Số học liệu: %d%n", row[0], row[1]);
        }
    }

    // ===================================================================
    //  BÀI TOÁN 5: TRUY VẤN LỚP CON & PHƯƠNG THỨC — Luận văn xuất sắc (Điểm >= 9.0)
    // ===================================================================
    private void demoBaiToan5_LuanVan_DiemCao(EntityManager em) {
        System.out.println("\n🔥 BÀI TOÁN 5: TRUY VẤN LỚP CON & PHƯƠNG THỨC — Luận văn có Điểm bảo vệ >= 9.0");
        System.out.println("   --------------------------------------------------------------------------------------");
        System.out.println("   🔴 SQL (PostgreSQL) — Phải JOIN bảng HocLieu và LuanVan:");
        System.out.println("      SELECT h.MaHocLieu, h.TieuDe, l.DiemBaoVe, l.NguoiHuongDan");
        System.out.println("      FROM LuanVan l JOIN HocLieu h ON l.MaHocLieu = h.MaHocLieu");
        System.out.println("      WHERE l.DiemBaoVe >= 9.0;");
        System.out.println();
        System.out.println("   🟢 OQL / JPQL (ObjectDB) — Lọc trực tiếp trên Lớp con LuanVan (Không cần JOIN):");
        System.out.println("      SELECT l FROM LuanVan l WHERE l.diemBaoVe >= 9.0;");

        // === THỰC THI THỰC SỰ ===
        TypedQuery<LuanVan> query = em.createQuery(
                "SELECT l FROM com.library.model.LuanVan l WHERE l.diemBaoVe >= 9.0", LuanVan.class);
        List<LuanVan> result = query.getResultList();

        System.out.println("      → Kết quả thực tế (" + result.size() + " luận văn đạt xuất sắc):");
        for (LuanVan lv : result) {
            System.out.printf("        ✔ [%s] \"%s\" — Điểm: %.1f | GVHD: %s | Đạt loại xuất sắc: %s%n",
                    lv.getMaHocLieu(), lv.getTieuDe(), lv.getDiemBaoVe(), lv.getNguoiHuongDan(),
                    lv.isXuatSac() ? "CÓ (lv.isXuatSac() = true)" : "KHÔNG");
        }
    }

    // ===================================================================
    //  KẾT LUẬN
    // ===================================================================
    private void inKetLuan() {
        System.out.println("\n------------------------------------------------------------------------------------------");
        System.out.println("🏆 KẾT LUẬN NGHIÊN CỨU (thực thi trực tiếp trên ObjectDB v2.x):");
        System.out.println("   1. ObjectDB (OQL/JPQL) loại bỏ hoàn toàn 'Lệch pha trở kháng' (Impedance Mismatch).");
        System.out.println("      → Truy vấn kế thừa, đa trị, điều hướng: gọn hơn SQL tới 3–5 lần.");
        System.out.println("   2. PostgreSQL (SQL) vượt trội về Transaction + Locking chống xung đột mượn trùng.");
        System.out.println("   3. Mô hình Hybrid (PostgreSQL + ObjectDB) tận dụng ưu điểm của cả hai.");
        System.out.println("==========================================================================================\n");
    }
}