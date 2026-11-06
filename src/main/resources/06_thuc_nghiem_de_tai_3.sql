-- ====================================================================
-- ĐỒ ÁN CSDL NÂNG CAO - ĐỀ TÀI 3: THƯ VIỆN HỌC LIỆU ĐA PHƯƠNG TIỆN
-- FILE: 06_thuc_nghiem_de_tai_3.sql
-- MỤC ĐÍCH: KỊCH BẢN THỰC NGHIỆM ĐỐI CHỨNG THEO YÊU CẦU ĐỀ CƯƠNG (TRANG 5)
--
-- Gồm 2 bài toán thực nghiệm bắt buộc khi báo cáo:
-- 1. THỰC NGHIỆM 4NF: Bảng dữ liệu mẫu lặp tích Descartes trước phân rã 
--    và truy vấn JOIN khôi phục đúng thông tin (Lossless-Join Decomposition).
-- 2. THỰC NGHIỆM SINGLE TABLE INHERITANCE: Thiết kế một bảng chung, 
--    đo đếm tỷ lệ cột NULL thực tế, đánh giá vi phạm ràng buộc toàn vẹn 
--    và so sánh với mô hình Joined Table đang triển khai.
-- ====================================================================

-- ####################################################################
-- PHẦN 1: THỰC NGHIỆM CHUẨN HÓA 4NF (BƯỚC 3 ĐỀ TÀI 3)
-- ####################################################################

-- 1.1. Tạo bảng phẳng chưa chuẩn hóa (Lưu trữ chung 2 thuộc tính đa trị)
DROP TABLE IF EXISTS HocLieu_ChuaChuanHoa_4NF CASCADE;

CREATE TABLE HocLieu_ChuaChuanHoa_4NF (
    MaHocLieu VARCHAR(20) NOT NULL,
    TieuDe VARCHAR(255) NOT NULL,
    MaTacGia VARCHAR(20) NOT NULL,
    TenTacGia VARCHAR(100) NOT NULL,
    MaChuDe VARCHAR(20) NOT NULL,
    TenChuDe VARCHAR(100) NOT NULL,
    PRIMARY KEY (MaHocLieu, MaTacGia, MaChuDe)
);

-- 1.2. Nạp dữ liệu mẫu minh họa tích Descartes (M x N)
-- Cuốn sách HL01 có 2 tác giả (TG01, TG02) và 2 chủ đề (CD02, CD03)
-- Dẫn đến bắt buộc phải lưu 2 x 2 = 4 dòng trong bảng phẳng!
INSERT INTO HocLieu_ChuaChuanHoa_4NF 
    (MaHocLieu, TieuDe, MaTacGia, TenTacGia, MaChuDe, TenChuDe) 
VALUES
('HL01', 'Database System Concepts', 'TG01', 'A. Silberschatz', 'CD02', 'CSDL'),
('HL01', 'Database System Concepts', 'TG01', 'A. Silberschatz', 'CD03', 'CSDL Nâng cao'),
('HL01', 'Database System Concepts', 'TG02', 'R. Elmasri', 'CD02', 'CSDL'),
('HL01', 'Database System Concepts', 'TG02', 'R. Elmasri', 'CD03', 'CSDL Nâng cao');

-- Hiển thị bảng phẳng trước khi phân rã 4NF (Dư thừa 4 dòng do tích Descartes)
SELECT * FROM HocLieu_ChuaChuanHoa_4NF ORDER BY MaTacGia, MaChuDe;

-- 1.3. Mô phỏng 3 Dị thường của bảng chưa chuẩn hóa 4NF:
-- a) Dị thường thêm (Insertion Anomaly): Muốn gán chủ đề 'CD04' (Lập trình) cho HL01 
--    nhưng chưa xác định tác giả nào phụ trách -> Thất bại do MaTacGia thuộc Khóa chính (NOT NULL).
-- b) Dị thường sửa (Update Anomaly): Sửa tên tác giả 'A. Silberschatz' -> Phải sửa ở nhiều dòng (2 dòng), 
--    nếu sửa sót 1 dòng sẽ dẫn tới dữ liệu mâu thuẫn.
-- c) Dị thường xóa (Deletion Anomaly): Xóa chủ đề 'CSDL Nâng cao' nếu xóa cả bộ dữ liệu 
--    có nguy cơ mất luôn thông tin liên kết tác giả nếu tài liệu chỉ còn 1 dòng.

