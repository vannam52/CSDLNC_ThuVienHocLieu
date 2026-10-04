package entity;

import javax.persistence.Entity;
import javax.persistence.Id;

// Đánh dấu đây là một Thực thể (Entity) sẽ được lưu vào ObjectDB
@Entity
public class HocLieu {
    
    @Id // Đánh dấu đây là Khóa chính (Primary Key)
    private String maHocLieu;
    private String tieuDe;
    private int namXuatBan;

    // Bắt buộc phải có Constructor rỗng cho JPA
    public HocLieu() {}

    public HocLieu(String maHocLieu, String tieuDe, int namXuatBan) {
        this.maHocLieu = maHocLieu;
        this.tieuDe = tieuDe;
        this.namXuatBan = namXuatBan;
    }

    // Hàm in thông tin đối tượng ra màn hình
    @Override
    public String toString() {
        return "HocLieu {" +
                "Mã='" + maHocLieu + '\'' +
                ", Tiêu đề='" + tieuDe + '\'' +
                ", Năm XB=" + namXuatBan +
                '}';
    }
}
