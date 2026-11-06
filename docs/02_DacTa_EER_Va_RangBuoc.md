# BẢN ĐẶC TẢ MÔ HÌNH EER, RÀNG BUỘC TOÀN VẸN & CHỨNG MINH LÝ THUYẾT 4NF
## HỆ THỐNG QUẢN LÝ THƯ VIỆN HỌC LIỆU ĐA PHƯƠNG TIỆN (ĐỀ TÀI 3)
**Môn học:** Cơ sở dữ liệu Nâng cao  
**Nhóm thực hiện:** Nhóm 5 thành viên (Mô hình hóa EER, Chuẩn hóa 4NF, Kế thừa OODBMS, Giao dịch đồng thời)

---

## PHẦN I: MÔ TẢ THẾ GIỚI THỰC & PHẠM VI HỆ THỐNG (MINIWORLD SPECIFICATION)

Thư viện trường Đại học quản lý một kho học liệu đa phương tiện phong phú, phục vụ nhu cầu giảng dạy, học tập và nghiên cứu khoa học. Hệ thống giải quyết 4 bài toán trọng tâm của môn học CSDL Nâng cao:
1. **Mô hình hóa Kế thừa Đa hình (EER Inheritance):** Phân cấp giữa học liệu chung và các loại học liệu chuyên biệt có cấu trúc thuộc tính khác nhau.
2. **Chuẩn hóa Phụ thuộc Đa trị (4NF Normalization):** Xử lý độc lập giữa hai tập thuộc tính đa trị: Tác giả và Chủ đề học liệu, triệt tiêu tích Đề-các và dị thường dữ liệu.
3. **Cấu trúc Đệ quy Phân cấp (Recursive Hierarchy):** Danh mục chủ đề tri thức có khả năng mở rộng vô hạn cấp.
4. **Kiểm soát Giao dịch Đồng thời (Concurrency Control):** Tranh chấp mượn cùng một bản sao vật lý tại một thời điểm bằng kỹ thuật khóa bi quan (**Pessimistic Locking `SELECT ... FOR UPDATE`**) kết hợp hệ thống **Triggers** tự động hóa.

### 1. Phân cấp Học liệu (HocLieu)
* **Học liệu chung (HocLieu):** Lớp cơ sở chứa thông tin dùng chung: `MaHocLieu` (Khóa chính), `TieuDe`, `MoTa`, `NamXuatBan`, `NgonNgu`, `GiaTien`, `NgayNhapKho`, `AnhBia`.
* **Bốn loại học liệu chuyên biệt kế thừa hoàn toàn từ Học liệu cha:**
  * **Sách (Sach):** Có mã quốc tế `ISBN` (Duy nhất), `NhaXuatBan`, `SoTrang`, `LanTaiBan`.
  * **Luận văn (LuanVan):** Có `NguoiHuongDan`, `Truong`, `CapBaoVe` (Thạc sĩ, Tiến sĩ), `ChuyenNganh`, `DiemBaoVe`.
  * **Video bài giảng (VideoBaiGiang):** Có `ThoiLuong` (phút), `DoPhanGiai` (1080p, 4K), `NgonNguPhuDe`, `DungLuong` (MB).
  * **Tài liệu điện tử (TaiLieuDienTu):** Có `DinhDang` (PDF, EPUB, DOCX), `DungLuong` (MB), `QuyenTruyCap` (Public, Nội bộ), `URL_LienKet`.
* **Ràng buộc:** Phân cấp kế thừa mang tính chất **Disjoint, Total (`d, total`)** &mdash; Mỗi học liệu bắt buộc phải thuộc về đúng 1 loại con cụ thể.

### 2. Thuộc tính Đa trị: Tác giả & Cây Chủ đề (Chuẩn 4NF)
* **Tác giả (TacGia):** Một học liệu có thể do nhiều tác giả đồng sáng tác, một tác giả có thể đóng góp cho nhiều học liệu. Có `MaTacGia` (PK), `TenTacGia`, `DonViCongTac`, `Email`.
* **Chủ đề (ChuDe):** Mỗi học liệu có thể thuộc nhiều chuyên mục/chủ đề nghiên cứu. Có `MaChuDe` (PK), `TenChuDe`, `MoTaChuDe`, và `ChuDeCha` (Khóa ngoại tự tham chiếu đệ quy 1:N).

