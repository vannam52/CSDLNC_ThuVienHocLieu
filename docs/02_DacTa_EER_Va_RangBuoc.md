# BẢN ĐẶC TẢ YÊU CẦU TOÀN DIỆN (COMPREHENSIVE MINIWORLD SPECIFICATION)
## HỆ THỐNG QUẢN LÝ THƯ VIỆN HỌC LIỆU ĐA PHƯƠNG TIỆN VÀ TÀI NGUYÊN SỐ
**Môn học:** Cơ sở dữ liệu Nâng cao  
**Đối tượng áp dụng:** Nhóm 5 thành viên (Mô hình hóa EER, Chuẩn hóa 4NF, Kế thừa OODBMS, Phân quyền bảo mật & Giao dịch đồng thời)

---

## PHẦN I: MÔ TẢ THẾ GIỚI THỰC TOÀN DIỆN (EXPANDED MINIWORLD)

Trường Đại học triển khai hệ thống **Thư viện Học liệu Đa phương tiện thế hệ mới**, tích hợp giữa quản lý kho tài liệu truyền thống (sách giấy, luận văn) và thư viện số hiện đại (video bài giảng, báo cáo khoa học, tệp tài nguyên số, bài giảng e-learning).

### 1. Phân cấp Học liệu & Cấu trúc Đệ quy
* **Học liệu chung (HocLieu):** Mỗi học liệu có một `Mã học liệu` duy nhất, `Tiêu đề`, `Năm xuất bản`, `Tóm tắt nội dung`, `Ngôn ngữ`, `Giá tiền đền bù` (khi mất sách), và `Cấp độ bảo mật` (*Công khai*, *Nội bộ trường*, *Tài liệu mật/Hạn chế*).
* **Cấu trúc phân cấp cha - con (Mối quan hệ đệ quy):** Một học liệu lớn (ví dụ: Bộ giáo trình, Khóa học bài giảng chuyên đề) có thể bao gồm nhiều học liệu thành phần con (các tập, các chương, các bài giảng chi tiết). Một học liệu con thuộc về tối đa một học liệu cha.
* **Các loại học liệu chuyên biệt (Kế thừa hoàn toàn từ Học liệu cha):**
  * **Sách (Sach):** Có mã quốc tế `ISBN` (duy nhất), `Nhà xuất bản`, `Số trang`, `Tập` và `Lần tái bản`.
  * **Luận văn (LuanVan):** Có `Tên người hướng dẫn`, `Trường đào tạo`, `Cấp bảo vệ` (*Thạc sĩ, Tiến sĩ*), `Chuyên ngành` và `Năm bảo vệ`.
  * **Bài báo khoa học (BaoKhoaHoc):** Có `Tên tạp chí/Hội thảo`, `Chỉ số DOI` (duy nhất), `Số phát hành (Volume/Issue)` và `Chỉ số ảnh hưởng (IF)`.
  * **Video bài giảng (VideoBaiGiang):** Có `Thời lượng` (phút), `Độ phân giải` (*1080p, 4K*), `Giảng viên giảng dạy` và `Nền tảng phát`.
* *Ràng buộc:* Mọi học liệu bắt buộc phải thuộc về đúng 1 trong các loại chuyên biệt trên (Disjoint, Total).

### 2. Tệp tài nguyên số (TepTaiNguyen - Digital Assets)
* Đối với tài liệu điện tử và đa phương tiện, một học liệu có thể đính kèm **nhiều tệp tài nguyên số** (ví dụ: một cuốn sách có file PDF toàn văn và file Audio sách nói; một bài giảng có file video MP4, slide trình chiếu PDF và mã nguồn ZIP đính kèm).
* Mỗi tệp tài nguyên có: `Mã tệp` (duy nhất), `Tên tệp`, `Định dạng tệp (MIME Type: PDF, MP4, MP3, ZIP)`, `Dung lượng (MB)`, `Đường dẫn lưu trữ (Cloud Storage / S3 URL)`, `Mã Hash MD5` (dùng để kiểm tra tính toàn vẹn của tệp) và `Ngày tải lên`.
* Mỗi tệp tài nguyên bắt buộc phải gắn liền với đúng 1 học liệu xác định.