-- 1.4. Phép phân rã theo Định lý Fagin (1977) thành 2 bảng quan hệ chuẩn 4NF
-- Trong hệ thống chính (01_schema.sql), dữ liệu được tách thành:
-- • HocLieu_TacGia(MaHocLieu, MaTacGia) - 2 dòng cho HL01
-- • HocLieu_ChuDe(MaHocLieu, MaChuDe)   - 2 dòng cho HL01
-- Tổng số dòng lưu trữ: 2 + 2 = 4 dòng (Với M=5, N=10: giảm từ 50 xuống 15 dòng, tiết kiệm 70%).

-- 1.5. TRUY VẤN NỐI KHÔI PHỤC ĐÚNG THÔNG TIN (LOSSLESS-JOIN RECOVERY)
-- Nối HocLieu_TacGia và HocLieu_ChuDe trên thuộc tính MaHocLieu
-- Kết quả trả về đúng 100% 4 dòng ban đầu, không mất mát và không sinh dòng giả (spurious tuple).
SELECT 
    h.MaHocLieu,
    h.TieuDe,
    tg.MaTacGia,
    tg.TenTacGia,
    cd.MaChuDe,
    cd.TenChuDe
FROM HocLieu h
JOIN HocLieu_TacGia htg ON h.MaHocLieu = htg.MaHocLieu
JOIN TacGia tg ON htg.MaTacGia = tg.MaTacGia
JOIN HocLieu_ChuDe hcd ON h.MaHocLieu = hcd.MaHocLieu
JOIN ChuDe cd ON hcd.MaChuDe = cd.MaChuDe
WHERE h.MaHocLieu = 'HL01'
ORDER BY tg.MaTacGia, cd.MaChuDe;


-- ####################################################################
-- PHẦN 2: THỰC NGHIỆM SINGLE TABLE INHERITANCE (BƯỚC 4 ĐỀ TÀI 3)
-- ####################################################################

-- 2.1. Thiết kế phương án "Một bảng chung" gom toàn bộ thuộc tính lớp cha và 4 lớp con
DROP TABLE IF EXISTS HocLieu_SingleTable CASCADE;

CREATE TABLE HocLieu_SingleTable (
    -- Thuộc tính dùng chung của lớp cha HocLieu (8 cột)
    MaHocLieu VARCHAR(20) PRIMARY KEY,
    TieuDe VARCHAR(255) NOT NULL,
    MoTa TEXT,
    NamXuatBan INT,
    NgonNgu VARCHAR(50),
    GiaTien DECIMAL(12, 2),
    NgayNhapKho DATE DEFAULT CURRENT_DATE,
    AnhBia VARCHAR(255),
    
    -- Discriminator: Cột phân biệt kiểu loại đối tượng con
    LoaiHocLieu VARCHAR(20) NOT NULL, -- 'Sach', 'LuanVan', 'VideoBaiGiang', 'TaiLieuDienTu'
    
    -- Thuộc tính đặc thù của lớp con Sach (4 cột)
    ISBN VARCHAR(50),
    NhaXuatBan VARCHAR(100),
    SoTrang INT,
    LanTaiBan INT,
    
    -- Thuộc tính đặc thù của lớp con LuanVan (5 cột)
    NguoiHuongDan VARCHAR(255),
    Truong VARCHAR(150),
    CapBaoVe VARCHAR(50),
    ChuyenNganh VARCHAR(100),
    DiemBaoVe DECIMAL(4, 2),
    
    -- Thuộc tính đặc thù của lớp con VideoBaiGiang (4 cột)
    ThoiLuong INT,
    DoPhanGiai VARCHAR(20),
    NgonNguPhuDe VARCHAR(50),
    DungLuongVideo DECIMAL(8, 2),
    
    -- Thuộc tính đặc thù của lớp con TaiLieuDienTu (4 cột)
    DinhDang VARCHAR(20),
    DungLuongFile DECIMAL(8, 2),
    QuyenTruyCap VARCHAR(50),
    Url_LienKet TEXT
);