### 3. Bản sao Vật lý trên Kệ sách (BanSao)
* Đối với học liệu có hiện vật trên kệ (Sách, Luận văn), thư viện quản lý từng bản sao vật lý độc lập.
* Có `MaBanSao` (mã vạch định danh dán trên bìa sách), `MaHocLieu` (FK), `TrangThai` (*'San sang', 'Dang muon', 'Bao tri', 'Bi mat'*), `TinhTrangVatLy`, `ViTriKe` (Kho/Kệ sách).

### 4. Bạn đọc & Luồng Giao dịch Mượn Trả
* **Bạn đọc (BanDoc):** `MaBanDoc` (PK), `HoTen`, `Email` (UNIQUE), `SoDienThoai`, `MatKhau`, `LoaiTaiKhoan` (*Sinh vien, Giang vien, Nghien cuu sinh*), `NgayHetHan`, `TaiKhoanBiKhoa`.
* **Phiếu mượn (PhieuMuon):** `MaPhieu` (PK), `MaBanDoc` (FK), `MaBanSao` (FK), `NgayMuon`, `NgayHanTra` (mặc định 14 ngày), `NgayTraThucTe`, `TienPhat` (tự động tính 5.000 VNĐ/ngày trễ hạn qua Trigger), `TrangThai` (*'Dang muon', 'Da tra'*), `GhiChu`.

---

## PHẦN II: BẢNG ÁNH XẠ THỰC THỂ & THUỘC TÍNH (CHUẨN EER)

Dưới đây là danh sách toàn bộ các thực thể cốt lõi được triển khai trong cơ sở dữ liệu:

| Thực thể (Entity) | Vai trò trong Mô hình EER | Thuộc tính Khóa (PK) | Thuộc tính thường | Thuộc tính quan hệ / Khóa ngoại (FK) |
| :--- | :--- | :--- | :--- | :--- |
| **HOCLIEU** | Superclass | <u>MaHocLieu</u> | TieuDe, MoTa, NamXuatBan, NgonNgu, GiaTien, NgayNhapKho, AnhBia | |
| **SACH** | Subclass của HOCLIEU | <u>MaHocLieu</u> (Kế thừa) | <u>ISBN</u> (Khóa phụ), NhaXuatBan, SoTrang, LanTaiBan | FK $\rightarrow$ `HocLieu(MaHocLieu)` |
| **LUANVAN** | Subclass của HOCLIEU | <u>MaHocLieu</u> (Kế thừa) | NguoiHuongDan, Truong, CapBaoVe, ChuyenNganh, DiemBaoVe | FK $\rightarrow$ `HocLieu(MaHocLieu)` |
| **VIDEOBAIGIANG** | Subclass của HOCLIEU | <u>MaHocLieu</u> (Kế thừa) | ThoiLuong, DoPhanGiai, NgonNguPhuDe, DungLuong | FK $\rightarrow$ `HocLieu(MaHocLieu)` |
| **TAILIEUDIENTU** | Subclass của HOCLIEU | <u>MaHocLieu</u> (Kế thừa) | DinhDang, DungLuong, QuyenTruyCap, URL_LienKet | FK $\rightarrow$ `HocLieu(MaHocLieu)` |
| **TACGIA** | Thực thể mạnh | <u>MaTacGia</u> | TenTacGia, DonViCongTac, Email | |
| **CHUDE** | Thực thể mạnh (Đệ quy) | <u>MaChuDe</u> | TenChuDe, MoTaChuDe | FK đệ quy `ChuDeCha` $\rightarrow$ `ChuDe(MaChuDe)` |
| **BANSAO** | Thực thể phụ thuộc (Weak) | <u>MaBanSao</u> | TrangThai, TinhTrangVatLy, ViTriKe | FK `MaHocLieu` $\rightarrow$ `HocLieu(MaHocLieu)` |
| **BANDOC** | Thực thể người dùng | <u>MaBanDoc</u> | HoTen, <u>Email</u>, SoDienThoai, MatKhau, LoaiTaiKhoan, NgayHetHan, TaiKhoanBiKhoa | |
| **PHIEUMUON** | Thực thể giao tác | <u>MaPhieu</u> | NgayMuon, NgayHanTra, NgayTraThucTe, TienPhat, TrangThai, GhiChu | FK `MaBanDoc`, FK `MaBanSao` |
| **HOCLIEU_TACGIA**| Bảng liên kết N:M | <u>(MaHocLieu, MaTacGia)</u> | | Phục vụ chuẩn hóa 4NF |
| **HOCLIEU_CHUDE** | Bảng liên kết N:M | <u>(MaHocLieu, MaChuDe)</u> | | Phục vụ chuẩn hóa 4NF |

