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
*   **Kết quả mong đợi:** Menu Console xuất hiện với 7 tính năng (Từ 1 đến 7) và tuỳ chọn 0 để thoát.

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
    *Kết quả phải là 'Dang cho muon' (chứng tỏ có người đã mượn được) và trong bảng `PhieuMuon` chỉ phát sinh đúng 1 phiếu mới cho sách này, không bị trùng (Lost Update).*

### 4. Menu 5 - Transaction: Mô phỏng lỗi & Rollback
*   **Thao tác Java:** Gõ `5`. Ứng dụng thử đổi trạng thái của sách `BS02` thành Đang mượn, nhưng sau đó cố tình tạo một mã độc giả sai (`BD_ERR`).
*   **Kết quả mong đợi trên Java:** Terminal báo lỗi thao tác 2 và in ra dòng chữ **"Tự động ROLLBACK!"**.
*   **Backtest trên pgAdmin4:**
    ```sql
    SELECT TrangThai FROM BanSao WHERE MaBanSao = 'BS02';
    ```
    *Mặc dù ở thao tác 1 Java đã cập nhật thành 'Dang muon', nhưng nhờ Rollback, khi bạn kiểm tra lại trên pgAdmin4, trạng thái vẫn được bảo toàn nguyên vẹn là 'San sang'.*

### 5. Menu 6 - CSDL Hướng đối tượng (OQL/ObjectDB)
*   **Thao tác Java:** Gõ `6`. Hệ thống chuyển sang thao tác với file `thuvien.odb` bằng ngôn ngữ hướng đối tượng. Không dùng lệnh `JOIN` nào của SQL.
*   **Giải thích:** Đối chiếu mã nguồn file `OqlQueryDAO.java` với `HocLieuDAO.java` để thấy sự gọn gàng và tránh được vấn đề "Lệch pha trở kháng" (Impedance Mismatch).

### 6. Menu 7 - Thực nghiệm Hiệu năng Index (GIN Trigram)
*   **Thao tác Java:** Gõ `7`. Hệ thống sẽ chạy so sánh song song 2 trường hợp:
    1. **Trường hợp 1 (Sequential Scan):** Quét tuần tự toàn bộ bảng (chi phí `cost` và thời gian cao).
    2. **Trường hợp 2 (Bitmap Index Scan):** Tận dụng chỉ mục GIN Trigram (`idx_hoclieu_tieude_trgm`) để tra cứu cây n-gram chuỗi từ khóa `%Database%`.
*   **Giải thích:** Kết quả in ra chứng minh chi phí truy vấn (`cost`) và cơ chế quét thông minh khi có Index (Chụp ảnh 2 bảng kế hoạch này đưa vào Báo cáo Word để lấy trọn điểm thực nghiệm).

