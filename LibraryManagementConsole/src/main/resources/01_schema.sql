-- 1. BẢNG CHA: HocLieu
CREATE TABLE HocLieu (
    MaHocLieu VARCHAR(20) PRIMARY KEY,
    TieuDe VARCHAR(255) NOT NULL,
    MoTa TEXT,
    NamXuatBan INT,
    NgonNgu VARCHAR(50),
    GiaTien DECIMAL(12, 2),
    NgayNhapKho DATE DEFAULT CURRENT_DATE,
    AnhBia VARCHAR(255)
);

-- 2. BẢNG CON (Kế thừa - Class Table Inheritance)
CREATE TABLE Sach (
    MaHocLieu VARCHAR(20) PRIMARY KEY REFERENCES HocLieu(MaHocLieu) ON DELETE CASCADE,
    ISBN VARCHAR(50) UNIQUE NOT NULL,
    NhaXuatBan VARCHAR(100),
    SoTrang INT,
    LanTaiBan INT
);

CREATE TABLE LuanVan (
    MaHocLieu VARCHAR(20) PRIMARY KEY REFERENCES HocLieu(MaHocLieu) ON DELETE CASCADE,
    NguoiHuongDan VARCHAR(255),
    Truong VARCHAR(150),
    CapBaoVe VARCHAR(50),
    ChuyenNganh VARCHAR(100),
    DiemBaoVe DECIMAL(4, 2)
);

CREATE TABLE VideoBaiGiang (
    MaHocLieu VARCHAR(20) PRIMARY KEY REFERENCES HocLieu(MaHocLieu) ON DELETE CASCADE,
    ThoiLuong INT, -- Phút
    DoPhanGiai VARCHAR(20),
    NgonNguPhuDe VARCHAR(50),
    DungLuong DECIMAL(8, 2)
);

CREATE TABLE TaiLieuDienTu (
    MaHocLieu VARCHAR(20) PRIMARY KEY REFERENCES HocLieu(MaHocLieu) ON DELETE CASCADE,
    DinhDang VARCHAR(20),
    DungLuong DECIMAL(8, 2),
    QuyenTruyCap VARCHAR(50),
    URL_LienKet TEXT
);

-- 3. XỬ LÝ ĐA TRỊ (Chuẩn hóa 4NF)
CREATE TABLE TacGia (
    MaTacGia VARCHAR(20) PRIMARY KEY,
    TenTacGia VARCHAR(100) NOT NULL,
    DonViCongTac VARCHAR(150),
    Email VARCHAR(100)
);

CREATE TABLE HocLieu_TacGia (
    MaHocLieu VARCHAR(20) REFERENCES HocLieu(MaHocLieu) ON DELETE CASCADE,
    MaTacGia VARCHAR(20) REFERENCES TacGia(MaTacGia) ON DELETE CASCADE,
    PRIMARY KEY (MaHocLieu, MaTacGia)
);

-- Cây chủ đề (Có tự liên kết đệ quy ChuDeCha)
CREATE TABLE ChuDe (
    MaChuDe VARCHAR(20) PRIMARY KEY,
    TenChuDe VARCHAR(100) NOT NULL,
    MoTaChuDe TEXT,
    ChuDeCha VARCHAR(20) REFERENCES ChuDe(MaChuDe) ON DELETE SET NULL
);

CREATE TABLE HocLieu_ChuDe (
    MaHocLieu VARCHAR(20) REFERENCES HocLieu(MaHocLieu) ON DELETE CASCADE,
    MaChuDe VARCHAR(20) REFERENCES ChuDe(MaChuDe) ON DELETE CASCADE,
    PRIMARY KEY (MaHocLieu, MaChuDe)
);

-- 4. BẢN SAO VẬT LÝ & QUẢN LÝ MƯỢN TRẢ
CREATE TABLE BanSao (
    MaBanSao VARCHAR(20) PRIMARY KEY,
    MaHocLieu VARCHAR(20) REFERENCES HocLieu(MaHocLieu) ON DELETE CASCADE,
    TrangThai VARCHAR(50) DEFAULT 'San sang', -- 'San sang', 'Dang muon', 'Bao tri'
    TinhTrangVatLy VARCHAR(100),
    ViTriKe VARCHAR(100)
);

CREATE TABLE BanDoc (
    MaBanDoc VARCHAR(20) PRIMARY KEY,
    HoTen VARCHAR(100) NOT NULL,
    Email VARCHAR(100) UNIQUE,
    SoDienThoai VARCHAR(20),
    MatKhau VARCHAR(255) NOT NULL,
    LoaiTaiKhoan VARCHAR(50),
    NgayHetHan DATE,
    TaiKhoanBiKhoa BOOLEAN DEFAULT FALSE
);

CREATE TABLE PhieuMuon (
    MaPhieu VARCHAR(20) PRIMARY KEY,
    MaBanDoc VARCHAR(20) REFERENCES BanDoc(MaBanDoc),
    MaBanSao VARCHAR(20) REFERENCES BanSao(MaBanSao),
    NgayMuon TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    NgayHanTra TIMESTAMP,
    NgayTraThucTe TIMESTAMP,
    TienPhat DECIMAL(12, 2) DEFAULT 0,
    TrangThai VARCHAR(50) DEFAULT 'Dang muon',
    GhiChu TEXT
);