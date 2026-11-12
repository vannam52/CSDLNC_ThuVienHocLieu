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
    AnhBia VARCHAR(255),
    LoaiHocLieu VARCHAR(20) NOT NULL CHECK (LoaiHocLieu IN ('Sach', 'LuanVan', 'VideoBaiGiang', 'TaiLieuDienTu')),
    UNIQUE (MaHocLieu, LoaiHocLieu)
);

-- 2. BẢNG CON (Kế thừa - Class Table / Joined Table Inheritance)
CREATE TABLE IF NOT EXISTS Sach (
    MaHocLieu VARCHAR(20) PRIMARY KEY,
    ISBN VARCHAR(50) UNIQUE NOT NULL,
    NhaXuatBan VARCHAR(100),
    SoTrang INT CHECK (SoTrang > 0),
    LanTaiBan INT DEFAULT 1 CHECK (LanTaiBan >= 1),
    LoaiHocLieu VARCHAR(20) GENERATED ALWAYS AS ('Sach') STORED,
    FOREIGN KEY (MaHocLieu, LoaiHocLieu) REFERENCES HocLieu(MaHocLieu, LoaiHocLieu) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS LuanVan (
    MaHocLieu VARCHAR(20) PRIMARY KEY,
    NguoiHuongDan VARCHAR(255),
    Truong VARCHAR(150),
    CapBaoVe VARCHAR(50), -- Thạc sĩ, Tiến sĩ
    ChuyenNganh VARCHAR(100),
    DiemBaoVe DECIMAL(4, 2) CHECK (DiemBaoVe >= 0 AND DiemBaoVe <= 10.0),
    LoaiHocLieu VARCHAR(20) GENERATED ALWAYS AS ('LuanVan') STORED,
    FOREIGN KEY (MaHocLieu, LoaiHocLieu) REFERENCES HocLieu(MaHocLieu, LoaiHocLieu) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS VideoBaiGiang (
    MaHocLieu VARCHAR(20) PRIMARY KEY,
    ThoiLuong INT CHECK (ThoiLuong > 0), -- Số phút
    DoPhanGiai VARCHAR(20),             -- 720p, 1080p, 4K
    NgonNguPhuDe VARCHAR(50),
    DungLuong DECIMAL(8, 2) CHECK (DungLuong > 0), -- MB
    LoaiHocLieu VARCHAR(20) GENERATED ALWAYS AS ('VideoBaiGiang') STORED,
    FOREIGN KEY (MaHocLieu, LoaiHocLieu) REFERENCES HocLieu(MaHocLieu, LoaiHocLieu) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS TaiLieuDienTu (
    MaHocLieu VARCHAR(20) PRIMARY KEY,
    DinhDang VARCHAR(20),                -- PDF, EPUB, DOCX
    DungLuong DECIMAL(8, 2) CHECK (DungLuong > 0), -- MB
    QuyenTruyCap VARCHAR(50) DEFAULT 'NoiBo',       -- Public, NoiBo, HanChe
    URL_LienKet TEXT,
    LoaiHocLieu VARCHAR(20) GENERATED ALWAYS AS ('TaiLieuDienTu') STORED,
    FOREIGN KEY (MaHocLieu, LoaiHocLieu) REFERENCES HocLieu(MaHocLieu, LoaiHocLieu) ON DELETE CASCADE
);

-- Kiểm tra toàn phần lúc kết thúc giao dịch để có thể chèn cha rồi chèn con.
-- Khi xóa cha, các dòng con bị xóa dây chuyền và mã cha không còn cần kiểm tra.
CREATE OR REPLACE FUNCTION fn_KiemTraChuyenBietHoa()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_MaHocLieu VARCHAR(20);
    v_LoaiHocLieu VARCHAR(20);
    v_CoLopCon BOOLEAN;
BEGIN
    IF TG_TABLE_NAME = 'hoclieu' THEN
        v_MaHocLieu := NEW.MaHocLieu;
    ELSE
        v_MaHocLieu := OLD.MaHocLieu;
    END IF;

    SELECT LoaiHocLieu INTO v_LoaiHocLieu
    FROM HocLieu WHERE MaHocLieu = v_MaHocLieu FOR UPDATE;
    IF NOT FOUND THEN
        RETURN NULL;
    END IF;

    v_CoLopCon := CASE v_LoaiHocLieu
        WHEN 'Sach' THEN EXISTS (SELECT 1 FROM Sach WHERE MaHocLieu = v_MaHocLieu)
        WHEN 'LuanVan' THEN EXISTS (SELECT 1 FROM LuanVan WHERE MaHocLieu = v_MaHocLieu)
        WHEN 'VideoBaiGiang' THEN EXISTS (SELECT 1 FROM VideoBaiGiang WHERE MaHocLieu = v_MaHocLieu)
        WHEN 'TaiLieuDienTu' THEN EXISTS (SELECT 1 FROM TaiLieuDienTu WHERE MaHocLieu = v_MaHocLieu)
    END;

    IF NOT v_CoLopCon THEN
        RAISE EXCEPTION 'Học liệu % cần một dòng ở lớp con %', v_MaHocLieu, v_LoaiHocLieu
            USING ERRCODE = '23514';
    END IF;
    RETURN NULL;
END;
$$;

CREATE CONSTRAINT TRIGGER trg_HocLieu_PhaiCoLopCon
AFTER INSERT OR UPDATE OF MaHocLieu, LoaiHocLieu ON HocLieu
DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION fn_KiemTraChuyenBietHoa();

CREATE CONSTRAINT TRIGGER trg_Sach_GiuLopCon
AFTER DELETE OR UPDATE OF MaHocLieu ON Sach
DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION fn_KiemTraChuyenBietHoa();

CREATE CONSTRAINT TRIGGER trg_LuanVan_GiuLopCon
AFTER DELETE OR UPDATE OF MaHocLieu ON LuanVan
DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION fn_KiemTraChuyenBietHoa();

CREATE CONSTRAINT TRIGGER trg_VideoBaiGiang_GiuLopCon
AFTER DELETE OR UPDATE OF MaHocLieu ON VideoBaiGiang
DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION fn_KiemTraChuyenBietHoa();

CREATE CONSTRAINT TRIGGER trg_TaiLieuDienTu_GiuLopCon
AFTER DELETE OR UPDATE OF MaHocLieu ON TaiLieuDienTu
DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION fn_KiemTraChuyenBietHoa();

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
    TrangThai VARCHAR(50) NOT NULL DEFAULT 'San sang' CHECK (TrangThai IN ('San sang', 'Dang muon', 'Bao tri', 'Bi mat')),
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
    TrangThai VARCHAR(50) NOT NULL DEFAULT 'Dang muon' CHECK (TrangThai IN ('Dang muon', 'Da tra', 'Qua han')),
    GhiChu TEXT
);

-- Một bản sao chỉ có tối đa một phiếu chưa trả, kể cả khi chèn trực tiếp.
CREATE UNIQUE INDEX uq_phieumuon_bansao_dang_hieu_luc
ON PhieuMuon (MaBanSao) WHERE TrangThai IN ('Dang muon', 'Qua han');

-- 5. CHỈ MỤC TỐI ƯU HÓA TÌM KIẾM (GIN Trigram Index phục vụ Menu 7)
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX IF NOT EXISTS idx_hoclieu_tieude_trgm ON HocLieu USING gin (TieuDe gin_trgm_ops);