-- 2.2. Nạp dữ liệu mẫu đại diện cho 4 lớp con vào bảng chung
INSERT INTO HocLieu_SingleTable (
    MaHocLieu, TieuDe, MoTa, NamXuatBan, NgonNgu, GiaTien, LoaiHocLieu,
    -- Sach
    ISBN, NhaXuatBan, SoTrang, LanTaiBan,
    -- LuanVan
    NguoiHuongDan, Truong, CapBaoVe, ChuyenNganh, DiemBaoVe,
    -- VideoBaiGiang
    ThoiLuong, DoPhanGiai, NgonNguPhuDe, DungLuongVideo,
    -- TaiLieuDienTu
    DinhDang, DungLuongFile, QuyenTruyCap, Url_LienKet
) VALUES
-- Bản ghi 1: Thuộc loại Sách (Chỉ có giá trị ở các cột Sách, các cột khác NULL)
('HL01', 'Database System Concepts', 'Giáo trình CSDL', 2020, 'Tiếng Anh', 500000, 'Sach',
 '978-0078022159', 'McGraw-Hill', 1376, 7,
 NULL, NULL, NULL, NULL, NULL,
 NULL, NULL, NULL, NULL,
 NULL, NULL, NULL, NULL),

-- Bản ghi 2: Thuộc loại Luận văn (Chỉ có giá trị ở các cột Luận văn, các cột khác NULL)
('HL02', 'Nghiên cứu CSDL Hướng đối tượng', 'Luận văn Thạc sĩ', 2023, 'Tiếng Việt', 0, 'LuanVan',
 NULL, NULL, NULL, NULL,
 'GS.TS Trần Văn B', 'ĐH Sài Gòn', 'Thạc sĩ', 'Khoa học máy tính', 9.50,
 NULL, NULL, NULL, NULL,
 NULL, NULL, NULL, NULL),

-- Bản ghi 3: Thuộc loại Video bài giảng
('HL03', 'Bài giảng Video SQL Nâng cao', 'Video khóa học', 2024, 'Tiếng Việt', 0, 'VideoBaiGiang',
 NULL, NULL, NULL, NULL,
 NULL, NULL, NULL, NULL, NULL,
 120, '1080p', 'Tiếng Việt', 1024.50,
 NULL, NULL, NULL, NULL),

-- Bản ghi 4: Thuộc loại Tài liệu điện tử
('HL04', 'Slide Bài giảng CSDL Nâng cao', 'Slide định dạng PDF', 2024, 'Tiếng Việt', 0, 'TaiLieuDienTu',
 NULL, NULL, NULL, NULL,
 NULL, NULL, NULL, NULL, NULL,
 NULL, NULL, NULL, NULL,
 'PDF', 15.50, 'NoiBo', 'https://thuvien.edu.vn/tailieu/hl04.pdf');