### 3. Tác giả & Chủ đề (Chuẩn hóa 4NF)
* **Tác giả (TacGia):** Một học liệu có thể do nhiều tác giả đồng sáng tác, một tác giả có thể đóng góp cho nhiều học liệu. Mỗi tác giả có `Mã tác giả`, `Họ tên`, `Đơn vị công tác`, `Email`, và mã định danh nhà nghiên cứu `ORCID`. Khi tham gia sáng tác, tác giả có `Vai trò` cụ thể (*Chủ biên, Đồng tác giả, Người hiệu đính*).
* **Chủ đề (ChuDe):** Mỗi học liệu có thể gắn với nhiều chủ đề nghiên cứu. Mỗi chủ đề có `Mã chủ đề`, `Tên chủ đề` và `Mô tả`.

### 4. Bản sao vật lý trên giá sách (BanSao - Physical Copies)
* Đối với học liệu có bản cứng (Sách in, Luận văn nộp lưu chiểu, Đĩa DVD), thư viện quản lý từng bản sao vật lý trên kệ.
* Mỗi bản sao có: `Mã bản sao` (mã vạch dán trên gáy sách), `Vị trí kệ sách` (*Kho - Dãy - Kệ - Ngăn*), `Tình trạng vật lý` (*Mới, Cũ, Rách bìa*), và `Trạng thái sẵn sàng` (*Sẵn sàng, Đang cho mượn, Đang bảo trì, Bị mất*).

### 5. Phân cấp Bạn đọc & Tài khoản người dùng
* **Bạn đọc (BanDoc):** Có `Mã bạn đọc` (duy nhất), `Họ và tên` (gồm Họ đệm và Tên), `Email`, `Số điện thoại`, `Ngày cấp thẻ`, `Ngày hết hạn thẻ` và `Trạng thái thẻ` (*Hoạt động, Bị khóa*).
* **Phân cấp đối tượng bạn đọc:**
  * **Sinh viên (SinhVien):** Có `MSSV`, `Lớp sinh hoạt`, `Niên khóa`.
  * **Giảng viên (GiangVien):** Có `Mã GV`, `Học vị` (*Thạc sĩ, Tiến sĩ, PGS, GS*), `Khoa / Viện`.
  * **Nghiên cứu sinh (NghienCuuSinh):** Có `Mã NCS`, `Tên đề tài nghiên cứu`, `Giảng viên hướng dẫn`.
* *Ràng buộc:* Một người trong nội bộ trường chỉ thuộc 1 loại đối tượng. Hệ thống có cho phép cấp thẻ bạn đọc bên ngoài/vãng lai (Disjoint, Partial).

### 6. Quản lý Quyền truy cập tài nguyên số (QuyenTruyCap - DRM & Access Rights)
* Để bảo vệ bản quyền tài liệu số, hệ thống thiết lập bảng phân quyền truy cập giữa **Bạn đọc** và **Học liệu** (hoặc Tệp tài nguyên).
* Mỗi quyền truy cập lưu: `Mã bạn đọc`, `Mã học liệu`, `Loại quyền` (*Chỉ đọc trực tuyến - View Only*, *Được phép tải xuống - Download*, *Toàn quyền in ấn*), `Ngày cấp quyền`, `Ngày hết hạn quyền` và `Số lượt tải tối đa cho phép`.

### 7. Giao dịch Mượn / Trả bản sao vật lý (PhieuMuon)
* Khi bạn đọc đến mượn sách giấy, một `Phiếu mượn` được tạo ra: có `Mã phiếu`, gắn với 1 Bạn đọc và 1 Bản sao cụ thể.
* Ghi nhận: `Ngày mượn`, `Hạn trả`, `Ngày trả thực tế`, `Tiền phạt trễ hạn` (tự động tính dựa vào số ngày quá hạn), và `Trạng thái phiếu` (*Đang mượn, Đã trả, Quá hạn*).
* Cơ chế khóa đồng thời (**Pessimistic Locking `FOR UPDATE`**) được kích hoạt tại bước này để tránh tình trạng 2 sinh viên cùng mượn 1 bản sao duy nhất.

### 8. Nhật ký truy cập số (NhatKyTruyCap - Digital Access Log / Audit)
* Mỗi khi bạn đọc mở xem online hoặc tải xuống một tệp tài nguyên số, hệ thống tự động ghi nhật ký: `Mã nhật ký`, `Mã bạn đọc`, `Mã tệp`, `Thời điểm truy cập` (Timestamp), `Hành động thực hiện` (*Xem trực tuyến, Tải file*), `Địa chỉ IP` và `Thiết bị sử dụng` (*Web, Mobile*).
* Giúp thống kê tần suất khai thác học liệu, phát hiện hành vi tải tài liệu bất thường hàng loạt.

