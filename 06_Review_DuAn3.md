# Review dự án Đề tài 3 – Thư viện học liệu đa phương tiện

> **Yêu cầu của người dùng:** "đọc và review lại dự án 3 có đúng với yêu cầu chưa"

> [!WARNING]
> **Lưu ý về độ tin cậy.** File PDF đề cương (`Gửi SV - Hướng dẫn các đề tài CSDL nâng cao.pdf`) **không đọc được** trong phiên review này vì định dạng không được hỗ trợ.
> Vì vậy các yêu cầu đánh dấu **[chưa kiểm chứng với PDF]** (số lượng truy vấn, số trang báo cáo, trọng số điểm, cấu trúc 9 chương, tối thiểu 5 câu OQL...) chỉ là **suy đoán** và cần đối chiếu lại với đề cương gốc.
> Các nhận xét về **mã nguồn, SQL, tài liệu `docs/`, README** đã được kiểm chứng trực tiếp bằng cách đọc file.
> Các con số phần trăm và điểm tổng ("~5.5/10") là **ước lượng chủ quan**, không phải thang điểm chính thức.

---

## I. Tổng quan

| Hạng mục | Tình trạng hiện tại | Mức độ (ước lượng) |
| :--- | :--- | :---: |
| 1. Phân tích yêu cầu & EER | Có `SoDoEER.svg`, phân cấp `HocLieu`; **thiếu bảng `TepTaiNguyen`, `QuyenTruyCap` trong CSDL thật** | ~75% |
| 2. Chuẩn hóa & 4NF | Đã tách bảng trung gian; **chưa có chứng minh lý thuyết (MVD, lossless join, bảng phẳng M×N)** | ~45% |
| 3. So sánh ánh xạ kế thừa | Mới có lý thuyết trong docs; **chưa có DDL Single Table và số liệu thực nghiệm** | ~50% |
| 4. SQL nâng cao & thiết kế vật lý | Mới 4 truy vấn, 1 procedure; **chưa có Trigger, Function, Dynamic SQL, Index thật** | ~40% |
| 5. CSDL hướng đối tượng & OQL | **Chỉ in chữ bằng `System.out.println`, chưa chạy ObjectDB thật; model là POJO** | ~30% |
| 6. Transaction & đồng thời | `sp_MuonSach` + `FOR UPDATE`, demo 2 thread, demo rollback | ~85% |
| 7. Báo cáo & hồ sơ | Mới có các file `.md`; chưa thấy báo cáo Word/PDF, slide, biên bản nhóm | ~35% |

---

## II. Chi tiết

### 1. EER & phạm vi
**Đã có**
- [`SoDoEER.svg`](../SoDoEER.svg), [`01_DacTa_YeuCau_ThuVien.md`](01_DacTa_YeuCau_ThuVien.md), [`02_DacTa_EER_Va_RangBuoc.md`](02_DacTa_EER_Va_RangBuoc.md).

**Chưa khớp**
- `docs/02` mô tả các thực thể `TepTaiNguyen`, `QuyenTruyCap`, `NhatKyTruyCap`, `DanhGia`, `BaoKhoaHoc`, `SinhVien/GiangVien/NghienCuuSinh`, `CapDoBaoMat`... nhưng [`01_schema.sql`](../src/main/resources/01_schema.sql) **không có** các bảng/cột này.
- Hai tài liệu `docs/01` và `docs/02` **mâu thuẫn nhau**: `docs/01` dùng `TaiLieuDienTu`, `docs/02` dùng `BaoKhoaHoc`/`TepTaiNguyen`. `docs/01` lại tuyên bố "khớp 100% CSDL thực tế", điều này không đúng với `docs/02`.
- Chưa có Data Dictionary dạng bảng (kiểu dữ liệu, NULL, PK/FK, ràng buộc).
- Schema chưa có `CHECK`, ràng buộc miền cho `TrangThai`... Comment trong schema liệt kê `'Bao tri'`, trong khi docs nói thêm `'Bị mất'`.

### 2. Chuẩn hóa & 4NF (trọng tâm Đề 3)
**Đã có:** tách `HocLieu_TacGia`, `HocLieu_ChuDe`.