-- 2.3. ĐÁNH GIÁ GIÁ TRỊ NULL THỰC TẾ TRÊN TỪNG DÒNG VÀ TOÀN BỘ BẢNG
-- Tổng số thuộc tính đặc thù của 4 lớp con là: 4 (Sach) + 5 (LuanVan) + 4 (Video) + 4 (TaiLieu) = 17 cột.
-- Mỗi đối tượng chỉ thuộc về 1 lớp con, do đó 12 đến 13 cột đặc thù của các lớp con khác BẮT BUỘC BỊ NULL!
SELECT 
    MaHocLieu,
    LoaiHocLieu,
    TieuDe,
    (
        (CASE WHEN ISBN IS NULL THEN 1 ELSE 0 END) +
        (CASE WHEN NhaXuatBan IS NULL THEN 1 ELSE 0 END) +
        (CASE WHEN SoTrang IS NULL THEN 1 ELSE 0 END) +
        (CASE WHEN LanTaiBan IS NULL THEN 1 ELSE 0 END) +
        (CASE WHEN NguoiHuongDan IS NULL THEN 1 ELSE 0 END) +
        (CASE WHEN Truong IS NULL THEN 1 ELSE 0 END) +
        (CASE WHEN CapBaoVe IS NULL THEN 1 ELSE 0 END) +
        (CASE WHEN ChuyenNganh IS NULL THEN 1 ELSE 0 END) +
        (CASE WHEN DiemBaoVe IS NULL THEN 1 ELSE 0 END) +
        (CASE WHEN ThoiLuong IS NULL THEN 1 ELSE 0 END) +
        (CASE WHEN DoPhanGiai IS NULL THEN 1 ELSE 0 END) +
        (CASE WHEN NgonNguPhuDe IS NULL THEN 1 ELSE 0 END) +
        (CASE WHEN DungLuongVideo IS NULL THEN 1 ELSE 0 END) +
        (CASE WHEN DinhDang IS NULL THEN 1 ELSE 0 END) +
        (CASE WHEN DungLuongFile IS NULL THEN 1 ELSE 0 END) +
        (CASE WHEN QuyenTruyCap IS NULL THEN 1 ELSE 0 END) +
        (CASE WHEN Url_LienKet IS NULL THEN 1 ELSE 0 END)
    ) AS SoCotLopConBiNull,
    17 AS TongCotLopCon,
    ROUND(
        (
            (CASE WHEN ISBN IS NULL THEN 1 ELSE 0 END) +
            (CASE WHEN NhaXuatBan IS NULL THEN 1 ELSE 0 END) +
            (CASE WHEN SoTrang IS NULL THEN 1 ELSE 0 END) +
            (CASE WHEN LanTaiBan IS NULL THEN 1 ELSE 0 END) +
            (CASE WHEN NguoiHuongDan IS NULL THEN 1 ELSE 0 END) +
            (CASE WHEN Truong IS NULL THEN 1 ELSE 0 END) +
            (CASE WHEN CapBaoVe IS NULL THEN 1 ELSE 0 END) +
            (CASE WHEN ChuyenNganh IS NULL THEN 1 ELSE 0 END) +
            (CASE WHEN DiemBaoVe IS NULL THEN 1 ELSE 0 END) +
            (CASE WHEN ThoiLuong IS NULL THEN 1 ELSE 0 END) +
            (CASE WHEN DoPhanGiai IS NULL THEN 1 ELSE 0 END) +
            (CASE WHEN NgonNguPhuDe IS NULL THEN 1 ELSE 0 END) +
            (CASE WHEN DungLuongVideo IS NULL THEN 1 ELSE 0 END) +
            (CASE WHEN DinhDang IS NULL THEN 1 ELSE 0 END) +
            (CASE WHEN DungLuongFile IS NULL THEN 1 ELSE 0 END) +
            (CASE WHEN QuyenTruyCap IS NULL THEN 1 ELSE 0 END) +
            (CASE WHEN Url_LienKet IS NULL THEN 1 ELSE 0 END)
        )::numeric / 17 * 100, 2
    ) AS TyLeNull_PhanTram
FROM HocLieu_SingleTable
ORDER BY MaHocLieu;

