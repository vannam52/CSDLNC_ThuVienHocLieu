# Hướng dẫn Kiểm thử và Chạy lại (Backtest) Đồ án
Tài liệu này đóng vai trò là "Nhật ký thực nghiệm và hướng dẫn chạy lại" theo yêu cầu của Đề cương môn học. 

## I. Môi trường Yêu cầu (Prerequisites)
Để chương trình hoạt động đúng, máy tính của người chạy cần có:
1. **PostgreSQL 14+**: Đang chạy dưới dạng background service. Database có password là `12345` (tài khoản `postgres`).
2. **Python 3.10+**: Đã cài đặt thư viện `psycopg2` và `faker` (`pip install psycopg2 faker`).
3. **Java JDK 17+** và **Apache Maven**: Để biên dịch ứng dụng Java.
4. **pgAdmin4 / DBeaver (Tuỳ chọn)**: Khuyên dùng để quan sát và đối chiếu dữ liệu (Backtest).

---

## II. Quy trình Thiết lập CSDL (Khởi tạo lần đầu)
Người dùng không cần thao tác tạo bảng bằng tay. Kịch bản Python sẽ thực hiện tự động toàn bộ.
*   **Mở Terminal** tại thư mục gốc của dự án (`CSDLNC_ThuVienHocLieu`).
*   **Chạy lệnh:**
    ```powershell
    python scripts\setup_library_management_db.py
    ```
*   **Mục đích & Cách kiểm tra (Xem cái gì?):** 
    - Lệnh này sẽ tự động tạo một Database trắng tên là `library_management` trong hệ thống PostgreSQL của máy bạn, sau đó chạy lệnh tạo bảng và bơm dữ liệu.
    - **Cách xem:** Bạn hãy mở phần mềm **pgAdmin4** lên -> Mở rộng nhánh `Servers` -> `PostgreSQL` -> `Databases`. Bạn sẽ thấy xuất hiện database `library_management`. Mở tiếp `Schemas` -> `public` -> `Tables` để thấy các bảng như `Sach`, `TacGia`, `BanSao` đã được tạo sẵn kèm dữ liệu bên trong. Việc này chứng minh kịch bản Python đã hoạt động thành công.

---

## III. Quy trình Khởi chạy Ứng dụng Java
Ứng dụng Java là nơi thực thi các truy vấn và kiểm thử Transaction.
*   **Vẫn tại Terminal cũ**, chạy lệnh biên dịch và khởi chạy:
    ```powershell
    .\mvn.cmd clean compile
    .\run.bat
    ```
*   **Kết quả mong đợi:** Menu Console xuất hiện với 8 tính năng (Từ 1 đến 8) và tuỳ chọn 0 để thoát.

---

## IV. Hướng dẫn Kiểm chứng (Backtest) từng chức năng

Khi thuyết trình/demo, hãy chia đôi màn hình: Một bên là Terminal chạy Java, một bên là giao diện truy vấn của pgAdmin4 kết nối vào DB `library_management`.

### 1. Menu 1 - Tra cứu danh mục chủ đề đệ quy
*   **Thao tác Java:** Gõ `1` trên màn hình Console Terminal đen. Hệ thống in ra cây chủ đề với các nhánh (Công nghệ thông tin -> Lập trình -> Java...).
*   **Backtest trên pgAdmin4 (Chạy ở đâu và Như thế nào?):** 
    1. Mở phần mềm **pgAdmin4**.
    2. Click chuột phải vào database `library_management` -> Chọn **Query Tool...**
    3. Copy toàn bộ đoạn lệnh SQL dưới đây và Paste vào khoảng trắng của Query Tool:
    ```sql
    WITH RECURSIVE CayChuDe AS (
        SELECT MaChuDe, TenChuDe, ChuDeCha, 1 AS CapDo 
        FROM ChuDe WHERE ChuDeCha IS NULL 
        UNION ALL 
        SELECT c.MaChuDe, c.TenChuDe, c.ChuDeCha, ccd.CapDo + 1 
        FROM ChuDe c JOIN CayChuDe ccd ON c.ChuDeCha = ccd.MaChuDe
    ) 
    SELECT * FROM CayChuDe ORDER BY CapDo, TenChuDe;
    ```
    4. Bấm nút **Play (Execute/Refresh)** hình tam giác trên thanh công cụ của pgAdmin4 (hoặc nhấn phím **F5**).
    5. **So sánh:** Nhìn xuống bảng Data Output của pgAdmin4, bạn sẽ thấy kết quả bảng hiển thị y hệt như những gì Java vừa in ra ở Terminal. Điều này chứng minh chức năng Java đã gọi đúng lệnh SQL đệ quy.

