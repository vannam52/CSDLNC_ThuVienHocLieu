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
  3. Đọc và thực thi tuần tự 4 file SQL `01_schema`, `02_data_sample`, `04_transaction_procedure`, và `05_triggers_and_functions`.

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

### 5. File `05_triggers_and_functions.sql` (Triggers, Functions, Dynamic SQL & Views)
- **Vai trò:** Tầng tự động hóa toàn vẹn dữ liệu và mở rộng phân tích nghiệp vụ sâu trên PostgreSQL (PL/pgSQL).
- **Các thành phần cốt lõi:**
  1. **Hệ thống Trigger tự động hóa:**
     - `trg_KiemTraDieuKienMuon` (`BEFORE INSERT ON PhieuMuon`): Chặn việc mượn sách nếu bạn đọc bị khóa, thẻ hết hạn, đã mượn tối đa 5 cuốn hoặc bản sao đang ở trạng thái khác `'San sang'`.
     - `trg_CapNhatTrangThaiBanSao` (`AFTER INSERT OR UPDATE ON PhieuMuon`): Tự động đồng bộ trạng thái bản sao thành `'Dang muon'` khi tạo phiếu và hoàn trả về `'San sang'` khi đổi trạng thái phiếu sang `'Da tra'`.
     - `trg_TinhTienPhat` (`BEFORE UPDATE ON PhieuMuon`): Tự động tính tiền phạt trễ hạn (5.000 VNĐ / ngày trễ), ghi nhận `NgayTraThucTe` và tự động cập nhật nhật ký ghi chú vào `GhiChu`.
  2. **Hệ thống Stored Functions & Dynamic SQL:**
     - `fn_ThongKe_HocLieu_TheoChuDe()`: Trả về `TABLE` thống kê số học liệu, tỷ lệ % và tổng giá trị từng chủ đề.
     - `fn_KiemTra_CoTheMuon(p_MaBanDoc)`: Hàm vô hướng trả về `BOOLEAN` kiểm tra tư cách mượn sách.
     - `fn_TimKiem_HocLieu(p_TuKhoa)`: Tìm kiếm toàn văn dựa trên độ tương đồng GIN Trigram (`pg_trgm`).
     - `fn_TimKiem_HocLieu_Dong(...)`: Kỹ thuật Dynamic SQL (`EXECUTE format/query`) với whitelist chống SQL Injection.
     - `fn_BaoCao_TongQuan_ThuVien()`: Báo cáo dashboard tổng quan chỉ số thư viện.
  3. **Thủ tục & Views:**
     - `sp_TraSach(p_MaPhieu)`: Thủ tục trả sách với Pessimistic Locking, phối hợp cùng các Triggers xử lý trọn vẹn luồng hoàn trả.
     - `v_HocLieu_DayDu`: View đa hình gom nhóm đầy đủ các thuộc tính lớp con, tác giả và chủ đề.
     - `v_BaoCao_MuonTra`: View báo cáo tình hình mượn trả, tính ngày trễ và tiền phạt.