> **Ghi chú về phân hệ mở rộng (Phase 2 - Extended Scope):**
> Trong các kịch bản triển khai mở rộng thư viện số quy mô viện - trường toàn quốc, hệ thống có thể tích hợp thêm các thực thể nâng cao như `BaoKhoaHoc` (kế thừa HocLieu với chỉ số DOI, IF), `TepTaiNguyen` (quản lý đa tệp đính kèm), `QuyenTruyCap` (phân quyền DRM) và `NhatKyTruyCap` (Audit Log truy vết IP). Bản cài đặt hiện tại tập trung cốt lõi vào 10 thực thể trên nhằm bảo đảm tính đồng bộ 100% giữa lược đồ quan hệ PostgreSQL và 10 JPA Entities trên ObjectDB.

---

## PHẦN III: CÁC RÀNG BUỘC CHUYÊN BIỆT HÓA EER (SPECIALIZATION CONSTRAINTS)

### 1. Phân cấp Học liệu: `HOCLIEU` $\rightarrow$ `{SACH, LUANVAN, VIDEOBAIGIANG, TAILIEUDIENTU}`
* **Ràng buộc Rời rạc (Disjointness Constraint):** Ký hiệu **`d` (Disjoint)**.
  * *Ý nghĩa:* Một học liệu chỉ có thể là Sách, hoặc Luận văn, hoặc Video bài giảng, hoặc Tài liệu điện tử; không thể đồng thời thuộc nhiều hơn một lớp con.
* **Ràng buộc Tham gia (Completeness Constraint):** Ký hiệu **Đường nối kép (`Total Specialization`)**.
  * *Ý nghĩa:* Mọi thực thể trong `HOCLIEU` bắt buộc phải là một trong bốn dạng cụ thể trên; không tồn tại học liệu chung chung trừu tượng không có thuộc tính nghiệp vụ.

### 2. Mối quan hệ Đệ quy (Self-relationship) trên `CHUDE`
* Cột `ChuDeCha` tự tham chiếu tới khóa chính `MaChuDe` với bản số $0..1$ (Một chủ đề có tối đa 1 chủ đề cha, và có thể có từ $0..N$ chủ đề con).
* Điều này hỗ trợ truy vấn duyệt cây phân cấp vô hạn cấp bằng biểu thức bảng chung đệ quy **`WITH RECURSIVE`** trong PostgreSQL.

---

## PHẦN IV: CÁC MỐI QUAN HỆ & BẢN SỐ (CARDINALITY & PARTICIPATION)

| Mối quan hệ | Thực thể tham gia | Bản số (Cardinality) | Ràng buộc tham gia | Triển khai trong CSDL |
| :--- | :--- | :--- | :--- | :--- |
| **KẾ THỪA** | `HocLieu` $\leftrightarrow$ Lớp con | $1 - 1$ | Toàn phần ở cả 2 phía | PK/FK `MaHocLieu` trên từng bảng con |
| **ĐỒNG SÁNG TÁC** | `HocLieu` $\leftrightarrow$ `TacGia` | $N - M$ | `HocLieu`: $1..N$, `TacGia`: $0..N$ | Bảng trung gian `HocLieu_TacGia` |
| **PHÂN LOẠI** | `HocLieu` $\leftrightarrow$ `ChuDe` | $N - M$ | `HocLieu`: $1..N$, `ChuDe`: $0..N$ | Bảng trung gian `HocLieu_ChuDe` |
| **SỞ HỮU BẢN SAO**| `HocLieu` $\leftrightarrow$ `BanSao` | $1 - N$ | `HocLieu`: $0..N$, `BanSao`: $1..1$ | FK `BanSao.MaHocLieu` $\rightarrow$ `HocLieu` |
| **MƯỢN TRẢ** | `BanDoc` $\leftrightarrow$ `BanSao` | Thông qua `PhieuMuon` | $0..N$ ở cả hai phía | Bảng giao tác `PhieuMuon` (2 FKs) |

---

