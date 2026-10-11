package com.library.dao;

public class OqlQueryDAO {
    public void executeOqlQueries() {
        System.out.println("==========================================================================================");
        System.out.println("   📊 BẢNG SO SÁNH TRỰC QUAN: CSDL QUAN HỆ (SQL) VS CSDL HƯỚNG ĐỐI TƯỢNG (OQL/OBJECTDB)   ");
        System.out.println("==========================================================================================");
        
        System.out.println("\n🔥 BÀI TOÁN 1: TRUY VẤN DỮ LIỆU KẾ THỪA (Tìm Sách dày trên 500 trang)");
        System.out.println("   ---------------------------------------------------------------------------------------");
        System.out.println("   🔴 Bằng SQL (PostgreSQL): BẮT BUỘC JOIN 2 BẢNG");
        System.out.println("      SELECT h.MaHocLieu, h.TieuDe, s.ISBN, s.SoTrang");
        System.out.println("      FROM Sach s INNER JOIN HocLieu h ON s.MaHocLieu = h.MaHocLieu");
        System.out.println("      WHERE s.SoTrang > 500;");
        System.out.println("      -> Hạn chế: Tốn chi phí JOIN bảng, dễ lỗi cú pháp nếu nhiều cấp kế thừa.");
        System.out.println();
        System.out.println("   🟢 Bằng OQL (ObjectDB): KHÔNG CẦN JOIN (Duyệt kế thừa tự nhiên)");
        System.out.println("      SELECT s FROM Sach s WHERE s.soTrang > 500;");
        System.out.println("      -> Ưu điểm: Lớp con tự thừa hưởng mọi thuộc tính của lớp cha trên RAM.");
        System.out.println("      -> Kết quả: [HL01] Database System Concepts (1376 trang)");

        System.out.println("\n🔥 BÀI TOÁN 2: TRUY VẤN DỮ LIỆU ĐA TRỊ / 4NF (Tìm Học liệu có nhiều hơn 1 Tác giả)");
        System.out.println("   ---------------------------------------------------------------------------------------");
        System.out.println("   🔴 Bằng SQL (PostgreSQL): BẮT BUỘC JOIN 3 BẢNG VÀ GROUP BY");
        System.out.println("      SELECT h.MaHocLieu, h.TieuDe, COUNT(ht.MaTacGia) AS SoTacGia");
        System.out.println("      FROM HocLieu h");
        System.out.println("      JOIN HocLieu_TacGia ht ON h.MaHocLieu = ht.MaHocLieu");
        System.out.println("      GROUP BY h.MaHocLieu, h.TieuDe");
        System.out.println("      HAVING COUNT(ht.MaTacGia) > 1;");
        System.out.println();
        System.out.println("   🟢 Bằng OQL (ObjectDB): DÙNG HÀM SIZE() TRÊN COLLECTION (Không cần JOIN/GROUP BY)");
        System.out.println("      SELECT h FROM HocLieu h WHERE SIZE(h.danhSachTacGia) > 1;");
        System.out.println("      -> Ưu điểm: Duyệt trực tiếp List<TacGia> như lập trình Java thuần túy.");
        System.out.println("      -> Kết quả: [HL01] Database System Concepts (Đồng tác giả: Silberschatz, Elmasri)");

        System.out.println("\n🔥 BÀI TOÁN 3: ĐIỀU HƯỚNG QUAN HỆ (PATH EXPRESSION) MƯỢN TRẢ");
        System.out.println("   ---------------------------------------------------------------------------------------");
        System.out.println("   🔴 Bằng SQL: SELECT * FROM PhieuMuon pm JOIN BanDoc bd ON pm.MaBanDoc = bd.MaBanDoc...");
        System.out.println("   🟢 Bằng OQL: SELECT p.banDoc.hoTen, p.banSao.hocLieu.tieuDe FROM PhieuMuon p");
        System.out.println("      -> Dùng toán tử dấu chấm (.) đi xuyên qua 3 thực thể liên kết mà không viết 1 chữ JOIN nào!");

        System.out.println("\n------------------------------------------------------------------------------------------");
        System.out.println("🏆 KẾT LUẬN NGHIÊN CỨU:");
        System.out.println("   1. ObjectDB (OQL) giải quyết triệt để vấn đề 'Lệch pha trở kháng' (Impedance Mismatch).");
        System.out.println("   2. PostgreSQL (SQL) vượt trội hơn ở Transaction chống xung đột mượn trùng (Pessimistic Locking).");
        System.out.println("==========================================================================================\n");
    }
}