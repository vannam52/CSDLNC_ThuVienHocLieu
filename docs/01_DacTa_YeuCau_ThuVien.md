# BẢN ĐẶC TẢ YÊU CẦU & TỪ ĐIỂN DỮ LIỆU THƯ VIỆN HỌC LIỆU ĐA PHƯƠNG TIỆN (ĐỀ TÀI 3)

Tài liệu này cung cấp bản đặc tả chuẩn xác, đầy đủ và **khớp 100% với mã nguồn triển khai thực tế** của hệ thống (bao gồm CSDL PostgreSQL `library_management` và CSDL Hướng đối tượng ObjectDB `thuvien.odb`).

---

## I. Phân Cấp Học Liệu & Kế Thừa (Class Table Inheritance)

Hệ thống thư viện áp dụng chiến lược kế thừa **Class Table Inheritance (Joined Table)**: Bảng cha `HocLieu` chứa các thuộc tính dùng chung, các bảng con (`Sach`, `LuanVan`, `VideoBaiGiang`, `TaiLieuDienTu`) liên kết qua khóa chính đồng thời là khóa ngoại `MaHocLieu REFERENCES HocLieu(MaHocLieu) ON DELETE CASCADE`.

### 1. Bảng cha: `HocLieu` (Lớp cơ sở)
Đại diện chung cho mọi đầu mục tài nguyên được quản lý trong thư viện.

| Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Giá Trị Mặc Định | Ý Nghĩa / Mô Tả Nghiệp Vụ |
| :--- | :--- | :--- | :--- | :--- |
| **MaHocLieu** | `VARCHAR(20)` | **PRIMARY KEY** | *Không* | Mã định danh duy nhất (VD: `HL01`, `HL02`) |
| **TieuDe** | `VARCHAR(255)` | **NOT NULL** | *Không* | Nhan đề / Tên học liệu |
| **MoTa** | `TEXT` | *Nullable* | `NULL` | Tóm tắt nội dung học liệu (Abstract) |
| **NamXuatBan** | `INT` | `CHECK (NamXuatBan > 1900)` | *Không* | Năm xuất bản hoặc phát hành |
| **NgonNgu** | `VARCHAR(50)` | *Nullable* | *Không* | Ngôn ngữ chính (Tiếng Việt, Tiếng Anh...) |
| **GiaTien** | `DECIMAL(12, 2)` | `CHECK (GiaTien >= 0)` | `0.00` | Giá bìa / Giá trị đền bù nếu làm mất sách |
| **NgayNhapKho** | `DATE` | *Nullable* | `CURRENT_DATE` | Ngày nhập học liệu vào hệ thống |
| **AnhBia** | `VARCHAR(255)` | *Nullable* | `NULL` | Đường dẫn file ảnh bìa hoặc thumbnail |

### 2. Các bảng con (Subclasses)

#### 2.1. Bảng `Sach` (Kế thừa từ HocLieu)
| Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Mô Tả |
| :--- | :--- | :--- | :--- |
| **MaHocLieu** | `VARCHAR(20)` | **PK, FK** $\rightarrow$ `HocLieu(MaHocLieu)` | Khóa chính kế thừa |
| **ISBN** | `VARCHAR(50)` | **UNIQUE NOT NULL** | Mã chuẩn quốc tế của sách |
| **NhaXuatBan** | `VARCHAR(100)` | *Nullable* | Nhà xuất bản phát hành sách |
| **SoTrang** | `INT` | `CHECK (SoTrang > 0)` | Tổng số trang sách |
| **LanTaiBan** | `INT` | `CHECK (LanTaiBan >= 1)` | Lần tái bản (1, 2, 3...) |