## PHẦN V: CHỨNG MINH LÝ THUYẾT PHỤ THUỘC ĐA TRỊ & ĐỊNH LÝ FAGIN (CHUẨN 4NF)

Đây là trọng tâm lý thuyết quan trọng nhất của Đề tài 3 môn CSDL Nâng cao.

### 1. Định nghĩa Hình thức về Phụ thuộc Đa trị (Multivalued Dependency - MVD)
Cho lược đồ quan hệ $R(X, Y, Z)$ với $X, Y, Z$ là các tập thuộc tính rời rạc nhau. Ta nói $X$ **xác định đa trị** $Y$ (ký hiệu $X \twoheadrightarrow Y$) nếu và chỉ nếu:
$$\forall t_1, t_2 \in R, \text{ nếu } t_1[X] = t_2[X] \implies \exists t_3, t_4 \in R \text{ sao cho:}$$
$$\begin{cases}
t_3[X] = t_4[X] = t_1[X] = t_2[X] \\
t_3[Y] = t_1[Y] \text{ và } t_3[Z] = t_2[Z] \\
t_4[Y] = t_2[Y] \text{ và } t_4[Z] = t_1[Z]
\end{cases}$$

*Diễn giải trực quan:* Giá trị của tập thuộc tính $Y$ gắn với một giá trị của $X$ chỉ phụ thuộc duy nhất vào $X$ mà **hoàn toàn độc lập** với tập thuộc tính $Z$.

### 2. Thiết lập Bài toán Thực tế trong Đề tài Thư viện
Xét quan hệ chưa chuẩn hóa tập hợp toàn bộ thông tin mô tả học liệu:
$$R(\underline{MaHocLieu}, \underline{MaTacGia}, \underline{MaChuDe})$$
* **Khảo sát nghiệp vụ thực tế:**
  1. Một cuốn giáo trình có thể do **nhiều tác giả** đồng biên soạn độc lập.
  2. Cuốn giáo trình đó có thể được phân loại vào **nhiều chủ đề** nghiên cứu độc lập.
  3. Việc cuốn sách thuộc chủ đề $C_1$ hay $C_2$ hoàn toàn không phụ thuộc vào tác giả $T_1$ hay $T_2$ biên soạn (Tác giả và Chủ đề độc lập tương hỗ: $TacGia \cap ChuDe = \emptyset$).
* **Hệ quả logic:** Tồn tại đồng thời 2 phụ thuộc đa trị phi tầm thường:
  $$MaHocLieu \twoheadrightarrow MaTacGia$$
  $$MaHocLieu \twoheadrightarrow MaChuDe$$

### 3. Minh họa Dị thường Dữ liệu khi Chưa Đạt Chuẩn 4NF (Bùng nổ Tích Descartes)
Giả sử cuốn giáo trình `HL01` ("Database System Concepts") có:
* 2 tác giả: `TG01` (A. Silberschatz), `TG02` (R. Elmasri).
* 2 chủ đề: `CD02` (Cơ sở dữ liệu), `CD03` (CSDL Nâng cao).

Nếu lưu chung trong bảng phẳng $R$, để thỏa mãn định nghĩa MVD, bảng bắt buộc phải lưu trữ toàn bộ tích Đề-các: $2 \times 2 = 4$ bản ghi:

| MaHocLieu | MaTacGia | MaChuDe | Nhận xét Dị Thường (Anomalies) |
| :--- | :--- | :--- | :--- |
| **HL01** | `TG01` (Silberschatz) | `CD02` (CSDL) | Dòng hợp lệ ban đầu |
| **HL01** | `TG01` (Silberschatz) | `CD03` (CSDL Nâng cao) | Lặp lại tác giả `TG01` |
| **HL01** | `TG02` (Elmasri) | `CD02` (CSDL) | Lặp lại chủ đề `CD02` |
| **HL01** | `TG02` (Elmasri) | `CD03` (CSDL Nâng cao) | Lặp lại tác giả `TG02` và chủ đề `CD03` |

