-- TẠO BẢNG THEO CHIẾN LƯỢC BẢNG CHA - BẢNG CON (Class Table)

-- 1. Bảng cha: Học Liệu
CREATE TABLE HocLieu (
    MaHocLieu VARCHAR(20) PRIMARY KEY,
    TieuDe VARCHAR(255) NOT NULL,
    NamXuatBan INT,
    GiaTien DECIMAL(10, 2)
);

-- 2. Các bảng con (Liên kết với bảng cha qua khóa ngoại MaHocLieu)
CREATE TABLE Sach (
    MaHocLieu VARCHAR(20) PRIMARY KEY REFERENCES HocLieu(MaHocLieu) ON DELETE CASCADE,
    ISBN VARCHAR(20) UNIQUE NOT NULL,
    NhaXuatBan VARCHAR(100),
    SoTrang INT
);

CREATE TABLE LuanVan (
    MaHocLieu VARCHAR(20) PRIMARY KEY REFERENCES HocLieu(MaHocLieu) ON DELETE CASCADE,
    NguoiHuongDan VARCHAR(100),
    CapBaoVe VARCHAR(50)
);

-- 3. Bảng Đa trị: Tác giả
CREATE TABLE TacGia (
    MaTacGia VARCHAR(20) PRIMARY KEY,
    TenTacGia VARCHAR(100) NOT NULL
);

-- Bảng trung gian để phá vỡ quan hệ N-N (Chuẩn 4NF)
CREATE TABLE HocLieu_TacGia (
    MaHocLieu VARCHAR(20) REFERENCES HocLieu(MaHocLieu) ON DELETE CASCADE,
    MaTacGia VARCHAR(20) REFERENCES TacGia(MaTacGia) ON DELETE CASCADE,
    PRIMARY KEY (MaHocLieu, MaTacGia)
);

-- 4. Bảng Bản sao & Mượn sách
CREATE TABLE BanSao (
    MaBanSao VARCHAR(20) PRIMARY KEY,
    MaHocLieu VARCHAR(20) REFERENCES HocLieu(MaHocLieu) ON DELETE CASCADE,
    TrangThai VARCHAR(50) DEFAULT 'San sang'
);