#### 2.2. Bảng `LuanVan` (Kế thừa từ HocLieu)
| Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Mô Tả |
| :--- | :--- | :--- | :--- |
| **MaHocLieu** | `VARCHAR(20)` | **PK, FK** $\rightarrow$ `HocLieu(MaHocLieu)` | Khóa chính kế thừa |
| **NguoiHuongDan** | `VARCHAR(255)` | *Nullable* | Học hàm, học vị & họ tên giảng viên hướng dẫn |
| **Truong** | `VARCHAR(150)` | *Nullable* | Tên trường đại học / Viện đào tạo |
| **CapBaoVe** | `VARCHAR(50)` | *Nullable* | Cử nhân, Thạc sĩ, Tiến sĩ |
| **ChuyenNganh** | `VARCHAR(100)` | *Nullable* | Chuyên ngành bảo vệ (Khoa học máy tính, CNPM...) |
| **DiemBaoVe** | `DECIMAL(4, 2)` | `CHECK (DiemBaoVe BETWEEN 0 AND 10)` | Điểm chấm bảo vệ của Hội đồng (Thang 10) |

#### 2.3. Bảng `VideoBaiGiang` (Kế thừa từ HocLieu)
| Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Mô Tả |
| :--- | :--- | :--- | :--- |
| **MaHocLieu** | `VARCHAR(20)` | **PK, FK** $\rightarrow$ `HocLieu(MaHocLieu)` | Khóa chính kế thừa |
| **ThoiLuong** | `INT` | `CHECK (ThoiLuong > 0)` | Thời lượng video (tính theo phút) |
| **DoPhanGiai** | `VARCHAR(20)` | *Nullable* | Độ phân giải hiển thị (`720p`, `1080p`, `4K`) |
| **NgonNguPhuDe** | `VARCHAR(50)` | *Nullable* | Ngôn ngữ phụ đề đính kèm |
| **DungLuong** | `DECIMAL(8, 2)` | `CHECK (DungLuong > 0)` | Dung lượng tệp video (MB) |

#### 2.4. Bảng `TaiLieuDienTu` (Kế thừa từ HocLieu)
| Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Mô Tả |
| :--- | :--- | :--- | :--- |
| **MaHocLieu** | `VARCHAR(20)` | **PK, FK** $\rightarrow$ `HocLieu(MaHocLieu)` | Khóa chính kế thừa |
| **DinhDang** | `VARCHAR(20)` | *Nullable* | Định dạng file (`PDF`, `EPUB`, `DOCX`, `ZIP`) |
| **DungLuong** | `DECIMAL(8, 2)` | `CHECK (DungLuong > 0)` | Kích thước file tài liệu (MB) |
| **QuyenTruyCap** | `VARCHAR(50)` | *Nullable* | Cấp phép truy cập (`Public`, `Noi bo`, `Han che`) |
| **URL_LienKet** | `TEXT` | *Nullable* | Đường dẫn liên kết tải file hoặc xem online |

---

## II. Xử Lý Thuộc Tính Đa Trị & Chuẩn Hóa 4NF

Một học liệu có thể do nhiều tác giả đồng sáng tác, và có thể thuộc về nhiều chủ đề nghiên cứu độc lập. Để triệt tiêu tích Đề-các (Cartesian product) và đạt Chuẩn 4NF, hai mối quan hệ này được chuẩn hóa thành các bảng trung gian N-N.

### 1. Bảng `TacGia` & `HocLieu_TacGia`
* **Bảng `TacGia`:**
  | Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Mô Tả |
  | :--- | :--- | :--- | :--- |
  | **MaTacGia** | `VARCHAR(20)` | **PRIMARY KEY** | Mã tác giả (VD: `TG01`, `TG02`) |
  | **TenTacGia** | `VARCHAR(100)` | **NOT NULL** | Họ và tên tác giả |
  | **DonViCongTac** | `VARCHAR(150)` | *Nullable* | Trường ĐH, Viện nghiên cứu, Tổ chức |
  | **Email** | `VARCHAR(100)` | *Nullable* | Địa chỉ thư điện tử |

* **Bảng liên kết `HocLieu_TacGia` (N:M):**
  | Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Mô Tả |
  | :--- | :--- | :--- | :--- |
  | **MaHocLieu** | `VARCHAR(20)` | **PK, FK** $\rightarrow$ `HocLieu(MaHocLieu)` | `ON DELETE CASCADE` |
  | **MaTacGia** | `VARCHAR(20)` | **PK, FK** $\rightarrow$ `TacGia(MaTacGia)` | `ON DELETE CASCADE` |

