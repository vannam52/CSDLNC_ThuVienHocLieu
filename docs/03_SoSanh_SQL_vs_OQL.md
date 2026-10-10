# So sánh Giải pháp Thiết kế: Relational vs Object-Oriented (SQL vs OQL)

Một trong những trọng tâm nghiên cứu của Đề tài 3 là đánh giá hiệu quả giữa việc lưu trữ dữ liệu bằng CSDL Quan hệ (PostgreSQL) và CSDL Hướng đối tượng (ObjectDB). Báo cáo này so sánh hai khía cạnh chính: Chiến lược ánh xạ kế thừa và Hiệu năng truy vấn.

## 1. So sánh Chiến lược Ánh xạ Kế thừa (Inheritance Mapping)

Trong hệ thống Thư viện, thực thể `HocLieu` đóng vai trò lớp cha (Superclass) cho các lớp con (Subclass) như `Sach`, `LuanVan`, `VideoBaiGiang`, `TaiLieuDienTu`.

### 1.1. CSDL Quan hệ (Relational Database)
Do RDBMS không hỗ trợ khái niệm kế thừa nguyên thủy (trừ một số ngoại lệ như tính năng kế thừa của riêng PostgreSQL), ta phải dùng kỹ thuật ánh xạ. Dự án đang sử dụng **Class Table Inheritance (Bảng cha - Bảng con)**:
*   **Ưu điểm:** Dữ liệu chuẩn hóa tốt, không có giá trị `NULL` dư thừa (không gặp tình trạng `SoTrang` của LuanVan bị bỏ trống).
*   **Nhược điểm:** Khi muốn lấy toàn bộ thông tin của một đối tượng cụ thể (VD: sách), phải thực hiện phép `JOIN` giữa bảng `HocLieu` và `Sach`. Khi số lượng lớp con nhiều, số lượng phép `JOIN` tăng lên gây ảnh hưởng hiệu suất đọc (Read Performance).
*   *Lựa chọn thay thế (Single Table):* Nếu dồn tất cả thuộc tính vào một bảng `HocLieu` khổng lồ, ta loại bỏ được `JOIN` nhưng gặp vấn đề về lãng phí không gian lưu trữ và khó đảm bảo tính toàn vẹn (rất nhiều cột `NULL`).

### 1.2. CSDL Hướng đối tượng (Object-Oriented Database)
Với ObjectDB / JPA, khái niệm Kế thừa được ánh xạ trực tiếp (Direct Mapping):
*   `@Inheritance(strategy = InheritanceType.JOINED)` hoặc `@Inheritance(strategy = InheritanceType.SINGLE_TABLE)` được Java tự động quản lý.
*   **Ưu điểm vượt trội:** Developer không cần viết lệnh `JOIN` thủ công. Các Object được tải lên bộ nhớ một cách tự nhiên. Tính toàn vẹn cấu trúc và logic đa hình (Polymorphism) được giữ nguyên vẹn từ Code xuống Database.

---

## 2. So sánh Ngôn ngữ Truy vấn (SQL vs OQL/JPQL)

### 2.1. Truy vấn Dữ liệu Đa trị và Liên kết phức tạp
**Bài toán:** Tìm các cuốn Sách có chủ đề là "Cơ sở dữ liệu".

**Bằng SQL (PostgreSQL):**
Cần dùng 3 bảng và 2 phép JOIN vì thuộc tính Đa trị phải tách thành bảng trung gian:
```sql
SELECT s.ISBN, h.TieuDe 
FROM Sach s
JOIN HocLieu h ON s.MaHocLieu = h.MaHocLieu
JOIN HocLieu_ChuDe hc ON h.MaHocLieu = hc.MaHocLieu
JOIN ChuDe c ON hc.MaChuDe = c.MaChuDe
WHERE c.TenChuDe = 'Cơ sở dữ liệu';
```
*Nhược điểm:* Câu lệnh dài, tư duy theo hướng "tập hợp" và "khóa ngoại".

**Bằng OQL / JPQL (ObjectDB):**
Nhờ các cấu trúc dữ liệu collection (như `List<ChuDe>`) được bọc thẳng trong Object, câu truy vấn ngắn gọn hơn hẳn bằng cách duyệt qua đường dẫn thuộc tính (Path Expression):
```jpql
SELECT s FROM Sach s JOIN s.chuDeList c WHERE c.tenChuDe = 'Cơ sở dữ liệu'
```
*Ưu điểm:* Cú pháp tự nhiên, dễ đọc. Developer duyệt quan hệ giống hệt như đang duyệt List trong Java.

### 2.2. Xử lý Dữ liệu Đệ quy (Recursive Data)
**Bài toán:** Cây chủ đề phân cấp (VD: Công nghệ Thông tin -> Phần mềm -> Cơ sở dữ liệu). Lấy toàn bộ Chủ đề con của "Công nghệ Thông tin".

**Bằng SQL:**
Sử dụng Common Table Expressions (CTE) đệ quy `WITH RECURSIVE`:
```sql
WITH RECURSIVE ChuDeTree AS (
    SELECT MaChuDe, TenChuDe, ChuDeCha FROM ChuDe WHERE TenChuDe = 'Công nghệ thông tin'
    UNION ALL
    SELECT c.MaChuDe, c.TenChuDe, c.ChuDeCha 
    FROM ChuDe c INNER JOIN ChuDeTree ct ON c.ChuDeCha = ct.MaChuDe
)
SELECT * FROM ChuDeTree;
```
*Đánh giá:* Rất mạnh mẽ ở tầng Database nhưng cú pháp phức tạp, khó debug.

**Bằng OQL / Java:**
Thường xử lý bằng cách Load lớp gốc lên Java, sau đó dùng thuật toán đệ quy trên RAM (Duyệt qua danh sách `childTopics` của Object). 
*Đánh giá:* Dễ viết code, nhưng có nguy cơ gây tràn bộ nhớ (N+1 Select problem) nếu số lượng nút trên cây quá lớn và không dùng `FETCH JOIN` hợp lý.

---

## 3. Kết Luận So Sánh
*   **PostgreSQL (SQL)** phù hợp với việc kiểm soát chặt chẽ toàn vẹn dữ liệu, các truy vấn xử lý số lượng lớn data, và các thao tác giao dịch (Transaction) phức tạp (như xử lý đồng thời khi mượn sách).
*   **ObjectDB (OQL/JPQL)** tỏ ra vượt trội về thời gian phát triển (Development Time) và xử lý dữ liệu có tính phân cấp sâu (Inheritance/Polymorphism), loại bỏ hoàn toàn "Impedance Mismatch" (sự bất đồng bộ) giữa hướng đối tượng và quan hệ.

Việc áp dụng cả 2 mô hình trong đồ án giúp chứng minh rõ ràng những trade-off (sự đánh đổi) của mỗi mô hình trên từng hoàn cảnh bài toán cụ thể.
