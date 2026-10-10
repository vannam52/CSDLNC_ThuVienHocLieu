<p align="center">
  <img src="https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL"/>
  <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java"/>
  <img src="https://img.shields.io/badge/ObjectDB-4CAF50?style=for-the-badge&logo=database&logoColor=white" alt="ObjectDB"/>
  <img src="https://img.shields.io/badge/Python-3776AB?style=for-the-badge&logo=python&logoColor=white" alt="Python"/>
</p>

# Đồ án môn học: Cơ Sở Dữ Liệu Nâng Cao (Advanced Databases)

**Chủ đề: Đề tài 3 - Thiết kế Cơ sở dữ liệu Thư viện học liệu đa phương tiện**

👥 **Nhóm thực hiện:**
- Nguyễn Thành Nhân
- Chế Văn Nam
- Hồ Minh Khuyến
- Đặng Hoàng Phúc
- Lý Nguyễn

---

## 📝 1. Giới thiệu đề tài (Mục tiêu và Phạm vi)
- **Mục tiêu:** Quản lý thống nhất sách, luận văn, video bài giảng và tài liệu điện tử; nghiên cứu kế thừa và dữ liệu đa trị.
- **Câu hỏi nghiên cứu:** Cách biểu diễn các loại học liệu và các thuộc tính đa trị ảnh hưởng thế nào đến vấn đề dư thừa dữ liệu, hiệu suất truy vấn và khả năng mở rộng của hệ thống?
- **Phạm vi quản lý:** Hệ thống quản lý Học liệu, tác giả, chủ đề (thuộc tính đa trị), bản sao sách vật lý, bạn đọc, luồng phiếu mượn trả và quyền truy cập học liệu.

## ⚙️ 2. Sơ đồ Kiến trúc & Cách hoạt động (Workflow)

Dự án được vận hành song song trên 2 hệ quản trị CSDL để phục vụ mục đích "So sánh" (Comparative Study) theo yêu cầu đồ án.

```mermaid
graph TD;
    subgraph Bước 1: Triển khai CSDL
        A[Python Script] -->|1. Khởi tạo Database & Bảng| B[(PostgreSQL)];
        A -->|2. Fake Data| B;
    end
    
    subgraph Bước 2: Ứng dụng Java Console
        C[Java Application] -->|JDBC / SQL| B;
        C -->|JPA / OQL| D[(ObjectDB .odb)];
    end
    
    B --> E[Đánh giá: Transaction, SQL 4NF, EXPLAIN ANALYZE];
    D --> F[Đánh giá: Lập trình OQL, Ánh xạ Kế thừa];
```

**Cách hoạt động chi tiết:**
1. Kịch bản Python `setup_library_management_db.py` sẽ đọc các file `01_schema.sql` và `02_data_sample.sql` để kết nối vào PostgreSQL, tạo các bảng (tuân thủ chuẩn hóa 4NF) và bơm dữ liệu mượn/trả mẫu.
2. Ứng dụng Java Console đóng vai trò trung tâm xử lý logic. Thông qua các tuỳ chọn trên Menu, người dùng có thể kích hoạt các chức năng chạy ngầm (truy vấn SQL đệ quy, giả lập 2 luồng tranh chấp mượn sách bằng Pessimistic Locking).
3. Ở tính năng số 6, ứng dụng Java không gọi PostgreSQL nữa mà gọi ObjectDB qua JPA, thực thi các câu lệnh **OQL (Object Query Language)** để chứng minh sự tiện lợi khi không cần `JOIN` bảng.

## 📂 3. Cấu trúc Mã nguồn
```text
CSDLNC_ThuVienHocLieu/
├── src/                # Toàn bộ mã nguồn Java (Console App kết nối cả PostgreSQL & ObjectDB)
│   ├── main/java       # Code DAO, Service, Models
│   └── main/resources  # File cấu hình JPA (persistence.xml) và Script SQL chính thức
├── docs/               # Chứa toàn bộ báo cáo phân tích, sơ đồ EER và So sánh (SQL vs OQL)
├── scripts/            # Kịch bản Python tự động tạo Database, sinh Mock Data
├── pom.xml             # Tệp cấu hình thư viện Maven (PostgreSQL Driver, ObjectDB)
└── run.bat / mvn.cmd   # Script khởi chạy chương trình nhanh chóng
```