#### Phân tích 3 Dị Thường Nghiêm Trọng (Anomalies):
1. **Dị thường Chèn (Insertion Anomaly):** Khi thư viện muốn bổ sung thêm 1 chủ đề mới cho `HL01` (ví dụ: `CD04` - Lập trình), thủ thư bắt buộc phải chèn thêm **2 dòng mới** cho cả 2 tác giả (`HL01, TG01, CD04` và `HL01, TG02, CD04`). Nếu cuốn sách có $N$ tác giả và thêm $M$ chủ đề, hệ thống phải chèn tới $N \times M$ dòng! Nếu chỉ chèn 1 dòng thì bảng sẽ vi phạm tính toàn vẹn của MVD.
2. **Dị thường Xóa (Deletion Anomaly):** Nếu muốn hủy chủ đề `CD03` khỏi sách `HL01`, ta phải xóa đủ cả 2 dòng chứa `CD03`. Nếu sơ suất xóa thiếu 1 dòng, dữ liệu rơi vào trạng thái mâu thuẫn (Silberschatz còn liên kết `CD03` nhưng Elmasri thì mất).
3. **Dị thường Sửa (Update Anomaly):** Nếu thay thế mã tác giả `TG01`, ta phải duyệt cập nhật đồng loạt trên tất cả các dòng chứa mọi chủ đề của cuốn sách đó.

### 4. Định lý Phân Rã Fagin (Fagin's Theorem - 1977)
> **Định lý (Ronald Fagin):** Cho quan hệ $R(X, Y, Z)$. Phép phân rã $R$ thành hai quan hệ $R_1(X, Y)$ và $R_2(X, Z)$ là **phân rã không mất mát thông tin (Lossless-Join Decomposition)** khi và chỉ khi:
> $$X \twoheadrightarrow Y \quad \text{hoặc} \quad X \twoheadrightarrow Z$$

#### Chứng minh Áp dụng cho Đồ án:
Ta phân rã quan hệ $R(MaHocLieu, MaTacGia, MaChuDe)$ thành 2 quan hệ độc lập:
* $R_1(MaHocLieu, MaTacGia)$ &mdash; Đặt tên là `HocLieu_TacGia`
* $R_2(MaHocLieu, MaChuDe)$ &mdash; Đặt tên là `HocLieu_ChuDe`

1. **Kiểm tra điều kiện Fagin:**
   * Tập thuộc tính chung: $R_1 \cap R_2 = \{MaHocLieu\} = X$.
   * Do đã chứng minh tồn tại $MaHocLieu \twoheadrightarrow MaTacGia$ (tức $X \twoheadrightarrow Y$), theo Định lý Fagin, phép kết tự nhiên giữa $R_1$ và $R_2$ trên thuộc tính $MaHocLieu$ **chắc chắn bảo toàn thông tin 100%**.
2. **Đánh giá sau khi phân rã:**
   * Bảng `HocLieu_TacGia`: Chỉ cần 2 dòng `(HL01, TG01)`, `(HL01, TG02)`.
   * Bảng `HocLieu_ChuDe`: Chỉ cần 2 dòng `(HL01, CD02)`, `(HL01, CD03)`.
   * Tổng số dòng lưu trữ giảm từ $N \times M = 4$ xuống $N + M = 4$ dòng (Nếu $N=5, M=10$, giảm từ **50 dòng** xuống chỉ còn **15 dòng**).
   * Khóa chính của mỗi bảng: $PK(R_1) = (MaHocLieu, MaTacGia)$, $PK(R_2) = (MaHocLieu, MaChuDe)$.

### 5. Khôi phục Thông tin Không Mất Mát (Lossless-Join Verification)
Khi cần tái lập lại bảng phẳng ban đầu, ta thực hiện phép nối tự nhiên:
$$R = R_1 \bowtie_{MaHocLieu} R_2$$

```sql
SELECT r1.MaHocLieu, r1.MaTacGia, r2.MaChuDe
FROM HocLieu_TacGia r1
JOIN HocLieu_ChuDe r2 ON r1.MaHocLieu = r2.MaHocLieu
WHERE r1.MaHocLieu = 'HL01';
```

* **Kết quả:** Phép nối khôi phục chính xác 4 dòng ban đầu mà **hoàn toàn không phát sinh dòng dữ liệu giả (No Spurious Tuples)** và không bị thất thoát thông tin.
* **Kết luận khoa học:** Phân rã đã đưa cơ sở dữ liệu đạt chuẩn **4NF (Fourth Normal Form)** hoàn hảo, giải quyết triệt để vấn đề phụ thuộc đa trị của Đề tài 3.

---

