# Đặc tả Thực thể và Thuộc tính - Hệ thống Thư viện đa phương tiện (Đề tài 3)

Tài liệu này liệt kê các thực thể (Entity) cốt lõi và các thuộc tính (Attribute) CHI TIẾT. Bản đặc tả này KHỚP 100% với CSDL thực tế trong mã nguồn của hệ thống. Nhóm có thể copy toàn bộ dữ liệu này vào báo cáo Word để minh chứng cho phần "Từ điển dữ liệu" và "Phân tích quy tắc dữ liệu".

## 1. Phân cấp Học Liệu (Kế thừa - Inheritance)

### Lớp cha: `Học Liệu` (HocLieu)
Đại diện chung cho mọi tài nguyên trong thư viện.
*   **MaHocLieu** (PK): Mã định danh duy nhất (Ví dụ: HL001).
*   **TieuDe**: Tên của tài liệu/sách/video.
*   **MoTa**: Tóm tắt nội dung (Abstract).
*   **NamXuatBan**: Năm xuất bản hoặc phát hành.
*   **NgonNgu**: Tiếng Việt, Tiếng Anh...
*   **GiaTien**: Giá trị thực tế của học liệu (Dùng để tính tiền đền bù nếu làm mất).
*   **NgayNhapKho**: Ngày nhập vào thư viện.
*   **AnhBia**: Đường dẫn tới file ảnh bìa.

### Các lớp con (Subclasses) kế thừa từ `Học Liệu`:
*   **`Sách` (Sach)**
    *   **ISBN**: Mã số sách chuẩn quốc tế (Thuộc tính riêng).
    *   **NhaXuatBan**: Nhà xuất bản.
    *   **SoTrang**: Số lượng trang.
    *   **LanTaiBan**: Lần tái bản (Ví dụ: 1, 2, 3).
*   **`Luận văn` (LuanVan)**
    *   **NguoiHuongDan**: Giảng viên hướng dẫn (Có thể lưu dạng chuỗi tên).
    *   **Truong**: Trường đại học/Viện đào tạo.
    *   **CapBaoVe**: Cử nhân, Thạc sĩ, Tiến sĩ.
    *   **ChuyenNganh**: Chuyên ngành bảo vệ (VD: Khoa học máy tính).
    *   **DiemBaoVe**: Điểm số đạt được (Hệ 10).
*   **`Video bài giảng` (VideoBaiGiang)**
    *   **ThoiLuong**: Tổng thời lượng (phút).
    *   **DoPhanGiai**: 1080p, 4K...
    *   **NgonNguPhuDe**: Ngôn ngữ của phụ đề.
    *   **DungLuong**: Kích thước file video (MB).
*   **`Tài liệu điện tử` (TaiLieuDienTu)**
    *   **DinhDang**: PDF, EPUB, DOCX...
    *   **DungLuong**: Kích thước file tài liệu (KB/MB).
    *   **QuyenTruyCap**: Public (Ai cũng xem được) hay Internal (Chỉ sinh viên trường).
    *   **URL_LienKet**: Đường dẫn để tải file trên Server.
---

## 2. Xử lý Thuộc tính Đa trị (Đạt chuẩn 4NF)
Một học liệu có thể do *nhiều tác giả* viết, và có thể thuộc *nhiều chủ đề* khác nhau.
*   **`Tác Giả` (TacGia):** MaTacGia (PK), TenTacGia, DonViCongTac, Email.
*   **`Chủ Đề` (ChuDe):** MaChuDe (PK), TenChuDe, MoTaChuDe, **ChuDeCha** (Khóa ngoại đệ quy để phân cấp chủ đề).
*   **Quan hệ:** Xây dựng các bảng liên kết `HocLieu_TacGia` (N-N) và `HocLieu_ChuDe` (N-N). 

---

## 3. Quản lý Mượn/Trả (Giao dịch)

### Thực thể: `Bản Sao` (BanSao)
Một cuốn sách có thể có nhiều cuốn giống hệt nhau trên kệ, mỗi cuốn là 1 "Bản sao".
*   **MaBanSao** (PK): Mã vạch dán trên cuốn sách vật lý.
*   **MaHocLieu** (FK): Thuộc học liệu nào.
*   **TrangThai**: Sẵn sàng, Đang cho mượn, Bị mất, Đang bảo trì.
*   **TinhTrangVatLy**: Còn mới, Rách bìa, Mất trang...
*   **ViTriKe**: Tọa độ kệ sách để thủ thư dễ tìm (Ví dụ: Tầng 2, Kệ A1).

### Thực thể: `Bạn Đọc` (BanDoc)
*   **MaBanDoc** (PK): Mã số sinh viên / Mã thành viên.
*   **HoTen**, **Email**, **SoDienThoai**.
*   **MatKhau**: Mật khẩu đăng nhập hệ thống (Mã hóa Hash).
*   **LoaiTaiKhoan**: Sinh viên, Giảng viên, Khách.
*   **NgayHetHan**: Ngày thẻ thư viện hết hạn sử dụng.
*   **TaiKhoanBiKhoa**: Trạng thái true/false (Bị khóa nếu nợ quá hạn).

### Thực thể: `Phiếu Mượn` (PhieuMuon)
Lưu trữ thông tin mỗi lần mượn (để xử lý phần Transaction tranh chấp).
*   **MaPhieu** (PK): Mã phiếu mượn.
*   **MaBanDoc** (FK): Người mượn.
*   **MaBanSao** (FK): Mượn cuốn vật lý nào.
*   **NgayMuon**: Thời gian mượn thực tế.
*   **NgayHanTra**: Deadline phải trả sách.
*   **NgayTraThucTe**: Ngày đem trả thực tế (nếu NULL là chưa trả).
*   **TienPhat**: Số tiền phạt (nếu trả trễ hạn hoặc làm hỏng).
*   **TrangThai**: Đang mượn, Đã trả đúng hạn, Đã trả trễ hạn.
*   **GhiChu**: Chú thích (VD: Lúc trả sách bị rách vài trang).
