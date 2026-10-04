import psycopg2
from faker import Faker
import random

# Khởi tạo Faker hỗ trợ tên người Việt Nam
fake = Faker('vi_VN')

# Cấu hình kết nối CSDL
DB_CONFIG = {
    'dbname': 'thuvien_db', 
    'user': 'postgres',
    'password': '12345',    
    'host': 'localhost',
    'port': '5432'
}

def generate_data():
    try:
        # Kết nối tới PostgreSQL
        conn = psycopg2.connect(**DB_CONFIG)
        cur = conn.cursor()
        print("✅ Kết nối Database thành công! Đang sinh dữ liệu...")

        # 1. Sinh 50 Bạn Đọc
        for i in range(1, 51):
            ma_bd = f"BD{i:03d}"
            hoten = fake.name()
            email = fake.unique.email()
            sdt = fake.phone_number()
            matkhau = "123456"  # Mật khẩu mặc định
            khoa = random.choice(["CNTT", "Kinh tế", "Điện tử", "Ngôn ngữ Anh"])
            
            cur.execute("""
                INSERT INTO BanDoc (MaBanDoc, HoTen, Email, SoDienThoai, MatKhau, LoaiTaiKhoan, Khoa)
                VALUES (%s, %s, %s, %s, %s, %s, %s)
            """, (ma_bd, hoten, email, sdt, matkhau, 'Sinh viên', khoa))
            
        print("✅ Đã tạo xong 50 Bạn Đọc!")

        # 2. Sinh 100 Cuốn Sách (Bảng cha Học Liệu + Bảng con Sách)
        for i in range(1, 101):
            ma_hl = f"HL{i:03d}"
            tieude = fake.sentence(nb_words=5).replace(".", "")
            namxb = random.randint(2000, 2024)
            giatien = random.randint(50, 500) * 1000
            
            # Thêm vào bảng Học Liệu (Cha)
            cur.execute("""
                INSERT INTO HocLieu (MaHocLieu, TieuDe, NamXuatBan, NgonNgu, GiaTien)
                VALUES (%s, %s, %s, %s, %s)
            """, (ma_hl, tieude, namxb, 'Tiếng Việt', giatien))
            
            # Thêm vào bảng Sách (Con)
            isbn = fake.isbn13()
            nhaxb = fake.company()
            sotrang = random.randint(100, 800)
            
            cur.execute("""
                INSERT INTO Sach (MaHocLieu, ISBN, NhaXuatBan, SoTrang, LanTaiBan)
                VALUES (%s, %s, %s, %s, %s)
            """, (ma_hl, isbn, nhaxb, sotrang, 1))

        print("✅ Đã tạo xong 100 Cuốn Sách!")

        # Lưu thay đổi và đóng kết nối
        conn.commit()
        cur.close()
        conn.close()
        print("🎉 Hoàn tất! Dữ liệu đã được nạp vào Database.")

    except Exception as e:
        print(f"❌ Lỗi: {e}")

if __name__ == '__main__':
    generate_data()