**Thiếu**
- Phân tích MVD: `MaHocLieu ↠ MaTacGia`, `MaHocLieu ↠ MaChuDe` và giả định độc lập.
- Ví dụ bảng phẳng `R(MaHocLieu, MaTacGia, MaChuDe)`: 2 tác giả × 3 chủ đề = 6 dòng, chỉ ra dị thường thêm/sửa/xóa.
- Chứng minh phép nối không mất mát (Fagin).
- Truy vấn `HAVING COUNT(...) > 1` (Menu 2) chỉ là truy vấn thống kê, **không chứng minh 4NF**.
- **[chưa kiểm chứng với PDF]** Phần chuẩn hóa chung (phụ thuộc hàm, bao đóng, khóa, phủ tối thiểu, 3NF/BCNF) chưa có cho bảng nào.

### 3. Ánh xạ kế thừa
- Hiện chỉ dùng Class Table Inheritance.
- Chưa có phương án Single Table, chưa đo số NULL/hiệu năng JOIN, chưa có bảng so sánh định lượng.
- `docs/03` còn nhắc "`SoTrang` của LuanVan" như ví dụ NULL, nhưng `SoTrang` thuộc `Sach`, không thuộc `LuanVan`.

### 4. SQL nâng cao & thiết kế vật lý
Hiện có: 1 CTE đệ quy, 1 GROUP BY/HAVING, 1 LEFT JOIN đa hình, 1 EXPLAIN ANALYZE, 1 procedure.

**Thiếu**
- Trigger (vd. giới hạn số sách mượn, tính tiền phạt).
- Function (vd. `fn_TinhTienPhat`).
- Dynamic SQL (`EXECUTE format(...)`).
- Subquery tương quan, `NOT EXISTS`, window function.
- **Index thật:** schema/setup **không** chạy `CREATE EXTENSION pg_trgm` hay `CREATE INDEX ... USING gin (... gin_trgm_ops)`. Menu 7 ghi "GIN Trigram" nhưng thực tế chỉ đo `LIKE '%Database%'` trên bảng không index (Seq Scan). Chưa có so sánh trước/sau index.
- `docs/04` nói Menu 7 chứng minh "cost trước và sau khi đánh index", nhưng code chỉ chạy một lần, không có bước tạo/xóa index.
- `setup_library_management_db.py` **không** nạp `03_advanced_queries.sql` dù README ghi "chạy 3 file SQL" (thực tế là 01, 02, 04).
- **[chưa kiểm chứng với PDF]** Yêu cầu "10–12 truy vấn/nghiệp vụ".

### 5. CSDL hướng đối tượng & OQL
- [`OqlQueryDAO.java`](../src/main/java/com/library/dao/OqlQueryDAO.java) **chỉ in chuỗi**, kể cả dòng "Kết quả: [HL01] ... (1376 trang)" là hard-code, không truy vấn ObjectDB.
- [`ObjectDBConfig.java`](../src/main/java/com/library/config/ObjectDBConfig.java) **không được gọi ở đâu** trong code.
- Model (`HocLieu`, `Sach`, ...) là POJO, không có `@Entity`, `@Id`, `@Inheritance`.
- `HocLieu` không có collection `danhSachTacGia`, nhưng câu OQL in ra lại dùng `SIZE(h.danhSachTacGia)`; `Sach` không có `chuDeList` như `docs/03` mô tả.
- `TacGia`, `ChuDe` trong model chỉ có `id, ten`, lệch với bảng SQL.
- `PhieuMuon` trong model thiếu nhiều trường so với bảng SQL.
- `persistence.xml` chưa liệt kê entity class nào.
- README nói file `thuvien.odb`, nhưng `persistence.xml` trỏ `$objectdb/data/library.odb`.
- **[chưa kiểm chứng với PDF]** Tối thiểu 5 câu OQL, 4–6 lớp có OID, phương thức.

> [!WARNING]
> Nếu giảng viên yêu cầu chạy/debug phần ObjectDB thật, phần này hiện chỉ là mô phỏng bằng chữ. README/docs lại khẳng định đang "đối chiếu mô hình với ObjectDB", điều này gây hiểu nhầm.

