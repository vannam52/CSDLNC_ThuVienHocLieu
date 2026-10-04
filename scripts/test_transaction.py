import psycopg2
import threading
import time

DB_CONFIG = {
    'dbname': 'thuvien_db', 'user': 'postgres',
    'password': '12345', 'host': 'localhost', 'port': '5432'
}

def muon_sach(ten_sinh_vien):
    try:
        # Bắt buộc mỗi luồng phải tạo 1 kết nối độc lập để mô phỏng 2 máy tính khác nhau
        conn = psycopg2.connect(**DB_CONFIG)
        cur = conn.cursor()

        print(f"[{ten_sinh_vien}] Đang kiểm tra sách trên kệ...")
        
        # Lệnh SQL bắt đầu giao dịch và KHÓA (LOCK) dòng dữ liệu bản sao này
        # FOR UPDATE: Khóa dòng này lại, không cho ai đụng vào cho đến khi tôi làm xong
        cur.execute("""
            SELECT TrangThai FROM BanSao 
            WHERE MaBanSao = 'BS_001' FOR UPDATE
        """)
        
        trang_thai = cur.fetchone()[0]
        
        if trang_thai == 'San sang':
            print(f"[{ten_sinh_vien}] Thấy sách còn Sẵn sàng! Tiến hành làm phiếu mượn...")
            # Giả lập thời gian máy chủ xử lý làm phiếu mất 2 giây
            time.sleep(2) 
            
            # Đổi trạng thái sách thành Đang cho mượn
            cur.execute("""
                UPDATE BanSao SET TrangThai = 'Dang cho muon' 
                WHERE MaBanSao = 'BS_001'
            """)
            conn.commit()
            print(f"✅ [{ten_sinh_vien}] MƯỢN THÀNH CÔNG!")
        else:
            print(f"❌ [{ten_sinh_vien}] MƯỢN THẤT BẠI! Sách đã bị ai đó mượn mất rồi.")

    except Exception as e:
        print(f"Lỗi: {e}")
        conn.rollback()
    finally:
        cur.close()
        conn.close()

# Tạo 2 luồng (Giả lập 2 sinh viên bấm nút cùng lúc)
thread1 = threading.Thread(target=muon_sach, args=("Sinh viên A",))
thread2 = threading.Thread(target=muon_sach, args=("Sinh viên B",))

# Cho 2 luồng chạy CÙNG MỘT LÚC
thread1.start()
thread2.start()

thread1.join()
thread2.join()