### 2. Menu 2 & 3 - Chuẩn hóa 4NF (Đa trị) và Thống kê lượt mượn
*   **Thao tác Java:** Gõ `2` (Tìm học liệu đa chủ đề) hoặc gõ `3` (Thống kê lượt mượn). 
*   **Backtest trên pgAdmin4:**
    *   *Đối chiếu Menu 2 (Học liệu đa chủ đề - Chuẩn 4NF):*
        ```sql
        SELECT h.MaHocLieu, h.TieuDe, COUNT(hc.MaChuDe) AS SoLuongChuDe
        FROM HocLieu h
        JOIN HocLieu_ChuDe hc ON h.MaHocLieu = hc.MaHocLieu
        GROUP BY h.MaHocLieu, h.TieuDe
        HAVING COUNT(hc.MaChuDe) > 1;
        ```
    *   *Đối chiếu Menu 3 (Thống kê lượt mượn theo từng học liệu):*
        ```sql
        SELECT h.MaHocLieu, h.TieuDe, COUNT(p.MaPhieu) AS LuotMuon
        FROM HocLieu h
        JOIN BanSao b ON h.MaHocLieu = b.MaHocLieu
        JOIN PhieuMuon p ON b.MaBanSao = p.MaBanSao
        GROUP BY h.MaHocLieu, h.TieuDe
        ORDER BY LuotMuon DESC;
        ```
*   **Giải thích:** Chứng minh dữ liệu đa trị đã được tách thành bảng trung gian (`HocLieu_ChuDe`, `PhieuMuon`), kết quả Java in ra khớp 100% với pgAdmin4.

### 3. Menu 4 - Transaction: Tranh chấp mượn sách (Pessimistic Locking)
*   **Thao tác Java:** Gõ `4`. Ứng dụng sẽ giả lập 2 luồng (2 sinh viên) bấm mượn cuốn sách `BS01` ở cùng một phần nghìn giây.
*   **Kết quả mong đợi trên Java:** Một luồng báo `✅ THÀNH CÔNG`, luồng còn lại báo `❌ BỊ CHẶN/LỖI` do cơ chế khóa `FOR UPDATE` trong procedure `sp_MuonSach`.
*   **Backtest trên pgAdmin4:**
    ```sql
    SELECT TrangThai FROM BanSao WHERE MaBanSao = 'BS01';
    ```
    *Kết quả phải là 'Dang muon' (chứng tỏ có người đã mượn được) và trong bảng `PhieuMuon` chỉ phát sinh đúng 1 phiếu mới cho sách này, không bị trùng (Lost Update).*

### 4. Menu 5 - Transaction: Mô phỏng lỗi & Rollback
*   **Thao tác Java:** Gõ `5`. Ứng dụng thử đổi trạng thái của sách `BS02` thành Đang mượn, nhưng sau đó cố tình tạo một mã độc giả sai (`BD_ERR`).
*   **Kết quả mong đợi trên Java:** Terminal báo lỗi thao tác 2 và in ra dòng chữ **"Tự động ROLLBACK!"**.
*   **Backtest trên pgAdmin4:**
    ```sql
    SELECT TrangThai FROM BanSao WHERE MaBanSao = 'BS02';
    ```
    *Mặc dù ở thao tác 1 Java đã cập nhật thành 'Dang muon', nhưng nhờ Rollback, khi bạn kiểm tra lại trên pgAdmin4, trạng thái vẫn được bảo toàn nguyên vẹn là 'San sang'.*

### 5. Menu 6 - CSDL Hướng đối tượng (OQL/ObjectDB JPA)
*   **Thao tác Java:** Gõ `6`. Hệ thống tự động nạp 5 thực thể mẫu và thực thi trực tiếp bộ 4 câu truy vấn JPQL/OQL trên file CSDL hướng đối tượng `thuvien.odb` thông qua `EntityManager`.
*   **Giải thích:** Đối chiếu mã nguồn file `OqlQueryDAO.java` với `HocLieuDAO.java` để thấy:
    1. Kế thừa đa hình không cần `JOIN` (`SELECT s FROM Sach s WHERE s.soTrang > 500`).
    2. Kiểm tra thuộc tính đa trị bằng hàm `SIZE(h.danhSachTacGia) > 1`.
    3. Điều hướng liên kết bằng toán tử chấm `p.banDoc.maBanDoc = 'BD01'`.
    4. Gom nhóm thống kê trên tập hợp `JOIN h.danhSachChuDe c GROUP BY c.tenChuDe`.