### 9. Đánh giá & Bình luận học liệu (DanhGia - Review & Rating)
* Bạn đọc sau khi mượn hoặc đọc xong có thể gửi đánh giá học liệu: `Điểm đánh giá` (từ 1 đến 5 sao), `Nội dung nhận xét` và `Thời điểm đánh giá`.

---

## PHẦN II: DANH SÁCH THỰC THỂ VÀ THUỘC TÍNH CHI TIẾT (CHUẨN EER)

| Thực thể (Entity) | Vai trò trong mô hình | Thuộc tính Khóa (PK - Gạch chân) | Thuộc tính thường | Thuộc tính phức hợp / Đa trị / Dẫn xuất |
| :--- | :--- | :--- | :--- | :--- |
| **HOCLIEU** | Superclass | <u>MaHocLieu</u> | TieuDe, NamXB, MoTa, NgonNgu, GiaTien, CapDoBaoMat | *(Có FK đệ quy trỏ đến MaHocLieu cha)* |
| **SACH** | Subclass của HOCLIEU | *(Kế thừa MaHocLieu)* | <u>ISBN</u> (Khóa phụ), NhaXuatBan, SoTrang, Tap, LanTaiBan | |
| **LUANVAN** | Subclass của HOCLIEU | *(Kế thừa MaHocLieu)* | NguoiHuongDan, Truong, CapBaoVe, ChuyenNganh, NamBaoVe | |
| **BAOKHOAHOC** | Subclass của HOCLIEU | *(Kế thừa MaHocLieu)* | <u>DOI</u> (Khóa phụ), TenTapChi, SoPhatHanh, ChiSoIF | |
| **VIDEOBAIGIANG** | Subclass của HOCLIEU | *(Kế thừa MaHocLieu)* | ThoiLuongPhut, DoPhanGiai, GiangVien, NenTang | |
| **TEPTAINGUYEN** | Thực thể mạnh | <u>MaTep</u> | TenTep, DinhDangMIME, DungLuongMB, DuongDanLuuTru, MaHashMD5, NgayTaiLen | |
| **TACGIA** | Thực thể mạnh | <u>MaTacGia</u> | TenTacGia, DonVi, Email, MaORCID | |
| **CHUDE** | Thực thể mạnh | <u>MaChuDe</u> | TenChuDe, MoTa | |
| **BANSAO** | Thực thể phụ thuộc (Weak) | <u>MaBanSao</u> | ViTriKe, TinhTrangVatLy, TrangThai | |
| **BANDOC** | Superclass | <u>MaBanDoc</u> | Email, SoDienThoai, NgayCapThe, NgayHetHan, TrangThaiThe | **HoTen** (gồm: HoDem + Ten) |
| **SINHVIEN** | Subclass của BANDOC | *(Kế thừa MaBanDoc)* | MSSV, Lop, NienKhoa | |
| **GIANGVIEN** | Subclass của BANDOC | *(Kế thừa MaBanDoc)* | MaGV, HocVi, Khoa | |
| **NGHIENCUUSINH** | Subclass của BANDOC | *(Kế thừa MaBanDoc)* | MaNCS, DeTaiNghienCuu, NguoiHuongDan | |
| **PHIEUMUON** | Thực thể giao tác | <u>MaPhieu</u> | NgayMuon, HanTra, NgayTraThucTe, TrangThai | **TienPhat** (Dẫn xuất từ ngày trễ hạn) |
| **QUYENTRUYCAP** | Thực thể liên kết / Phân quyền | *(MaBanDoc, MaHocLieu)* | LoaiQuyen, NgayCap, NgayHetHan, LuotTaiToiDa | |
| **NHATKYTRUYCAP** | Thực thể sự kiện (Audit Log) | <u>MaNhatKy</u> | ThoiDiem, HanhDong, DiaChiIP, ThietBi | |
| **DANHGIA** | Thực thể liên kết | *(MaBanDoc, MaHocLieu)* | DiemSo, BinhLuan, ThoiGianDanhGia | |

---

## PHẦN III: CÁC RÀNG BUỘC CHUYÊN BIỆT HÓA EER (SPECIALIZATION CONSTRAINTS)

### 1. Phân cấp Học liệu: `HOCLIEU` $\rightarrow$ `{SACH, LUANVAN, BAOKHOAHOC, VIDEOBAIGIANG}`
* **Loại phân cấp:** **Disjoint (Ký hiệu `d`)** & **Total Specialization (Ký hiệu đường nối đôi `═`)**.
* **Ý nghĩa:**
  * Không có học liệu nào vừa là Sách vừa là Video (`Disjoint`).
  * Mọi học liệu nhập vào hệ thống bắt buộc phải thuộc một định dạng cụ thể, không tồn tại học liệu chung chung (`Total`).

