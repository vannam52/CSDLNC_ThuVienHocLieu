import psycopg2
from faker import Faker
import random
import datetime

fake = Faker('vi_VN')

DB_CONFIG = {
    'dbname': 'library_management',
    'user': 'postgres',
    'password': '12345',
    'host': 'localhost',
    'port': '5432'
}

def generate_data(num_books=100, num_readers=50):
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        cur = conn.cursor()
        print(f"✅ Đang kết nối tới Database 'library_management'...")
        print(f"🚀 Bắt đầu sinh dữ liệu mẫu: {num_readers} Bạn Đọc và {num_books} Học Liệu...")

        # 1. Sinh Bạn Đọc (50 bạn đọc, mã từ BD_M_001 đến BD_M_050)
        inserted_readers = 0
        for i in range(1, num_readers + 1):
            ma_bd = f"BD_M_{i:03d}"
            hoten = fake.name()
            email = f"user_{i}_{random.randint(100, 999)}@thuvien.edu.vn"
            sdt = f"09{random.randint(10000000, 99999999)}"
            matkhau = "123456"
            loai_tk = random.choice(["Sinh vien", "Giang vien", "Khach"])
            ngay_het_han = datetime.date(2027, 12, 31)

            cur.execute("""
                INSERT INTO BanDoc (MaBanDoc, HoTen, Email, SoDienThoai, MatKhau, LoaiTaiKhoan, NgayHetHan, TaiKhoanBiKhoa)
                VALUES (%s, %s, %s, %s, %s, %s, %s, FALSE)
                ON CONFLICT (MaBanDoc) DO NOTHING;
            """, (ma_bd, hoten, email, sdt, matkhau, loai_tk, ngay_het_han))
            inserted_readers += 1

        print(f"✅ Đã nạp thành công {inserted_readers} Bạn Đọc mới!")

        # 2. Sinh Học Liệu & Sách (100 cuốn sách, mã từ HL_M_001 đến HL_M_100)
        topics = [
            "Lập trình Java nâng cao", "Hệ quản trị CSDL PostgreSQL", "Thiết kế CSDL quan hệ",
            "Database Systems and Architecture", "Distributed Database Management",
            "Cấu trúc dữ liệu và giải thuật", "Trí tuệ nhân tạo căn bản", "Machine Learning cơ bản",
            "Advanced Database Optimization and Indexing", "Phát triển ứng dụng Web an toàn"
        ]

        inserted_books = 0
        for i in range(1, num_books + 1):
            ma_hl = f"HL_M_{i:03d}"
            
            # 25% sách có chứa từ khóa 'Database' để phục vụ test Menu 7
            if random.random() < 0.25:
                tieude = f"Giao trinh Database - Tap {random.randint(1, 5)} - {fake.word().capitalize()}"
            else:
                tieude = f"{random.choice(topics)} - Tap {random.randint(1, 5)}"

            mota = "Tai lieu hoc tap nghien cuu chuyen sau cho sinh vien"
            namxb = random.randint(2018, 2025)
            ngonngu = random.choice(["Tieng Viet", "Tieng Anh"])
            giatien = random.randint(80, 500) * 1000.0

            cur.execute("""
                INSERT INTO HocLieu (MaHocLieu, TieuDe, MoTa, NamXuatBan, NgonNgu, GiaTien, NgayNhapKho)
                VALUES (%s, %s, %s, %s, %s, %s, CURRENT_DATE)
                ON CONFLICT (MaHocLieu) DO NOTHING;
            """, (ma_hl, tieude, mota, namxb, ngonngu, giatien))

            isbn = f"978-604-{random.randint(100, 999)}-{random.randint(1000, 9999)}-{i % 9}"
            nhaxb = random.choice(["NXB Giao Duc", "NXB Thong Tin & Truyen Thong", "NXB Dai Hoc Quoc Gia", "Springer", "O'Reilly"])
            sotrang = random.randint(150, 800)
            lantaiban = random.randint(1, 4)

            cur.execute("""
                INSERT INTO Sach (MaHocLieu, ISBN, NhaXuatBan, SoTrang, LanTaiBan)
                VALUES (%s, %s, %s, %s, %s)
                ON CONFLICT (MaHocLieu) DO NOTHING;
            """, (ma_hl, isbn, nhaxb, sotrang, lantaiban))

            # Sinh Bản sao cho sách
            ma_bansao = f"BS_M_{i:03d}"
            cur.execute("""
                INSERT INTO BanSao (MaBanSao, MaHocLieu, TrangThai, TinhTrangVatLy, ViTriKe)
                VALUES (%s, %s, 'San sang', 'Moi tinh', 'Ke Sach')
                ON CONFLICT (MaBanSao) DO NOTHING;
            """, (ma_bansao, ma_hl))

            inserted_books += 1

        print(f"✅ Đã nạp thành công {inserted_books} Học Liệu, Sách và Bản Sao!")

        conn.commit()
        cur.close()
        conn.close()
        print("\n🎉 HOÀN TẤT 100%! CSDL 'library_management' đã có đủ dữ liệu sẵn sàng để test!")

    except Exception as e:
        print(f"❌ Lỗi: {e}")

if __name__ == '__main__':
    generate_data(num_books=100, num_readers=50)

