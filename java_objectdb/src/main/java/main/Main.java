package main;

import entity.HocLieu;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // 1. Kết nối tới CSDL ObjectDB
        // Nó sẽ tự động tạo ra một file tên là 'thuvien.odb' nằm trong thư mục project
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("thuvien.odb");
        EntityManager em = emf.createEntityManager();

        System.out.println("✅ Kết nối CSDL Hướng đối tượng ObjectDB thành công!");

        try {
            // --- BƯỚC 1: THÊM DỮ LIỆU ---
            em.getTransaction().begin(); // Mở khóa Giao dịch
            
            // Tạo ra 2 ĐỐI TƯỢNG (Objects) bằng Java
            HocLieu hl1 = new HocLieu("HL_OOP_01", "Lập trình Java Căn bản", 2026);
            HocLieu hl2 = new HocLieu("HL_OOP_02", "Cơ sở dữ liệu Nâng cao", 2025);
            
            // Lưu thẳng ĐỐI TƯỢNG vào Database (Không cần dùng lệnh INSERT INTO rườm rà)
            // em.persist(hl1); 
            // em.persist(hl2); 
            
            em.getTransaction().commit(); // Chốt lưu
            System.out.println("💾 Đã lưu 2 đối tượng Học Liệu vào CSDL thuvien.odb");


            // --- BƯỚC 2: TRUY VẤN LẤY DỮ LIỆU BẰNG OQL ---
            System.out.println("\n🔍 Đang truy vấn danh sách Học Liệu từ CSDL...");
            
            // Câu lệnh OQL: Rất giống SQL nhưng nó truy vấn Class chứ không phải Table
            TypedQuery<HocLieu> query = em.createQuery("SELECT h FROM HocLieu h", HocLieu.class);
            List<HocLieu> results = query.getResultList();

            // In kết quả ra màn hình
            for (HocLieu h : results) {
                System.out.println(h);
            }

        } finally {
            // Đóng kết nối
            em.close();
            emf.close();
        }
    }
}