### 2. Phân cấp Bạn đọc: `BANDOC` $\rightarrow$ `{SINHVIEN, GIANGVIEN, NGHIENCUUSINH}`
* **Loại phân cấp:** **Disjoint (Ký hiệu `d`)** & **Partial Specialization (Ký hiệu đường nối đơn `─`)**.
* **Ý nghĩa:**
  * Trong phạm vi tài khoản nội bộ trường, một bạn đọc không thể vừa là Sinh viên vừa là Giảng viên (`Disjoint`).
  * Có những bạn đọc là khách bên ngoài, học viên ngắn hạn, cựu sinh viên (không thuộc 3 nhóm trên) nên việc tham gia phân cấp là từng phần (`Partial`).

---

## PHẦN IV: CÁC MỐI QUAN HỆ VÀ BẢN SỐ (RELATIONSHIPS & CARDINALITY)

| Tên quan hệ (Hình thoi) | Thực thể tham gia | Bản số (Cardinality) | Ràng buộc tham gia (Participation) | Thuộc tính trên quan hệ (nếu có) |
| :--- | :--- | :--- | :--- | :--- |
| **CHUA_CON** *(Đệ quy)* | `HOCLIEU` &mdash; `HOCLIEU` | **1 - N** | Cha: Từng phần ($0..N$)<br>Con: Từng phần ($0..1$) | Thứ tự chương/mục (`ThuTu`) |
| **DINH_KEM_TEP** | `HOCLIEU` &mdash; `TEPTAINGUYEN` | **1 - N** | HOCLIEU: Từng phần ($0..N$)<br>TEPTAINGUYEN: Toàn phần ($1..1$) | |
| **CO_BANSAO** | `HOCLIEU` &mdash; `BANSAO` | **1 - N** | HOCLIEU: Từng phần ($0..N$)<br>BANSAO: Toàn phần ($1..1$) | |
| **DONG_SANG_TAC** | `TACGIA` &mdash; `HOCLIEU` | **N - M** | TACGIA: Từng phần<br>HOCLIEU: Toàn phần ($1..N$) | `VaiTro` *(Chủ biên, Đồng tác giả)* |
| **PHAN_LOAI** | `HOCLIEU` &mdash; `CHUDE` | **N - M** | HOCLIEU: Toàn phần ($1..N$)<br>CHUDE: Từng phần | |
| **LAP_PHIEU_MUON** | `BANDOC` &mdash; `BANSAO` | Thông qua `PHIEUMUON` | BANDOC: Từng phần ($0..N$)<br>BANSAO: Từng phần ($0..N$) | Thực thể giao tác mượn/trả |
| **CAP_QUYEN** | `BANDOC` &mdash; `HOCLIEU` | Thông qua `QUYENTRUYCAP` | BANDOC: Từng phần<br>HOCLIEU: Từng phần | `LoaiQuyen`, `NgayHetHan`, `LuotTai` |
| **GHI_NHAT_KY** | `BANDOC` &mdash; `TEPTAINGUYEN` | Thông qua `NHATKYTRUYCAP` | BANDOC: Từng phần<br>TEPTAINGUYEN: Từng phần | Lịch sử truy cập số |
| **DANH_GIA_HOCLIEU** | `BANDOC` &mdash; `HOCLIEU` | Thông qua `DANHGIA` | BANDOC: Từng phần<br>HOCLIEU: Từng phần | `DiemSo`, `BinhLuan` |

---

## PHẦN V: VÌ SAO BẢN ĐẶC TẢ NÀY GIÚP NHÓM ĐẠT ĐIỂM TỐI ĐA (ĐIỂM A/A+)?

1. **Khớp trọn vẹn 100% gợi ý đề bài:** Có đầy đủ: *Học liệu, Tác giả, Chủ đề, Bản sao, Tệp tài nguyên, Bạn đọc, Phiếu mượn và Quyền truy cập học liệu*.
2. **Có tính kế thừa EER chuẩn mực:** 2 phân cấp cha - con rõ ràng (`HocLieu` và `BanDoc`), thể hiện rõ ràng buộc `{d, total}` và `{d, partial}`.
3. **Có mối quan hệ Đệ quy (Self-relationship):** Học liệu cha chứa học liệu con, phục vụ xuất sắc cho câu lệnh **Recursive CTE (WITH RECURSIVE)** trong bài tập lớn.
4. **Có giải quyết bài toán giao dịch đồng thời:** Thực thể `BanSao` + `PhieuMuon` với kịch bản tranh chấp khóa **Pessimistic Locking**.
5. **Có mô hình hóa An toàn thông tin & Dữ liệu lớn:** `QuyenTruyCap` (phân quyền DRM) và `NhatKyTruyCap` (Audit Log truy vết hành vi người dùng).

