# Đồ án 3: Thiết kế CSDL Thư viện học liệu đa phương tiện

<p align="left">
  <img src="https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL"/>
  <img src="https://img.shields.io/badge/DBeaver-382923?style=for-the-badge&logo=dbeaver&logoColor=white" alt="DBeaver"/>
  <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java"/>
  <img src="https://img.shields.io/badge/ObjectDB-4CAF50?style=for-the-badge&logo=database&logoColor=white" alt="ObjectDB"/>
  <img src="https://img.shields.io/badge/IntelliJ_IDEA-000000.svg?style=for-the-badge&logo=intellij-idea&logoColor=white" alt="IntelliJ"/>
</p>

Đây là thư mục chứa toàn bộ mã nguồn, tài liệu và script cho Đồ án môn học Cơ sở dữ liệu Nâng cao (Đề tài 3).

## 📂 Cấu trúc thư mục hiện tại
- `/database`: Chứa các file script SQL (tạo bảng, sinh dữ liệu mẫu).
- `/java_objectdb`: Chứa mã nguồn dự án Java kết nối ObjectDB.
- `/scripts`: Chứa các đoạn script (Python/Java) để test giao dịch (transaction/tranh chấp).
- `/docs`: Chứa báo cáo, sơ đồ EER và tài liệu liên quan.

## 🛠️ Danh sách Công cụ cần cài đặt (Giai đoạn đầu)
Để bắt đầu làm việc với dự án, mỗi thành viên trong nhóm cần chuẩn bị các phần mềm sau:

1. **PostgreSQL & DBeaver (hoặc pgAdmin)**
   - **Mục đích:** Dùng cho phần thiết kế CSDL Quan hệ.
   - **Ghi chú:** Đã bao gồm cả pgAdmin 4 (giao diện quản lý) khi cài đặt. Cần ghi nhớ mật khẩu `postgres` lúc cài.

2. **ObjectDB (Bản SDK Portable)**
   - **Mục đích:** Dùng cho phần thiết kế CSDL Hướng đối tượng và truy vấn OQL.
   - **Ghi chú:** Không cần cài đặt. Chỉ cần giải nén và mở `bin/explorer.exe` để xem dữ liệu. Thư viện sẽ được tích hợp vào code Java sau.

3. **Java JDK 17 (Trở lên)**
   - **Mục đích:** Môi trường chạy ObjectDB và code ứng dụng.

4. **IDE (Trình soạn thảo Code)**
   - **Đề xuất:** IntelliJ IDEA (Community) hoặc Visual Studio Code.
   - **Mục đích:** Dùng để viết code Java.

5. **Phần mềm thiết kế sơ đồ (Tùy chọn)**
   - **Đề xuất:** draw.io hoặc dbdiagram.io để vẽ sơ đồ EER.

---
## 💡 Mẹo nhỏ (Tips)
**Cách Reset (Xóa trắng) Database nhanh:**
Trong quá trình phát triển, nếu bạn code sai, muốn sửa file SQL và chạy tạo lại các bảng từ đầu mà không bị báo lỗi `Table already exists` (Bảng đã tồn tại), hãy chạy 2 dòng lệnh "quyền lực" sau trong pgAdmin trước khi chạy script tạo bảng mới:
```sql
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
```
*(Lưu ý: Lệnh này sẽ **xóa vĩnh viễn** toàn bộ bảng và dữ liệu trong Database hiện tại. Chỉ dùng khi bạn đang test và muốn reset làm lại từ đầu).*

---
*Lưu ý: Môi trường cơ bản đã sẵn sàng, các cấu hình nâng cao sẽ được thêm vào trong quá trình phát triển.*