### 2. Bảng `ChuDe` (Cây Đệ Quy) & `HocLieu_ChuDe`
* **Bảng `ChuDe`:** Có quan hệ tự tham chiếu đệ quy 1:N thông qua `ChuDeCha`.
  | Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Mô Tả |
  | :--- | :--- | :--- | :--- |
  | **MaChuDe** | `VARCHAR(20)` | **PRIMARY KEY** | Mã chủ đề (VD: `CD01`, `CD02`) |
  | **TenChuDe** | `VARCHAR(100)` | **NOT NULL** | Tên chủ đề (CNTT, CSDL, CSDL Nâng cao) |
  | **MoTaChuDe** | `TEXT` | *Nullable* | Mô tả chi tiết phạm vi chủ đề |
  | **ChuDeCha** | `VARCHAR(20)` | **FK** $\rightarrow$ `ChuDe(MaChuDe)` | `ON DELETE SET NULL` (Đệ quy phân cấp) |

* **Bảng liên kết `HocLieu_ChuDe` (N:M):**
  | Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Mô Tả |
  | :--- | :--- | :--- | :--- |
  | **MaHocLieu** | `VARCHAR(20)` | **PK, FK** $\rightarrow$ `HocLieu(MaHocLieu)` | `ON DELETE CASCADE` |
  | **MaChuDe** | `VARCHAR(20)` | **PK, FK** $\rightarrow$ `ChuDe(MaChuDe)` | `ON DELETE CASCADE` |

---

## III. Quản Lý Bản Sao Vật Lý & Giao Dịch Mượn Trả

### 1. Bảng `BanSao` (Bản sao vật lý trên kệ sách)
Mỗi học liệu vật lý (Sách, Luận văn) có thể có nhiều bản in thực tế trên kệ.
| Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Mặc Định | Mô Tả |
| :--- | :--- | :--- | :--- | :--- |
| **MaBanSao** | `VARCHAR(20)` | **PRIMARY KEY** | *Không* | Mã vạch dán trên gáy sách (VD: `BS01`, `BS02`) |
| **MaHocLieu** | `VARCHAR(20)` | **FK** $\rightarrow$ `HocLieu` | *Không* | Thuộc học liệu nào (`ON DELETE CASCADE`) |
| **TrangThai** | `VARCHAR(50)` | `CHECK (TrangThai IN ('San sang', 'Dang muon', 'Bao tri', 'Bi mat'))` | `'San sang'` | Trạng thái vật lý sẵn sàng cho mượn |
| **TinhTrangVatLy** | `VARCHAR(100)` | *Nullable* | `'Mới'` | Hiện trạng (Mới, Rách bìa, Mất trang...) |
| **ViTriKe** | `VARCHAR(100)` | *Nullable* | *Không* | Tọa độ lưu kho (VD: Kệ A1-01, Tầng 2) |

### 2. Bảng `BanDoc` (Độc giả thư viện)
| Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Mặc Định | Mô Tả |
| :--- | :--- | :--- | :--- | :--- |
| **MaBanDoc** | `VARCHAR(20)` | **PRIMARY KEY** | *Không* | Mã bạn đọc (MSSV/Mã thẻ) |
| **HoTen** | `VARCHAR(100)` | **NOT NULL** | *Không* | Họ và tên đầy đủ |
| **Email** | `VARCHAR(100)` | **UNIQUE** | *Không* | Thư điện tử định danh duy nhất |
| **SoDienThoai** | `VARCHAR(20)` | *Nullable* | *Không* | Số điện thoại liên hệ |
| **MatKhau** | `VARCHAR(255)` | **NOT NULL** | *Không* | Mật khẩu xác thực tài khoản |
| **LoaiTaiKhoan** | `VARCHAR(50)` | *Nullable* | `'Sinh vien'` | Sinh viên, Giảng viên, Nghiên cứu sinh |
| **NgayHetHan** | `DATE` | *Nullable* | *Không* | Ngày hết hạn hiệu lực của thẻ |
| **TaiKhoanBiKhoa** | `BOOLEAN` | *Not Null* | `FALSE` | Trạng thái khóa (TRUE nếu vi phạm mượn trả) |

