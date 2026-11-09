-- ====================================================================
-- ĐỒ ÁN CSDL NÂNG CAO - ĐỀ TÀI 3: THƯ VIỆN HỌC LIỆU ĐA PHƯƠNG TIỆN
-- FILE: 01_schema.sql
-- MỤC ĐÍCH: TẠO CẤU TRÚC 12 BẢNG VẬT LÝ, KHÓA CHÍNH, KHÓA NGOẠI,
--          RÀNG BUỘC TOÀN VẸN (NOT NULL, UNIQUE, CHECK, CASCADE) VÀ INDEX
-- ====================================================================

-- 1. BẢNG CHA: HocLieu (Superclass)
CREATE TABLE IF NOT EXISTS HocLieu (
    MaHocLieu VARCHAR(20) PRIMARY KEY,
    TieuDe VARCHAR(255) NOT NULL,
    MoTa TEXT,
    NamXuatBan INT CHECK (NamXuatBan > 1900),
    NgonNgu VARCHAR(50) DEFAULT 'Tiếng Việt',
    GiaTien DECIMAL(12, 2) DEFAULT 0 CHECK (GiaTien >= 0),
    NgayNhapKho DATE DEFAULT CURRENT_DATE,
    AnhBia VARCHAR(255)
);

-- 2. BẢNG CON (Kế thừa - Class Table / Joined Table Inheritance)
CREATE TABLE IF NOT EXISTS Sach (
    MaHocLieu VARCHAR(20) PRIMARY KEY REFERENCES HocLieu(MaHocLieu) ON DELETE CASCADE,
    ISBN VARCHAR(50) UNIQUE NOT NULL,
    NhaXuatBan VARCHAR(100),
    SoTrang INT CHECK (SoTrang > 0),
    LanTaiBan INT DEFAULT 1 CHECK (LanTaiBan >= 1)
);

CREATE TABLE IF NOT EXISTS LuanVan (
    MaHocLieu VARCHAR(20) PRIMARY KEY REFERENCES HocLieu(MaHocLieu) ON DELETE CASCADE,
    NguoiHuongDan VARCHAR(255),
    Truong VARCHAR(150),
    CapBaoVe VARCHAR(50), -- Thạc sĩ, Tiến sĩ
    ChuyenNganh VARCHAR(100),
    DiemBaoVe DECIMAL(4, 2) CHECK (DiemBaoVe >= 0 AND DiemBaoVe <= 10.0)
);

CREATE TABLE IF NOT EXISTS VideoBaiGiang (
    MaHocLieu VARCHAR(20) PRIMARY KEY REFERENCES HocLieu(MaHocLieu) ON DELETE CASCADE,
    ThoiLuong INT CHECK (ThoiLuong > 0), -- Số phút
    DoPhanGiai VARCHAR(20),             -- 720p, 1080p, 4K
    NgonNguPhuDe VARCHAR(50),
    DungLuong DECIMAL(8, 2) CHECK (DungLuong > 0) -- MB
);

CREATE TABLE IF NOT EXISTS TaiLieuDienTu (
    MaHocLieu VARCHAR(20) PRIMARY KEY REFERENCES HocLieu(MaHocLieu) ON DELETE CASCADE,
    DinhDang VARCHAR(20),                -- PDF, EPUB, DOCX
    DungLuong DECIMAL(8, 2) CHECK (DungLuong > 0), -- MB
    QuyenTruyCap VARCHAR(50) DEFAULT 'NoiBo',       -- Public, NoiBo, HanChe
    URL_LienKet TEXT
);

-- 3. XỬ LÝ ĐA TRỊ (Chuẩn hóa 4NF)
CREATE TABLE IF NOT EXISTS TacGia (
    MaTacGia VARCHAR(20) PRIMARY KEY,
    TenTacGia VARCHAR(100) NOT NULL,
    DonViCongTac VARCHAR(150),
    Email VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS HocLieu_TacGia (
    MaHocLieu VARCHAR(20) REFERENCES HocLieu(MaHocLieu) ON DELETE CASCADE,
    MaTacGia VARCHAR(20) REFERENCES TacGia(MaTacGia) ON DELETE CASCADE,
    PRIMARY KEY (MaHocLieu, MaTacGia)
);

-- Cây chủ đề (Tự liên kết đệ quy 1:N ChuDeCha)
CREATE TABLE IF NOT EXISTS ChuDe (
    MaChuDe VARCHAR(20) PRIMARY KEY,
    TenChuDe VARCHAR(100) NOT NULL,
    MoTaChuDe TEXT,
    ChuDeCha VARCHAR(20) REFERENCES ChuDe(MaChuDe) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS HocLieu_ChuDe (
    MaHocLieu VARCHAR(20) REFERENCES HocLieu(MaHocLieu) ON DELETE CASCADE,
    MaChuDe VARCHAR(20) REFERENCES ChuDe(MaChuDe) ON DELETE CASCADE,
    PRIMARY KEY (MaHocLieu, MaChuDe)
);

-- 4. BẢN SAO VẬT LÝ & QUẢN LÝ MƯỢN TRẢ
CREATE TABLE IF NOT EXISTS BanSao (
    MaBanSao VARCHAR(20) PRIMARY KEY,
    MaHocLieu VARCHAR(20) NOT NULL REFERENCES HocLieu(MaHocLieu) ON DELETE CASCADE,
    TrangThai VARCHAR(50) DEFAULT 'San sang' CHECK (TrangThai IN ('San sang', 'Dang muon', 'Bao tri', 'Bi mat')),
    TinhTrangVatLy VARCHAR(100) DEFAULT 'Mới',
    ViTriKe VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS BanDoc (
    MaBanDoc VARCHAR(20) PRIMARY KEY,
    HoTen VARCHAR(100) NOT NULL,
    Email VARCHAR(100) UNIQUE NOT NULL,
    SoDienThoai VARCHAR(20),
    MatKhau VARCHAR(255) NOT NULL,
    LoaiTaiKhoan VARCHAR(50) DEFAULT 'Sinh vien' CHECK (LoaiTaiKhoan IN ('Sinh vien', 'Giang vien', 'Nghien cuu sinh', 'Khach')),
    NgayHetHan DATE,
    TaiKhoanBiKhoa BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS PhieuMuon (
    MaPhieu VARCHAR(20) PRIMARY KEY,
    MaBanDoc VARCHAR(20) NOT NULL REFERENCES BanDoc(MaBanDoc),
    MaBanSao VARCHAR(20) NOT NULL REFERENCES BanSao(MaBanSao),
    NgayMuon TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    NgayHanTra TIMESTAMP,
    NgayTraThucTe TIMESTAMP,
    TienPhat DECIMAL(12, 2) DEFAULT 0 CHECK (TienPhat >= 0),
    TrangThai VARCHAR(50) DEFAULT 'Dang muon' CHECK (TrangThai IN ('Dang muon', 'Da tra', 'Qua han')),
    GhiChu TEXT
);

-- 5. CHỈ MỤC TỐI ƯU HÓA TÌM KIẾM (GIN Trigram Index phục vụ Menu 7)
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX IF NOT EXISTS idx_hoclieu_tieude_trgm ON HocLieu USING gin (TieuDe gin_trgm_ops);