## PHẦN VI: CHUẨN HÓA BẬC CAO: PHỤ THUỘC HÀM, BAO ĐÓNG, KHÓA, PHỦ TỐI THIỂU & 3NF/BCNF

> **Yêu cầu đề cương môn học:** *"Xác định phụ thuộc hàm từ quy tắc nghiệp vụ; trình bày bao đóng, khóa, phủ tối thiểu trên một lược đồ tiêu biểu; giải thích lựa chọn 3NF/BCNF, tính nối không mất mát và bảo toàn phụ thuộc."*

Để minh chứng đầy đủ toàn bộ quy trình chuẩn hóa từ 1NF $\rightarrow$ BCNF $\rightarrow$ 4NF, ta xét **Lược đồ quan hệ tiêu biểu quản lý Luồng Mượn Trả & Bạn Đọc** trước khi chuẩn hóa:
$$R(\underline{MaPhieu}, MaBanDoc, HoTen, Email, MaBanSao, MaHocLieu, NgayMuon, NgayHanTra)$$

### 1. Xác định Tập Phụ thuộc Hàm (Functional Dependencies - $F$)
Từ các quy tắc thế giới thực đã khảo sát:
* Mỗi phiếu mượn cấp phát cho 1 bạn đọc cụ thể mượn 1 cuốn sách vật lý xác định với ngày mượn và hạn trả cụ thể:
  $$f_1: MaPhieu \rightarrow \{MaBanDoc, MaBanSao, NgayMuon, NgayHanTra\}$$
* Mã bạn đọc xác định duy nhất họ tên và thư điện tử của bạn đọc đó:
  $$f_2: MaBanDoc \rightarrow \{HoTen, Email\}$$
* Mỗi địa chỉ thư điện tử đăng ký duy nhất cho 1 tài khoản bạn đọc:
  $$f_3: Email \rightarrow \{MaBanDoc, HoTen\}$$
* Mỗi bản sao vật lý trên kệ gắn liền với đúng 1 đầu mục học liệu gốc:
  $$f_4: MaBanSao \rightarrow \{MaHocLieu\}$$

Tập phụ thuộc hàm ban đầu:
$$F = \{ f_1: MaPhieu \rightarrow MaBanDoc, MaBanSao, NgayMuon, NgayHanTra; \quad f_2: MaBanDoc \rightarrow HoTen, Email; \quad f_3: Email \rightarrow MaBanDoc, HoTen; \quad f_4: MaBanSao \rightarrow MaHocLieu \}$$

---

### 2. Thuật toán Tính Bao Đóng Thuộc Tính ($X^+$) & Xác Định Khóa Ứng Viên
Để tìm tất cả các khóa của quan hệ $R$, ta phân loại các thuộc tính:
* **Tập nguồn $L$** (chỉ xuất hiện ở vế trái của $F$ mà không có ở vế phải): $L = \{MaPhieu\}$.
  $\implies MaPhieu$ **bắt buộc phải xuất hiện trong mọi khóa ứng viên**.
* **Tập đích $R_{attr}$** (chỉ xuất hiện ở vế phải): $R_{attr} = \{NgayMuon, NgayHanTra, MaHocLieu\}$.
* **Tập trung gian $M$** (xuất hiện ở cả hai vế): $M = \{MaBanDoc, MaBanSao, HoTen, Email\}$.

#### Tính Bao Đóng $(MaPhieu)^+$:
1. Khởi tạo: $X^{(0)} = \{MaPhieu\}$.
2. Áp dụng $f_1$: Do $\{MaPhieu\} \subseteq X^{(0)} \implies X^{(1)} = \{MaPhieu, MaBanDoc, MaBanSao, NgayMuon, NgayHanTra\}$.
3. Áp dụng $f_2$: Do $\{MaBanDoc\} \subseteq X^{(1)} \implies X^{(2)} = \{MaPhieu, MaBanDoc, MaBanSao, NgayMuon, NgayHanTra, HoTen, Email\}$.
4. Áp dụng $f_4$: Do $\{MaBanSao\} \subseteq X^{(2)} \implies X^{(3)} = \{MaPhieu, MaBanDoc, MaBanSao, NgayMuon, NgayHanTra, HoTen, Email, MaHocLieu\} = R^+$.
5. Dừng vì $X^{(3)}$ chứa toàn bộ thuộc tính của lược đồ $R$.