---

## PHẦN VI: MINH CHỨNG PHỤ THUỘC ĐA TRỊ & PHÂN RÃ CHUẨN 4NF (THEO ĐỀ CƯƠNG)

### 1. Phân tích Phụ thuộc Đa trị (Multivalued Dependency - MVD)
* Giả sử ta gom chung tất cả thuộc tính đa trị vào 1 bảng duy nhất: `HocLieu_ChuaChuanHoa(MaHocLieu, TacGia, ChuDe)`.
* **Giả định nghiệp vụ:** Tập tác giả và tập chủ đề là **hoàn toàn độc lập** với nhau. Một cuốn sách có nhiều tác giả đồng sáng tác và cuốn sách đó có thể phân loại vào nhiều chủ đề nghiên cứu. Tác giả A không chỉ định hay giới hạn chủ đề nào.
* **Tồn tại 2 phụ thuộc đa trị (MVD):**
  $$MaHocLieu \twoheadrightarrow TacGia$$
  $$MaHocLieu \twoheadrightarrow ChuDe$$

### 2. Ví dụ Dữ liệu chứng minh dư thừa trước phân rã 4NF (Tích Descartes)
Xét cuốn sách `HL01` có 2 tác giả (*"Silberschatz"*, *"Elmasri"*) và 2 chủ đề (*"Cơ sở dữ liệu"*, *"Lập trình"*). Bảng trước phân rã bắt buộc phải lưu $2 \times 2 = 4$ bản ghi lặp lại:

| MaHocLieu | TacGia | ChuDe | Nhận xét bất thường (Anomaly) |
| :--- | :--- | :--- | :--- |
| **HL01** | Silberschatz | Cơ sở dữ liệu | Cặp (Tác giả, Chủ đề) bị nhân tích Descartes |
| **HL01** | Silberschatz | Lập trình | Dư thừa thông tin tác giả |
| **HL01** | Elmasri | Cơ sở dữ liệu | Dư thừa thông tin chủ đề |
| **HL01** | Elmasri | Lập trình | Nếu thêm 1 chủ đề mới, bắt buộc phải chèn thêm 2 dòng mới cho cả 2 tác giả |

*Hậu quả:* Gây dị thường thêm/xóa/sửa (Insertion/Deletion/Update Anomaly) và lãng phí dung lượng lưu trữ nghiêm trọng.

### 3. Phân rã về Dạng Chuẩn 4 (4NF)
Để đạt chuẩn 4NF, ta phân rã bảng trên thành 2 quan hệ độc lập:
1. **$R_1$ (`HocLieu_TacGia`):** `(MaHocLieu, MaTacGia)` &mdash; Lưu danh sách tác giả của từng học liệu.
2. **$R_2$ (`HocLieu_ChuDe`):** `(MaHocLieu, MaChuDe)` &mdash; Lưu danh sách chủ đề của từng học liệu.

*Dữ liệu sau phân rã:*
* Bảng $R_1$: `(HL01, Silberschatz)`, `(HL01, Elmasri)` &rarr; Chỉ mất 2 dòng.
* Bảng $R_2$: `(HL01, CSDL)`, `(HL01, LapTrinh)` &rarr; Chỉ mất 2 dòng.
* $\rightarrow$ **Triệt tiêu hoàn toàn sự dư thừa** từ cấp số nhân $N \times M$ về phép cộng $N + M$.

### 4. Kết quả Phép nối khôi phục đúng thông tin (Lossless-Join)
Khi cần hiển thị lại toàn bộ thông tin ban đầu, ta thực hiện phép nối tự nhiên:
$$R = R_1 \bowtie_{MaHocLieu} R_2$$

```sql
SELECT r1.MaHocLieu, r1.MaTacGia, r2.MaChuDe
FROM HocLieu_TacGia r1
JOIN HocLieu_ChuDe r2 ON r1.MaHocLieu = r2.MaHocLieu
WHERE r1.MaHocLieu = 'HL01';
```
*Kết quả phép nối khôi phục chính xác 100% các dòng dữ liệu ban đầu mà **không làm phát sinh bất kỳ dòng dữ liệu giả nào (No Spurious Tuples)**. Điều này chứng minh quá trình phân rã đạt chuẩn 4NF và bảo toàn thông tin tuyệt đối.*