-- Thống kê tổng hợp toàn bảng: Tỷ lệ ô NULL trung bình trên các thuộc tính lớp con
SELECT 
    COUNT(*) AS TongSoBanGhi,
    COUNT(*) * 17 AS TongSoOThuocLopCon,
    SUM(
        (CASE WHEN ISBN IS NULL THEN 1 ELSE 0 END) +
        (CASE WHEN NhaXuatBan IS NULL THEN 1 ELSE 0 END) +
        (CASE WHEN SoTrang IS NULL THEN 1 ELSE 0 END) +
        (CASE WHEN LanTaiBan IS NULL THEN 1 ELSE 0 END) +
        (CASE WHEN NguoiHuongDan IS NULL THEN 1 ELSE 0 END) +
        (CASE WHEN Truong IS NULL THEN 1 ELSE 0 END) +
        (CASE WHEN CapBaoVe IS NULL THEN 1 ELSE 0 END) +
        (CASE WHEN ChuyenNganh IS NULL THEN 1 ELSE 0 END) +
        (CASE WHEN DiemBaoVe IS NULL THEN 1 ELSE 0 END) +
        (CASE WHEN ThoiLuong IS NULL THEN 1 ELSE 0 END) +
        (CASE WHEN DoPhanGiai IS NULL THEN 1 ELSE 0 END) +
        (CASE WHEN NgonNguPhuDe IS NULL THEN 1 ELSE 0 END) +
        (CASE WHEN DungLuongVideo IS NULL THEN 1 ELSE 0 END) +
        (CASE WHEN DinhDang IS NULL THEN 1 ELSE 0 END) +
        (CASE WHEN DungLuongFile IS NULL THEN 1 ELSE 0 END) +
        (CASE WHEN QuyenTruyCap IS NULL THEN 1 ELSE 0 END) +
        (CASE WHEN Url_LienKet IS NULL THEN 1 ELSE 0 END)
    ) AS TongSoOBiNull,
    ROUND(
        SUM(
            (CASE WHEN ISBN IS NULL THEN 1 ELSE 0 END) +
            (CASE WHEN NhaXuatBan IS NULL THEN 1 ELSE 0 END) +
            (CASE WHEN SoTrang IS NULL THEN 1 ELSE 0 END) +
            (CASE WHEN LanTaiBan IS NULL THEN 1 ELSE 0 END) +
            (CASE WHEN NguoiHuongDan IS NULL THEN 1 ELSE 0 END) +
            (CASE WHEN Truong IS NULL THEN 1 ELSE 0 END) +
            (CASE WHEN CapBaoVe IS NULL THEN 1 ELSE 0 END) +
            (CASE WHEN ChuyenNganh IS NULL THEN 1 ELSE 0 END) +
            (CASE WHEN DiemBaoVe IS NULL THEN 1 ELSE 0 END) +
            (CASE WHEN ThoiLuong IS NULL THEN 1 ELSE 0 END) +
            (CASE WHEN DoPhanGiai IS NULL THEN 1 ELSE 0 END) +
            (CASE WHEN NgonNguPhuDe IS NULL THEN 1 ELSE 0 END) +
            (CASE WHEN DungLuongVideo IS NULL THEN 1 ELSE 0 END) +
            (CASE WHEN DinhDang IS NULL THEN 1 ELSE 0 END) +
            (CASE WHEN DungLuongFile IS NULL THEN 1 ELSE 0 END) +
            (CASE WHEN QuyenTruyCap IS NULL THEN 1 ELSE 0 END) +
            (CASE WHEN Url_LienKet IS NULL THEN 1 ELSE 0 END)
        )::numeric / (COUNT(*) * 17) * 100, 2
    ) AS TyLeNullTrungBinh_PhanTram
FROM HocLieu_SingleTable;

-- 2.4. ĐÁNH GIÁ RÀNG BUỘC TOÀN VẸN (INTEGRITY CONSTRAINTS)
-- Ví dụ: Nghiệp vụ yêu cầu Sách bắt buộc phải có mã ISBN (NOT NULL).
-- Trong phương án Joined Table: Bảng Sach khai báo "ISBN VARCHAR(50) NOT NULL UNIQUE" -> Hoàn toàn hợp lệ.
-- Nhưng trong Single Table: Nếu chạy lệnh dưới đây sẽ BỊ LỖI NGAY LẬP TỨC:
--   ALTER TABLE HocLieu_SingleTable ALTER COLUMN ISBN SET NOT NULL;
-- -> LỖI: column "isbn" of relation "hoclieu_singletable" contains null values (vì các dòng LuanVan, Video không có ISBN).
-- Muốn ràng buộc, bắt buộc phải viết CHECK Constraint phức tạp:
ALTER TABLE HocLieu_SingleTable 
ADD CONSTRAINT chk_isbn_not_null_for_sach 
CHECK (LoaiHocLieu <> 'Sach' OR ISBN IS NOT NULL);

-- 2.5. SO SÁNH TRUY VẤN VÀ HIỆU NĂNG
-- a) Truy vấn đa hình trên Single Table: Không cần JOIN, đọc trực tiếp 1 bảng
EXPLAIN ANALYZE 
SELECT MaHocLieu, TieuDe, LoaiHocLieu, GiaTien 
FROM HocLieu_SingleTable 
WHERE LoaiHocLieu = 'Sach';

-- b) Truy vấn đa hình trên Joined Table: Phải JOIN bảng cha HocLieu với bảng con Sach
EXPLAIN ANALYZE 
SELECT h.MaHocLieu, h.TieuDe, s.ISBN, s.SoTrang, h.GiaTien 
FROM HocLieu h 
JOIN Sach s ON h.MaHocLieu = s.MaHocLieu;

