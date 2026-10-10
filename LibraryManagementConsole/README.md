# Hướng Dẫn Khởi Chạy Đồ Án Quản Lý Thư Viện

## 1. Khởi tạo Cơ sở dữ liệu PostgreSQL
Mở DBeaver / psql và thực thi các file script trong `src/main/resources/` theo đúng thứ tự:
1. `01_schema.sql`: Khởi tạo Schema và cấu trúc các bảng (Class Table Inheritance & 4NF).
2. `02_data_sample.sql`: Nạp dữ liệu mẫu ban đầu.
3. `03_advanced_queries.sql`: Các truy vấn SQL nâng cao và đệ quy.
4. `04_transaction_procedure.sql`: Định nghĩa Stored Procedure `sp_MuonSach` (Nhớ mở bỏ comment nếu có).

## 2. Cấu hình Môi trường Java
Thiết lập các biến môi trường (Optional):
- `DB_URL`: URL kết nối PostgreSQL
- `DB_USER`: Tài khoản CSDL
- `DB_PASSWORD`: Mật khẩu CSDL

## 3. Khởi chạy
Chạy ứng dụng qua file `com.library.Main`.