### 6. Menu 7 - Thực nghiệm Hiệu năng Index (GIN Trigram)
*   **Thao tác Java:** Gõ `7`. Hệ thống sẽ chạy so sánh song song 2 trường hợp:
    1. **Trường hợp 1 (Sequential Scan):** Quét tuần tự toàn bộ bảng (chi phí `cost` và thời gian cao).
    2. **Trường hợp 2 (Bitmap Index Scan):** Tận dụng chỉ mục GIN Trigram (`idx_hoclieu_tieude_trgm`) để tra cứu cây n-gram chuỗi từ khóa `%Database%`.
*   **Giải thích:** Kết quả in ra chứng minh chi phí truy vấn (`cost`) và cơ chế quét thông minh khi có Index (Chụp ảnh 2 bảng kế hoạch này đưa vào Báo cáo Word để lấy trọn điểm thực nghiệm).

### 7. Menu 8 - PL/pgSQL Triggers, Stored Functions & Dynamic SQL
*   **Thao tác Java:** Gõ `8`. Hệ thống chạy kịch bản kiểm thử toàn diện các thủ tục, hàm và trigger.
*   **Backtest trực tiếp trên pgAdmin4:**
    1. **Kiểm tra Function thống kê theo chủ đề (TABLE Function):**
       ```sql
       SELECT * FROM fn_ThongKe_HocLieu_TheoChuDe();
       ```
    2. **Kiểm tra Function kiểm tra điều kiện mượn (Scalar Function):**
       ```sql
       SELECT fn_KiemTra_CoTheMuon('BD01') AS CoTheMuon;
       ```
    3. **Kiểm tra Function tìm kiếm toàn văn GIN Trigram:**
       ```sql
       SELECT * FROM fn_TimKiem_HocLieu('Database');
       ```
    4. **Kiểm tra Function tìm kiếm động Dynamic SQL (`EXECUTE format`):**
       ```sql
       SELECT * FROM fn_TimKiem_HocLieu_Dong(p_TieuDe := 'Database', p_NamTu := 2015, p_SapXepTheo := 'GiaTien');
       ```
    5. **Kiểm tra Trigger 1 & 2 (Mượn sách & Tự động đổi trạng thái bản sao):**
       ```sql
       -- Tạo phiếu mượn qua SQL thông thường:
       INSERT INTO PhieuMuon(MaPhieu, MaBanDoc, MaBanSao) VALUES ('PM_TEST_BK', 'BD01', 'BS02');
       -- Kiểm tra BanSao BS02: Trigger tự động đổi sang 'Dang muon' mà không cần lệnh UPDATE thủ công!
       SELECT MaBanSao, TrangThai FROM BanSao WHERE MaBanSao = 'BS02';
       ```
    6. **Kiểm tra Trigger 1 (Chặn mượn khi bản sao đang bận):**
       ```sql
       -- Cố tình mượn lại BS02 khi đang bận: Hệ thống sẽ bắn Exception ngay lập tức!
       -- INSERT INTO PhieuMuon(MaPhieu, MaBanDoc, MaBanSao) VALUES ('PM_FAIL', 'BD01', 'BS02');
       ```
    7. **Kiểm tra Trigger 3 & Procedure Trả sách (Tự động tính phạt trễ hạn):**
       ```sql
       -- Giả lập trễ hạn 5 ngày:
       UPDATE PhieuMuon SET NgayHanTra = CURRENT_TIMESTAMP - INTERVAL '5 days' WHERE MaPhieu = 'PM_TEST_BK';
       -- Gọi thủ tục trả sách:
       CALL sp_TraSach('PM_TEST_BK', 'Độc giả trả trễ do công tác');
       -- Kiểm tra: TienPhat được tự động tính (25.000 VNĐ) và BanSao tự phục hồi về 'San sang':
       SELECT MaPhieu, TrangThai, NgayTraThucTe, TienPhat, GhiChu FROM PhieuMuon WHERE MaPhieu = 'PM_TEST_BK';
       SELECT MaBanSao, TrangThai FROM BanSao WHERE MaBanSao = 'BS02';
       ```
    8. **Kiểm tra Views báo cáo tổng hợp:**
       ```sql
       SELECT * FROM v_BaoCao_MuonTra;
       SELECT * FROM v_HocLieu_DayDu;
       SELECT * FROM fn_BaoCao_TongQuan_ThuVien();
       ```