**Kết luận về Khóa:**
Do $(MaPhieu)^+ = R$ và không có bất kỳ tập con thực sự nào của $\{MaPhieu\}$ có bao đóng bằng $R$, nên $MaPhieu$ là **Siêu khóa tối thiểu duy nhất**, tức là **Khóa chính (Primary Key - PK)** duy nhất của lược đồ $R$.

---

### 3. Tìm Phủ Tối Thiểu (Minimal Cover / Canonical Cover - $F_{min}$)
Quy trình tìm phủ tối thiểu trải qua 3 bước:

#### Bước 1: Đưa tất cả vế phải về dạng thuộc tính đơn
$$F_1 = \{
MaPhieu \rightarrow MaBanDoc, \quad MaPhieu \rightarrow MaBanSao, \quad MaPhieu \rightarrow NgayMuon, \quad MaPhieu \rightarrow NgayHanTra,
$$
$$
MaBanDoc \rightarrow HoTen, \quad MaBanDoc \rightarrow Email, \quad Email \rightarrow MaBanDoc, \quad Email \rightarrow HoTen, \quad MaBanSao \rightarrow MaHocLieu
\}$$

#### Bước 2: Loại bỏ thuộc tính dư thừa ở vế trái
Do tất cả các phụ thuộc hàm trong $F_1$ đều có vế trái là thuộc tính đơn ($MaPhieu$, $MaBanDoc$, $Email$, $MaBanSao$), nên **không có thuộc tính dư thừa ở vế trái**.

#### Bước 3: Loại bỏ phụ thuộc hàm dư thừa
Xét phụ thuộc hàm $Email \rightarrow HoTen$:
* Xét tập phụ thuộc $F' = F_1 \setminus \{Email \rightarrow HoTen\}$.
* Tính bao đóng $(Email)^+$ trên tập $F'$:
  * Khởi tạo: $(Email)^+ = \{Email\}$.
  * Áp dụng $Email \rightarrow MaBanDoc \implies (Email)^+ = \{Email, MaBanDoc\}$.
  * Áp dụng $MaBanDoc \rightarrow HoTen \implies (Email)^+ = \{Email, MaBanDoc, HoTen\}$.
  * Do $HoTen \in (Email)^+$, phụ thuộc hàm $Email \rightarrow HoTen$ là **dư thừa (suy diễn gián tiếp qua tính bắc cầu)**. Ta loại bỏ nó.
* Kiểm tra tương tự cho tất cả các phụ thuộc hàm còn lại: Không có phụ thuộc hàm nào khác dư thừa.

**Tập Phủ Tối Thiểu Thu Được:**
$$F_{min} = \{
MaPhieu \rightarrow MaBanDoc, \quad MaPhieu \rightarrow MaBanSao, \quad MaPhieu \rightarrow NgayMuon, \quad MaPhieu \rightarrow NgayHanTra,
$$
$$
MaBanDoc \rightarrow HoTen, \quad MaBanDoc \rightarrow Email, \quad Email \rightarrow MaBanDoc, \quad MaBanSao \rightarrow MaHocLieu
\}$$

---

### 4. Kiểm Tra Dạng Chuẩn & Phân Rã về BCNF / 3NF
* **Khóa chính của $R$:** $MaPhieu$.
* **Thuộc tính khóa (Prime attribute):** $\{MaPhieu\}$.
* **Thuộc tính không khóa (Non-prime attributes):** $\{MaBanDoc, HoTen, Email, MaBanSao, MaHocLieu, NgayMuon, NgayHanTra\}$.

#### Đánh giá vi phạm chuẩn:
1. Xét $f_2: MaBanDoc \rightarrow \{HoTen, Email\}$:
   * $MaBanDoc$ không phải là siêu khóa của $R$.
   * $\{HoTen, Email\}$ không phải là thuộc tính khóa.
   $\implies$ Tồn tại phụ thuộc bắc cầu $MaPhieu \rightarrow MaBanDoc \rightarrow \{HoTen, Email\}$. **Vi phạm cả 3NF lẫn BCNF!**
2. Xét $f_4: MaBanSao \rightarrow \{MaHocLieu\}$:
   * $MaBanSao$ không phải là siêu khóa của $R$.
   * $MaHocLieu$ không phải thuộc tính khóa.
   $\implies$ Tồn tại phụ thuộc bắc cầu $MaPhieu \rightarrow MaBanSao \rightarrow MaHocLieu$. **Vi phạm cả 3NF lẫn BCNF!**

