import psycopg2
from faker import Faker
import random
import datetime

fake = Faker('vi_VN')

DB_CONFIG = {
    'dbname': 'library_management',
    'user': 'postgres',
    'password': '12092006',
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

            # Gán ngẫu nhiên Tác giả & Chủ đề (Chuẩn 4NF)
            cur.execute("INSERT INTO HocLieu_TacGia (MaHocLieu, MaTacGia) VALUES (%s, %s) ON CONFLICT DO NOTHING;", 
                        (ma_hl, random.choice(["TG01", "TG02", "TG03"])))
            if i % 3 == 0:
                cur.execute("INSERT INTO HocLieu_TacGia (MaHocLieu, MaTacGia) VALUES (%s, %s) ON CONFLICT DO NOTHING;", 
                            (ma_hl, random.choice(["TG01", "TG02"])))

            cur.execute("INSERT INTO HocLieu_ChuDe (MaHocLieu, MaChuDe) VALUES (%s, %s) ON CONFLICT DO NOTHING;", 
                        (ma_hl, random.choice(["CD01", "CD02", "CD03", "CD04"])))
            if i % 2 == 0:
                cur.execute("INSERT INTO HocLieu_ChuDe (MaHocLieu, MaChuDe) VALUES (%s, %s) ON CONFLICT DO NOTHING;", 
                            (ma_hl, random.choice(["CD02", "CD03"])))

            inserted_books += 1

        print(f"✅ Đã nạp thành công {inserted_books} Sách, Bản Sao, Tác Giả & Chủ Đề!")

        # 3. Sinh thêm Luận văn, Video bài giảng & Tài liệu điện tử (Kế thừa đa hình)
        print("🚀 Đang nạp thêm Luận văn, Video bài giảng & Tài liệu điện tử...")
        for j in range(1, 11):
            # Luận văn
            ma_lv = f"LV_M_{j:02d}"
            cur.execute("""
                INSERT INTO HocLieu (MaHocLieu, TieuDe, MoTa, NamXuatBan, NgonNgu, GiaTien, NgayNhapKho)
                VALUES (%s, %s, 'Luan van nghien cuu tot nghiep', %s, 'Tieng Viet', 0, CURRENT_DATE)
                ON CONFLICT DO NOTHING;
            """, (ma_lv, f"Nghien cuu va Ung dung CSDL Nang cao so {j}", random.randint(2021, 2024)))
            cur.execute("""
                INSERT INTO LuanVan (MaHocLieu, NguoiHuongDan, Truong, CapBaoVe, ChuyenNganh, DiemBaoVe)
                VALUES (%s, 'GS.TS Tran Van B', 'DH Sai Gon', 'Thac si', 'Khoa hoc may tinh', %s)
                ON CONFLICT DO NOTHING;
            """, (ma_lv, round(random.uniform(8.0, 9.8), 2)))

            # Video bài giảng
            ma_vd = f"VD_M_{j:02d}"
            cur.execute("""
                INSERT INTO HocLieu (MaHocLieu, TieuDe, MoTa, NamXuatBan, NgonNgu, GiaTien, NgayNhapKho)
                VALUES (%s, %s, 'Video khoa hoc Database truc tuyen', 2024, 'Tieng Viet', 0, CURRENT_DATE)
                ON CONFLICT DO NOTHING;
            """, (ma_vd, f"Khoa hoc Video SQL & PostgreSQL Nâng cao Bai {j}"))
            cur.execute("""
                INSERT INTO VideoBaiGiang (MaHocLieu, ThoiLuong, DoPhanGiai, NgonNguPhuDe, DungLuong)
                VALUES (%s, %s, '1080p', 'Tieng Viet', %s)
                ON CONFLICT DO NOTHING;
            """, (ma_vd, random.randint(45, 120), round(random.uniform(500, 1500), 2)))

            # Gán chủ đề cho Luận văn & Video
            cur.execute("INSERT INTO HocLieu_ChuDe (MaHocLieu, MaChuDe) VALUES (%s, 'CD03') ON CONFLICT DO NOTHING;", (ma_lv,))
            cur.execute("INSERT INTO HocLieu_ChuDe (MaHocLieu, MaChuDe) VALUES (%s, 'CD02') ON CONFLICT DO NOTHING;", (ma_vd,))

        # 4. Sinh 25 Phiếu Mượn mẫu phục vụ Menu 3 (chọn các bản sao khác nhau)
        sampled_copies = random.sample(range(1, num_books + 1), min(25, num_books))
        for k, copy_idx in enumerate(sampled_copies, 1):
            ma_pm = f"PM_M_{k:03d}"
            ma_bd = f"BD_M_{random.randint(1, num_readers):03d}"
            ma_bs = f"BS_M_{copy_idx:03d}"
            cur.execute("""
                INSERT INTO PhieuMuon (MaPhieu, MaBanDoc, MaBanSao, NgayMuon, NgayHanTra, TrangThai)
                VALUES (%s, %s, %s, CURRENT_TIMESTAMP - INTERVAL '10 days', CURRENT_TIMESTAMP + INTERVAL '4 days', 'Dang muon')
                ON CONFLICT DO NOTHING;
            """, (ma_pm, ma_bd, ma_bs))

        print("✅ Đã nạp thành công Luận văn, Video bài giảng và 25 Phiếu mượn!")

        conn.commit()
        cur.close()
        conn.close()
        print("\n🎉 HOÀN TẤT 100%! CSDL 'library_management' đã có đủ dữ liệu sẵn sàng để test!")

    except Exception as e:
        print(f"❌ Lỗi: {e}")

if __name__ == '__main__':
    generate_data(num_books=100, num_readers=50)

