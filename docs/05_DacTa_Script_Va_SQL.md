# Đặc tả Chi tiết: Script Python & SQL
Tài liệu này giải thích chi tiết vai trò, chức năng và ý đồ thiết kế của từng file kịch bản (Python) và file truy vấn (SQL) trong hệ thống, nhằm chứng minh hệ thống được thiết kế hoàn toàn bám sát yêu cầu Đồ án.

---

## I. Đặc tả Hệ thống Script Python (`/scripts`)

Hệ thống kịch bản Python được dùng để **Tự động hóa (Automation)** quy trình thiết lập và đo lường, thay thế cho các thao tác thủ công trên giao diện.

### 1. File `setup_library_management_db.py`
- **Vai trò:** Đóng vai trò là "Kiến trúc sư CSDL". Hỗ trợ nhóm và giảng viên chỉ cần 1 thao tác click là tạo ra toàn bộ hệ thống.
- **Tính năng cốt lõi:**
  1. Kết nối PostgreSQL và tạo mới Database `library_management`.
  2. **Clean & Reset:** Tự động gọi lệnh `DROP SCHEMA public CASCADE` để dọn sạch rác nếu chạy lại nhiều lần, đảm bảo tính ổn định của kịch bản test.
  3. Đọc và thực thi tuần tự các file SQL `01_schema`, `02_data_sample`, và `04_transaction_procedure`.

### 2. File `generate_mock_data.py`
- **Vai trò:** Hỗ trợ thực nghiệm "Đo lường hiệu năng".
- **Tính năng cốt lõi:** 
  - Sử dụng thư viện `Faker` của Python để tự động sinh ra dữ liệu mẫu (100 Học liệu/Sách và 50 Bạn đọc) một cách nhanh chóng và gọn nhẹ.
  - Phục vụ trực tiếp cho **Menu số 7** của Java: Cung cấp đủ dữ liệu để chứng minh trình tối ưu truy vấn (Query Optimizer) của PostgreSQL và so sánh sự khác biệt khi sử dụng **Chỉ mục (Index)** so với quét tuần tự (Seq Scan).

### 3. File `test_transaction.py`
- **Vai trò:** Bản nguyên mẫu (Prototype) cho kịch bản Tranh chấp.
- **Tính năng cốt lõi:** Sử dụng module `threading` của Python để mô phỏng 2 sinh viên cùng bấm mượn cuốn sách `BS01` ở cùng 1 tích tắc. *(Ghi chú: Logic này sau đó đã được chuyển thể lên bản chính thức tại Menu 4 của Java (`TransactionDAO.java`))*

---

## II. Đặc tả Hệ thống SQL (`/src/main/resources`)

Bộ khung SQL được thiết kế không phải để tạo ra một ứng dụng CRUD (Thêm/Sửa/Xóa) cơ bản, mà được thiết kế chuyên biệt để biểu diễn các kỹ thuật **CSDL Nâng Cao**.

### 1. File `01_schema.sql` (Cấu trúc bảng)
- **Thiết kế Kế thừa (Inheritance):** Sử dụng chiến lược `Class Table Inheritance`. Khai báo bảng cha `HocLieu`, sau đó dùng lệnh `REFERENCES HocLieu(MaHocLieu)` ở các bảng con (`Sach`, `LuanVan`, `VideoBaiGiang`, `TaiLieuDienTu`) để ép tính đa hình.
- **Thiết kế Đa trị 4NF:** Bóc tách thuộc tính đa trị (Một sách có nhiều Tác giả, nhiều Chủ đề) ra thành các bảng độc lập `TacGia`, `ChuDe`, nối lại bằng các bảng liên kết N-N.
- **Thiết kế Đệ quy:** Bảng `ChuDe` có cột `ChuDeCha` tự tham chiếu khóa ngoại đến chính nó, cho phép cây danh mục phát triển vô hạn cấp.

### 2. File `02_data_sample.sql` (Dữ liệu mồi)
- **Ý đồ:** Chèn các dữ liệu "đã được lên kịch bản" như Sách `HL01`, `HL02` và bản sao `BS01`, `BS02` để khi code Java gọi lệnh kiểm thử, kết quả trả về sẽ Match 100% với kịch bản (không bị trống dữ liệu).

### 3. File `03_advanced_queries.sql` (Phao cứu sinh)
- **Vai trò:** Là một "Cheat Sheet" lưu trữ các câu lệnh SQL siêu khó, dùng để dán trực tiếp vào pgAdmin4 chứng minh với giáo viên.
- **Bao gồm:**
  - Lệnh đệ quy `WITH RECURSIVE`.
  - Lệnh gom nhóm `GROUP BY ... HAVING COUNT > 1` (Minh chứng chuẩn hóa 4NF).
  - Lệnh đánh giá `EXPLAIN ANALYZE` xuất báo cáo chi phí.

### 4. File `04_transaction_procedure.sql` (Giao dịch)
- **Vai trò:** Nơi xử lý tính toàn vẹn dữ liệu ở cấp độ Server.
- **Tính năng cốt lõi:**
  - Định nghĩa Store Procedure `sp_MuonSach` bằng ngôn ngữ `PL/pgSQL`.
  - Áp dụng kỹ thuật **Khóa bi quan (Pessimistic Locking)** với cờ `FOR UPDATE`. Lệnh này khóa chết dòng dữ liệu của cuốn sách ngay khi luồng 1 chạm vào, bắt luồng 2 phải đợi.
  - Sử dụng khối `EXCEPTION` để chủ động bẫy lỗi và Rollback nếu phát hiện cuốn sách đã bị người khác mượn thành công.