#### Thuật toán Phân rã Đạt Chuẩn BCNF:
Để loại bỏ triệt để các phụ thuộc bắc cầu và dư thừa dữ liệu, áp dụng thuật toán phân rã BCNF:
1. Dựa trên $f_2: MaBanDoc \rightarrow \{HoTen, Email\}$, tách thành:
   $$R_{BanDoc}(\underline{MaBanDoc}, HoTen, \underline{Email}) \quad \text{với } F_2 = \{MaBanDoc \rightarrow HoTen, Email; \quad Email \rightarrow MaBanDoc\}$$
2. Dựa trên $f_4: MaBanSao \rightarrow \{MaHocLieu\}$, tách thành:
   $$R_{BanSao}(\underline{MaBanSao}, MaHocLieu) \quad \text{với } F_3 = \{MaBanSao \rightarrow MaHocLieu\}$$
3. Phần còn lại của lược đồ ban đầu:
   $$R_{PhieuMuon}(\underline{MaPhieu}, MaBanDoc, MaBanSao, NgayMuon, NgayHanTra) \quad \text{với } F_1 = \{MaPhieu \rightarrow MaBanDoc, MaBanSao, NgayMuon, NgayHanTra\}$$

---

### 5. Chứng Minh Tính Nối Không Mất Mát (Lossless-Join) & Bảo Toàn Phụ Thuộc (Dependency Preservation)

#### A. Chứng minh Nối Không Mất Mát (Lossless Join):
Theo Định lý phân rã quan hệ, phép tách $R$ thành $R_A$ và $R_B$ là không mất mát nếu $(R_A \cap R_B) \rightarrow R_A$ hoặc $(R_A \cap R_B) \rightarrow R_B$:
* **Bước 1:** Xét $R_{PhieuMuon}$ và $R_{BanDoc}$:
  $$R_{PhieuMuon} \cap R_{BanDoc} = \{MaBanDoc\}$$
  Do $MaBanDoc$ là khóa chính của $R_{BanDoc}$, ta có $MaBanDoc \rightarrow R_{BanDoc}$.
  $\implies$ Phép nối giữa $R_{PhieuMuon}$ và $R_{BanDoc}$ **không mất mát thông tin**.
* **Bước 2:** Xét $(R_{PhieuMuon} \cup R_{BanDoc})$ và $R_{BanSao}$:
  $$(R_{PhieuMuon} \cup R_{BanDoc}) \cap R_{BanSao} = \{MaBanSao\}$$
  Do $MaBanSao$ là khóa chính của $R_{BanSao}$, ta có $MaBanSao \rightarrow R_{BanSao}$.
  $\implies$ Phép nối với $R_{BanSao}$ **không mất mát thông tin**.
* $\implies$ **Toàn bộ phép phân rã bảo toàn thông tin 100% (Lossless-Join)**.

#### B. Chứng minh Bảo Toàn Phụ Thuộc (Dependency Preservation):
* Phụ thuộc $f_1 \in F_{PhieuMuon}$.
* Phụ thuộc $f_2, f_3 \in F_{BanDoc}$.
* Phụ thuộc $f_4 \in F_{BanSao}$.
* Tập hợp: $(F_{PhieuMuon} \cup F_{BanDoc} \cup F_{BanSao}) \equiv F_{min}$.
* $\implies$ Mọi phụ thuộc hàm trong phủ tối thiểu đều được kiểm tra độc lập trên từng quan hệ phân rã mà **hoàn toàn không cần thực hiện phép JOIN**. Hệ thống đạt **Bảo toàn Phụ thuộc tuyệt đối (Dependency Preservation)**.

#### C. Kết luận Tổng thể:
Lược đồ CSDL triển khai thực tế của dự án (`PhieuMuon`, `BanDoc`, `BanSao`) được chứng minh toán học đạt chuẩn **BCNF (Boyce-Codd Normal Form)**, sau đó tiếp tục được phân rã đạt chuẩn **4NF (Fourth Normal Form)** đối với các thuộc tính đa trị Tác giả và Chủ đề. Toàn bộ quy trình chuẩn hóa đã thỏa mãn trọn vẹn 100% chuẩn đầu ra của đề cương môn học!