### 3. Bảng `PhieuMuon` (Lưu vết giao dịch mượn/trả)
| Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Mặc Định | Mô Tả |
| :--- | :--- | :--- | :--- | :--- |
| **MaPhieu** | `VARCHAR(20)` | **PRIMARY KEY** | *Không* | Mã phiếu mượn (VD: `PM01`, `PM_PHIEN_1`) |
| **MaBanDoc** | `VARCHAR(20)` | **FK** $\rightarrow$ `BanDoc(MaBanDoc)` | *Không* | Người thực hiện mượn |
| **MaBanSao** | `VARCHAR(20)` | **FK** $\rightarrow$ `BanSao(MaBanSao)` | *Không* | Bản sao vật lý được mượn |
| **NgayMuon** | `TIMESTAMP` | *Nullable* | `CURRENT_TIMESTAMP` | Thời điểm lập phiếu |
| **NgayHanTra** | `TIMESTAMP` | *Nullable* | `+ 14 days` | Hạn chót phải trả sách |
| **NgayTraThucTe** | `TIMESTAMP` | *Nullable* | `NULL` | Thời điểm trả sách thực tế |
| **TienPhat** | `DECIMAL(12, 2)` | `CHECK (TienPhat >= 0)` | `0.00` | Tiền phạt tự động tính nếu trễ hạn |
| **TrangThai** | `VARCHAR(50)` | `CHECK (TrangThai IN ('Dang muon', 'Da tra'))` | `'Dang muon'` | Trạng thái hiện thời của phiếu |
| **GhiChu** | `TEXT` | *Nullable* | `NULL` | Nhật ký ghi nhận lý do trả trễ/phạt tiền |

---

## IV. Ràng Buộc Toàn Vẹn & Logic Nghiệp Vụ Tự Động (PL/pgSQL)

Bên cạnh các ràng buộc tĩnh ở cấp độ bảng (PK, FK, UNIQUE, CHECK), hệ thống áp dụng cơ chế bảo vệ dữ liệu đa tầng (**Defense-in-Depth**):

1. **Khóa Bi Quan (Pessimistic Concurrency Control):**
   * Trong thủ tục `sp_MuonSach` và `sp_TraSach`, sử dụng mệnh đề `SELECT ... FOR UPDATE` để khóa hàng độc quyền trên bản ghi `BanSao` hoặc `PhieuMuon`. Ngăn chặn hoàn toàn hiện tượng *Lost Update* và *Race Condition* khi có 2 phiên đồng thời cùng mượn 1 bản sao duy nhất.
2. **Trigger Kiểm Soát Nghiệp Vụ (`trg_KiemTraDieuKienMuon`):**
   * Kiểm tra tự động tại thời điểm `BEFORE INSERT`: Tài khoản không bị khóa, thẻ chưa hết hạn, số sách đang mượn $< 5$ cuốn và bản sao phải ở trạng thái `'San sang'`.
3. **Trigger Đồng Bộ Trạng Thái Bản Sao (`trg_CapNhatTrangThaiBanSao`):**
   * Khi tạo phiếu mượn thành công, trạng thái của `BanSao` tự động chuyển sang `'Dang muon'`.
   * Khi hoàn tất trả sách (`TrangThai = 'Da tra'`), `BanSao` tự động phục hồi về `'San sang'`.
4. **Trigger Tính Tiền Phạt Tự Động (`trg_TinhTienPhat`):**
   * Tự động ghi nhận `NgayTraThucTe = CURRENT_TIMESTAMP` nếu người thao tác chưa truyền vào.
   * Nếu `NgayTraThucTe > NgayHanTra`, tự động tính `TienPhat = SoNgayTre * 5000` VNĐ và ghi chú cụ thể vào cột `GhiChu`.