## 🚀 4. Hướng dẫn Khởi chạy và Kiểm thử

### Yêu cầu cấu hình môi trường:
Để có thể chạy được dự án này (kể cả trên máy giáo viên), bạn cần chuẩn bị:
1. **Java JDK 17+:** [Tải tại đây](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html). Chú ý cấu hình biến môi trường `JAVA_HOME`.
2. **PostgreSQL 14+:** [Tải tại đây](https://www.postgresql.org/download/). (Mật khẩu user `postgres` mặc định để là `12345` cho đồng bộ, nếu máy thầy cô là mật khẩu khác, hãy sửa lại trong file `setup_library_management_db.py`).
3. **Python 3.10+:** [Tải tại đây](https://www.python.org/downloads/). Dùng để chạy script tạo CSDL tự động.
   - Cài thêm 2 thư viện phụ trợ cho Python: `pip install psycopg2 faker`
4. **Apache Maven:** Đã được nhóm tích hợp sẵn file `mvn.cmd` vào mã nguồn nên không cần cài đặt ngoài.

### Quy trình khởi chạy dự án:

**Bước 1: Khởi tạo CSDL tự động bằng Python (Bắt buộc)**
Mở Terminal (hoặc CMD/PowerShell) tại thư mục gốc của đồ án và chạy lệnh sau để tự tạo Database `library_management` và chèn các dữ liệu mẫu khớp với kịch bản test:
```powershell
python scripts\setup_library_management_db.py
```

**Bước 2: Sinh dữ liệu khổng lồ - Big Data (Tuỳ chọn)**
Để chứng minh cho giáo viên thấy kỹ thuật Đánh Chỉ Mục (Index) ở Menu số 7 hoạt động hiệu quả, hãy chạy thêm lệnh sau để bơm 10.000+ bản ghi giả lập vào DB:
```powershell
python scripts\generate_mock_data.py
```

**Bước 3: Biên dịch và Chạy ứng dụng Java Console**
Gõ 2 lệnh sau để Maven tự động tải thư viện (PostgreSQL Driver, ObjectDB) và chạy chương trình chính:
```powershell
.\mvn.cmd clean compile
.\run.bat
```

**Bước 4: Kịch bản Kiểm thử qua Menu (Kịch bản "ăn điểm")**
Khi màn hình Console hiện lên, hãy biểu diễn cho giáo viên theo các Menu đắt giá nhất sau:
- **Chọn Menu 3:** Thể hiện các câu lệnh SQL siêu khó (`WITH RECURSIVE` đệ quy danh mục, `GROUP BY ... HAVING`).
- **Chọn Menu 4 & 5:** Kích hoạt chức năng Transaction 2 luồng tranh chấp nhau mượn sách. Báo cáo sẽ chứng minh được tính chất ACID bằng khóa bi quan (Pessimistic Locking `FOR UPDATE`).
- **Chọn Menu 6:** Thực thi OQL (Object Query Language) để truy xuất dữ liệu kế thừa không cần dùng lệnh `JOIN` như SQL, chứng minh tính ưu việt của CSDL Đối tượng.
- **Chọn Menu 7:** Chạy báo cáo đo lường chi phí `EXPLAIN ANALYZE` bằng Mili-giây để so sánh tốc độ quét Seq Scan vs Index Scan.

## 📚 5. Tài liệu tham khảo
1. **Fundamentals of Database Systems:** Đọc Chương 4 về EER, Chương 12 về CSDL đối tượng và Chương 15 về thuật toán thiết kế, các phụ thuộc nâng cao. [pearson.com](https://www.pearson.com)
2. **Database System Concepts – bài giảng:** Sử dụng phần *Relational Database Design* để nghiên cứu phụ thuộc và chuẩn hóa. slides
3. **ObjectDB – Developer's Guide:** Tham khảo nếu nhóm triển khai thêm bản mẫu lưu trữ đối tượng. [ObjectDB](https://www.objectdb.com)