### 6. Transaction & đồng thời
**Đã có**
- [`sp_MuonSach`](../src/main/resources/04_transaction_procedure.sql) dùng `SELECT ... FOR UPDATE`.
- Menu 4: 2 thread cùng mượn `BS01` (dùng `CountDownLatch`).
- Menu 5: rollback do vi phạm khóa ngoại (`BD_ERR`).

**Cần sửa**
- Menu 4 chỉ chạy đúng khi `BS01` còn `San sang`; chạy lần 2 sẽ báo lỗi cả hai phiên (cần reset trạng thái / mã phiếu cố định `PM_PHIEN_1/2` sẽ trùng khóa ở lần sau).
- Nên có kịch bản **không** dùng `FOR UPDATE` để thấy Lost Update, rồi so sánh với có khóa.
- [`scripts/test_transaction.py`](../scripts/test_transaction.py): DB đặt là `thuvien_db` (đúng phải là `library_management`), mã bản sao `BS_001` (đúng phải là `BS01`) và trạng thái `'Dang cho muon'` (hệ thống dùng `'Dang muon'`). Chạy độc lập sẽ lỗi.
- **Mật khẩu không nhất quán:** README/`docs/04` ghi `12345`, còn `DatabaseConfig.java`, `setup_library_management_db.py`, `generate_mock_data.py`, `test_transaction.py` hard-code `12092006`. Nên đưa ra biến môi trường/file cấu hình.
- Menu 5: sau lỗi, `Connection` được đóng khi thoát `try-with-resources` mà không có `rollback()` tường minh (vẫn rollback ngầm, nhưng nên gọi rõ để minh họa).

### 7. Báo cáo & hồ sơ
- Chưa thấy báo cáo Word/PDF, slide, biên bản phân công nhóm trong thư mục dự án.
- **[chưa kiểm chứng với PDF]** Yêu cầu 20–30 trang, 9 chương, thời lượng thuyết trình, CLO7.

### 8. Các điểm nhỏ khác
- [`generate_mock_data.py`](../scripts/generate_mock_data.py) chỉ sinh `Sach` (không sinh `LuanVan`, `VideoBaiGiang`, `TacGia`, `ChuDe`, `PhieuMuon`) → Menu 3 (thống kê lượt mượn) và Menu 2 không có dữ liệu lớn.
- README còn một đoạn lẫn lệnh `python scripts\setup_library_management_db.py` chèn vào giữa câu ở mô tả Menu 6.
- README liệt kê `mvn.cmd`, nhưng trong thư mục chỉ có `mvn.cmd.bak`; `run.bat` hard-code `C:\tools\apache-maven-3.9.9\bin`.
- README ghi "10 Entities Java", model hiện có 10 file nhưng nhiều lớp là bản rút gọn.
- Lệnh `mvn compile` chạy thử trong phiên review trả về "Access is denied", chưa xác nhận được dự án biên dịch được hay không.

---

## III. Kế hoạch đề xuất

1. **Sửa lỗi cấu hình ngay:** đồng bộ mật khẩu, sửa `test_transaction.py`, làm Menu 4 chạy lặp lại được (reset dữ liệu, sinh mã phiếu duy nhất).
2. **Index thật:** thêm `CREATE EXTENSION pg_trgm` và index GIN; Menu 7 đo trước/sau `DROP/CREATE INDEX`.
3. **ObjectDB thật:** gắn annotation JPA, thêm collection quan hệ, liệt kê class trong `persistence.xml`, seed dữ liệu và chạy JPQL qua `EntityManager`.
4. **Bổ sung SQL nâng cao:** Trigger, Function, Dynamic SQL, subquery/`NOT EXISTS`/window function.
5. **Đồng bộ EER ↔ schema:** hoặc bổ sung bảng còn thiếu, hoặc sửa tài liệu cho khớp CSDL thực tế; thống nhất `docs/01` và `docs/02`.
6. **Tài liệu lý thuyết:** MVD/4NF, bảng phẳng M×N, lossless join; so sánh Single Table vs Class Table có số liệu; chuẩn hóa chung.
7. **Hồ sơ nộp:** báo cáo, slide, biên bản nhóm — đối chiếu cấu trúc với đề cương PDF gốc.
