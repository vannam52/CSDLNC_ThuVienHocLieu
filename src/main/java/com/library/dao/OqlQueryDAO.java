package com.library.dao;

public class OqlQueryDAO {
    public void executeOqlQueries() {
        System.out.println("------------------------------------------------------------------");
        System.out.println("📌 TRUY VẤN CSDL HƯỚNG ĐỐI TƯỢNG (MÔ PHỎNG JPQL / OQL TRÊN OBJECTDB)");
        System.out.println("------------------------------------------------------------------");
        System.out.println("1. OQL: SELECT s FROM Sach s WHERE s.soTrang > 500");
        System.out.println("   -> Kết quả: [HL01] Database System Concepts (1376 trang)");
        System.out.println();
        System.out.println("2. OQL: SELECT v FROM VideoBaiGiang v WHERE v.thoiLuong >= 60");
        System.out.println("   -> Kết quả: [HL03] Bài giảng Video SQL Nâng cao (120 phút)");
        System.out.println();
        System.out.println("3. OQL: SELECT l FROM LuanVan l WHERE l.diemBaoVe >= 9.0");
        System.out.println("   -> Kết quả: [HL02] Nghiên cứu CSDL Hướng đối tượng (9.5 điểm)");
        System.out.println();
        System.out.println("4. OQL: SELECT h FROM HocLieu h WHERE SIZE(h.danhSachTacGia) > 1");
        System.out.println("   -> Kết quả: [HL01] Database System Concepts (Tác giả: Silberschatz, Elmasri)");
        System.out.println();
        System.out.println("5. OQL: SELECT p FROM PhieuMuon p WHERE p.trangThai = 'Dang muon'");
        System.out.println("   -> Kết quả: [PM001] Bạn đọc Thanh Nhàn đang mượn BS01");
        System.out.println("------------------------------------------------------------------");
    }
}