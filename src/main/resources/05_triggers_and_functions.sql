-- ====================================================================
-- ĐỒ ÁN CSDL NÂNG CAO - ĐỀ TÀI 3: THƯ VIỆN HỌC LIỆU ĐA PHƯƠNG TIỆN
-- FILE: 05_triggers_and_functions.sql
-- MỤC ĐÍCH: BỔ SUNG ĐẦY ĐỦ TRIGGERS, FUNCTIONS, PROCEDURES VÀ VIEWS (PL/pgSQL)
-- ====================================================================

-- --------------------------------------------------------------------
-- PHẦN I: VIEWS (TỔNG HỢP VÀ ĐA HÌNH)
-- --------------------------------------------------------------------

-- 1. View đa hình tổng hợp đầy đủ thông tin học liệu và các lớp con
-- Gom nhóm danh sách tác giả, chủ đề thành mảng chuỗi (STRING_AGG)
CREATE OR REPLACE VIEW v_HocLieu_DayDu AS
SELECT 
    h.MaHocLieu,
    h.TieuDe,
    h.MoTa,
    h.NamXuatBan,
    h.NgonNgu,
    h.GiaTien,
    h.NgayNhapKho,
    CASE 
        WHEN s.MaHocLieu IS NOT NULL THEN 'Sách'
        WHEN l.MaHocLieu IS NOT NULL THEN 'Luận văn'
        WHEN v.MaHocLieu IS NOT NULL THEN 'Video bài giảng'
        WHEN t.MaHocLieu IS NOT NULL THEN 'Tài liệu điện tử'
        ELSE 'Học liệu tổng quát'
    END AS LoaiHocLieu,
    -- Thuộc tính đặc thù của Sách
    s.ISBN,
    s.NhaXuatBan,
    s.SoTrang,
    -- Thuộc tính đặc thù của Luận văn
    l.NguoiHuongDan,
    l.Truong,
    l.CapBaoVe,
    l.DiemBaoVe,
    -- Thuộc tính đặc thù của Video
    v.ThoiLuong,
    v.DoPhanGiai,
    -- Thuộc tính đặc thù của Tài liệu điện tử
    t.DinhDang,
    t.QuyenTruyCap,
    -- Gom nhóm tác giả và chủ đề
    COALESCE(STRING_AGG(DISTINCT tg.TenTacGia, ', '), 'Chưa rõ') AS DanhSachTacGia,
    COALESCE(STRING_AGG(DISTINCT cd.TenChuDe, ', '), 'Chưa phân loại') AS DanhSachChuDe,
    COUNT(DISTINCT bs.MaBanSao) AS TongSoBanSao,
    COUNT(DISTINCT CASE WHEN bs.TrangThai = 'San sang' THEN bs.MaBanSao END) AS SoBanSaoKhaDung
FROM HocLieu h
LEFT JOIN Sach s ON h.MaHocLieu = s.MaHocLieu
LEFT JOIN LuanVan l ON h.MaHocLieu = l.MaHocLieu
LEFT JOIN VideoBaiGiang v ON h.MaHocLieu = v.MaHocLieu
LEFT JOIN TaiLieuDienTu t ON h.MaHocLieu = t.MaHocLieu
LEFT JOIN HocLieu_TacGia htg ON h.MaHocLieu = htg.MaHocLieu
LEFT JOIN TacGia tg ON htg.MaTacGia = tg.MaTacGia
LEFT JOIN HocLieu_ChuDe hcd ON h.MaHocLieu = hcd.MaHocLieu
LEFT JOIN ChuDe cd ON hcd.MaChuDe = cd.MaChuDe
LEFT JOIN BanSao bs ON h.MaHocLieu = bs.MaHocLieu
GROUP BY 
    h.MaHocLieu, h.TieuDe, h.MoTa, h.NamXuatBan, h.NgonNgu, h.GiaTien, h.NgayNhapKho,
    s.MaHocLieu, s.ISBN, s.NhaXuatBan, s.SoTrang,
    l.MaHocLieu, l.NguoiHuongDan, l.Truong, l.CapBaoVe, l.DiemBaoVe,
    v.MaHocLieu, v.ThoiLuong, v.DoPhanGiai,
    t.MaHocLieu, t.DinhDang, t.QuyenTruyCap;

-- 2. View báo cáo tình hình mượn trả học liệu
CREATE OR REPLACE VIEW v_BaoCao_MuonTra AS
SELECT 
    pm.MaPhieu,
    bd.MaBanDoc,
    bd.HoTen AS TenBanDoc,
    bd.LoaiTaiKhoan,
    bs.MaBanSao,
    hl.TieuDe AS TenHocLieu,
    bs.ViTriKe,
    pm.NgayMuon,
    pm.NgayHanTra,
    pm.NgayTraThucTe,
    pm.TrangThai,
    pm.TienPhat,
    CASE 
        WHEN pm.TrangThai = 'Dang muon' AND CURRENT_TIMESTAMP > pm.NgayHanTra 
            THEN GREATEST(0, CEIL(EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - pm.NgayHanTra)) / 86400)::INT)
        WHEN pm.NgayTraThucTe IS NOT NULL AND pm.NgayTraThucTe > pm.NgayHanTra 
            THEN GREATEST(0, CEIL(EXTRACT(EPOCH FROM (pm.NgayTraThucTe - pm.NgayHanTra)) / 86400)::INT)
        ELSE 0
    END AS SoNgayTre,
    pm.GhiChu
FROM PhieuMuon pm
JOIN BanDoc bd ON pm.MaBanDoc = bd.MaBanDoc
JOIN BanSao bs ON pm.MaBanSao = bs.MaBanSao
JOIN HocLieu hl ON bs.MaHocLieu = hl.MaHocLieu;


-- --------------------------------------------------------------------
-- PHẦN II: FUNCTIONS (HÀM NGHIỆP VỤ & TÌM KIẾM NÂNG CAO)
-- --------------------------------------------------------------------

-- FUNCTION 1: Kiểm tra bạn đọc có đủ điều kiện mượn sách hay không
-- Quy tắc nghiệp vụ:
--   1. Bạn đọc phải tồn tại
--   2. Tài khoản không bị khóa (TaiKhoanBiKhoa = FALSE)
--   3. Thẻ bạn đọc còn hạn (NgayHetHan >= CURRENT_DATE)
--   4. Số sách đang mượn < 5 cuốn
--   5. Không có phiếu mượn nào quá hạn mà chưa trả
CREATE OR REPLACE FUNCTION fn_KiemTra_CoTheMuon(p_MaBanDoc VARCHAR(20))
RETURNS BOOLEAN
LANGUAGE plpgsql
AS $$
DECLARE
    v_BiKhoa BOOLEAN;
    v_NgayHetHan DATE;
    v_SoSachDangMuon INT;
    v_CoPhieuQuaHan BOOLEAN;
BEGIN
    -- 1. Kiểm tra bạn đọc
    SELECT TaiKhoanBiKhoa, NgayHetHan 
    INTO v_BiKhoa, v_NgayHetHan
    FROM BanDoc
    WHERE MaBanDoc = p_MaBanDoc;

    IF NOT FOUND THEN
        RETURN FALSE;
    END IF;

    -- 2. Kiểm tra khóa tài khoản
    IF v_BiKhoa = TRUE THEN
        RETURN FALSE;
    END IF;

    -- 3. Kiểm tra hạn thẻ
    IF v_NgayHetHan IS NOT NULL AND v_NgayHetHan < CURRENT_DATE THEN
        RETURN FALSE;
    END IF;

    -- 4. Kiểm tra giới hạn số lượng sách (tối đa 5 cuốn)
    SELECT COUNT(*) INTO v_SoSachDangMuon
    FROM PhieuMuon
    WHERE MaBanDoc = p_MaBanDoc AND TrangThai IN ('Dang muon', 'Qua han');

    IF v_SoSachDangMuon >= 5 THEN
        RETURN FALSE;
    END IF;

    -- 5. Kiểm tra sách nợ quá hạn
    SELECT EXISTS (
        SELECT 1 FROM PhieuMuon
        WHERE MaBanDoc = p_MaBanDoc 
          AND (TrangThai = 'Qua han'
               OR (TrangThai = 'Dang muon' AND NgayHanTra < CURRENT_TIMESTAMP))
    ) INTO v_CoPhieuQuaHan;

    IF v_CoPhieuQuaHan THEN
        RETURN FALSE;
    END IF;

    RETURN TRUE;
END;
$$;


-- FUNCTION 2: Thống kê học liệu theo chủ đề (Trả về TABLE)
-- Tính số lượng học liệu, tổng giá trị và tỷ lệ phần trăm trên tổng học liệu
CREATE OR REPLACE FUNCTION fn_ThongKe_HocLieu_TheoChuDe()
RETURNS TABLE (
    MaChuDe VARCHAR(20),
    TenChuDe VARCHAR(100),
    SoLuongHocLieu BIGINT,
    TiLePhanTram NUMERIC(5, 2),
    TongGiaTri DECIMAL(14, 2)
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_TongHocLieu BIGINT;
BEGIN
    SELECT COUNT(*) INTO v_TongHocLieu FROM HocLieu;
    IF v_TongHocLieu = 0 THEN
        v_TongHocLieu := 1;
    END IF;

    RETURN QUERY
    SELECT 
        c.MaChuDe,
        c.TenChuDe,
        COUNT(hc.MaHocLieu) AS SoLuongHocLieu,
        ROUND((COUNT(hc.MaHocLieu)::NUMERIC / v_TongHocLieu * 100), 2) AS TiLePhanTram,
        COALESCE(SUM(h.GiaTien), 0) AS TongGiaTri
    FROM ChuDe c
    LEFT JOIN HocLieu_ChuDe hc ON c.MaChuDe = hc.MaChuDe
    LEFT JOIN HocLieu h ON hc.MaHocLieu = h.MaHocLieu
    GROUP BY c.MaChuDe, c.TenChuDe
    ORDER BY SoLuongHocLieu DESC, c.TenChuDe;
END;
$$;


-- FUNCTION 3: Tìm kiếm toàn văn kết hợp độ tương đồng GIN Trigram
-- Trả về bảng học liệu có kèm độ tương đồng (DoTuongDong từ 0.0 đến 1.0)
CREATE OR REPLACE FUNCTION fn_TimKiem_HocLieu(
    p_TuKhoa TEXT,
    p_NgonNgu VARCHAR(50) DEFAULT NULL
)
RETURNS TABLE (
    MaHocLieu VARCHAR(20),
    TieuDe VARCHAR(255),
    PhanLoai TEXT,
    NgonNgu VARCHAR(50),
    NamXuatBan INT,
    GiaTien DECIMAL(12, 2),
    DoTuongDong REAL
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        h.MaHocLieu,
        h.TieuDe,
        CASE 
            WHEN s.MaHocLieu IS NOT NULL THEN 'Sách'
            WHEN l.MaHocLieu IS NOT NULL THEN 'Luận văn'
            WHEN v.MaHocLieu IS NOT NULL THEN 'Video bài giảng'
            WHEN t.MaHocLieu IS NOT NULL THEN 'Tài liệu điện tử'
            ELSE 'Học liệu khác'
        END AS PhanLoai,
        h.NgonNgu,
        h.NamXuatBan,
        h.GiaTien,
        similarity(h.TieuDe, p_TuKhoa) AS DoTuongDong
    FROM HocLieu h
    LEFT JOIN Sach s ON h.MaHocLieu = s.MaHocLieu
    LEFT JOIN LuanVan l ON h.MaHocLieu = l.MaHocLieu
    LEFT JOIN VideoBaiGiang v ON h.MaHocLieu = v.MaHocLieu
    LEFT JOIN TaiLieuDienTu t ON h.MaHocLieu = t.MaHocLieu
    WHERE (p_NgonNgu IS NULL OR h.NgonNgu = p_NgonNgu)
      AND (h.TieuDe % p_TuKhoa OR h.TieuDe ILIKE '%' || p_TuKhoa || '%')
    ORDER BY DoTuongDong DESC, h.NamXuatBan DESC;
END;
$$;


-- FUNCTION 4: Tìm kiếm động linh hoạt với Dynamic SQL (EXECUTE format)
-- Minh họa kỹ thuật Dynamic SQL nâng cao trong PL/pgSQL
CREATE OR REPLACE FUNCTION fn_TimKiem_HocLieu_Dong(
    p_TieuDe TEXT DEFAULT NULL,
    p_NamTu INT DEFAULT NULL,
    p_NamDen INT DEFAULT NULL,
    p_MaChuDe VARCHAR(20) DEFAULT NULL,
    p_SapXepTheo VARCHAR(50) DEFAULT 'TieuDe'
)
RETURNS TABLE (
    MaHocLieu VARCHAR(20),
    TieuDe VARCHAR(255),
    NamXuatBan INT,
    GiaTien DECIMAL(12, 2)
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_Sql TEXT;
    v_Where TEXT := ' WHERE 1=1 ';
BEGIN
    v_Sql := 'SELECT DISTINCT h.MaHocLieu, h.TieuDe, h.NamXuatBan, h.GiaTien FROM HocLieu h ';

    IF p_MaChuDe IS NOT NULL AND TRIM(p_MaChuDe) <> '' THEN
        v_Sql := v_Sql || ' JOIN HocLieu_ChuDe hc ON h.MaHocLieu = hc.MaHocLieu AND hc.MaChuDe = ' || quote_literal(p_MaChuDe);
    END IF;

    IF p_TieuDe IS NOT NULL AND TRIM(p_TieuDe) <> '' THEN
        v_Where := v_Where || ' AND h.TieuDe ILIKE ' || quote_literal('%' || p_TieuDe || '%');
    END IF;

    IF p_NamTu IS NOT NULL THEN
        v_Where := v_Where || ' AND h.NamXuatBan >= ' || p_NamTu;
    END IF;

    IF p_NamDen IS NOT NULL THEN
        v_Where := v_Where || ' AND h.NamXuatBan <= ' || p_NamDen;
    END IF;

    v_Sql := v_Sql || v_Where;

    -- Kiểm tra chống SQL Injection với danh sách trắng (Whitelist)
    IF LOWER(p_SapXepTheo) = 'giatien' THEN
        v_Sql := v_Sql || ' ORDER BY h.giatien';
    ELSIF LOWER(p_SapXepTheo) = 'namxuatban' THEN
        v_Sql := v_Sql || ' ORDER BY h.namxuatban';
    ELSE
        v_Sql := v_Sql || ' ORDER BY h.tieude';
    END IF;

    RETURN QUERY EXECUTE v_Sql;
END;
$$;


-- FUNCTION 5: Báo cáo tổng quan chỉ số thư viện
CREATE OR REPLACE FUNCTION fn_BaoCao_TongQuan_ThuVien()
RETURNS TABLE (
    ChiSo VARCHAR(100),
    GiaTri TEXT
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT 'Tổng số đầu mục học liệu'::VARCHAR, COUNT(*)::TEXT FROM HocLieu
    UNION ALL
    SELECT 'Tổng số bản sao vật lý'::VARCHAR, COUNT(*)::TEXT FROM BanSao
    UNION ALL
    SELECT 'Số bản sao đang sẵn sàng'::VARCHAR, COUNT(*)::TEXT FROM BanSao WHERE TrangThai = 'San sang'
    UNION ALL
    SELECT 'Số bản sao đang được mượn'::VARCHAR, COUNT(*)::TEXT FROM BanSao WHERE TrangThai = 'Dang muon'
    UNION ALL
    SELECT 'Tổng số bạn đọc đã đăng ký'::VARCHAR, COUNT(*)::TEXT FROM BanDoc
    UNION ALL
    SELECT 'Số bạn đọc đang bị khóa thẻ'::VARCHAR, COUNT(*)::TEXT FROM BanDoc WHERE TaiKhoanBiKhoa = TRUE
    UNION ALL
    SELECT 'Tổng số lượt mượn đã thực hiện'::VARCHAR, COUNT(*)::TEXT FROM PhieuMuon
    UNION ALL
    SELECT 'Tổng tiền phạt đã thu (VNĐ)'::VARCHAR, to_char(COALESCE(SUM(TienPhat), 0), 'FM999,999,999') FROM PhieuMuon;
END;
$$;


-- --------------------------------------------------------------------
-- PHẦN III: PROCEDURES (THỦ TỤC XỬ LÝ GIAO DỊCH)
-- --------------------------------------------------------------------

-- Thủ tục Trả Sách (Hoàn thiện nghiệp vụ bên cạnh sp_MuonSach)
CREATE OR REPLACE PROCEDURE sp_TraSach(
    p_MaPhieu VARCHAR(20),
    p_GhiChu TEXT DEFAULT NULL
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_TrangThai VARCHAR(50);
BEGIN
    -- Khóa bản ghi phiếu mượn (Pessimistic Locking)
    SELECT TrangThai INTO STRICT v_TrangThai
    FROM PhieuMuon
    WHERE MaPhieu = p_MaPhieu
    FOR UPDATE;

    IF v_TrangThai = 'Da tra' THEN
        RAISE EXCEPTION 'Phiếu mượn % đã được trả trước đó!', p_MaPhieu;
    END IF;

    -- Cập nhật trạng thái phiếu mượn
    -- Lưu ý: Trigger trg_TinhTienPhat và trg_CapNhatTrangThaiBanSao sẽ tự động:
    --   1. Ghi nhận NgayTraThucTe
    --   2. Tính toán tiền phạt trễ hạn nếu có
    --   3. Đồng bộ trạng thái của BanSao về 'San sang'
    UPDATE PhieuMuon
    SET TrangThai = 'Da tra',
        NgayTraThucTe = CURRENT_TIMESTAMP,
        GhiChu = CASE 
            WHEN p_GhiChu IS NOT NULL THEN COALESCE(GhiChu || '; ', '') || p_GhiChu 
            ELSE GhiChu 
        END
    WHERE MaPhieu = p_MaPhieu;

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE EXCEPTION 'Không tìm thấy phiếu mượn %!', p_MaPhieu;
END;
$$;


-- --------------------------------------------------------------------
-- PHẦN IV: TRIGGERS (TỰ ĐỘNG HÓA RÀNG BUỘC VÀ TRẠNG THÁI)
-- --------------------------------------------------------------------

-- TRIGGER 1: Kiểm tra điều kiện mượn sách trước khi tạo phiếu mượn
-- (BEFORE INSERT ON PhieuMuon)
CREATE OR REPLACE FUNCTION fn_trg_KiemTraDieuKienMuon()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_BiKhoa BOOLEAN;
    v_NgayHetHan DATE;
    v_SoSachDangMuon INT;
    v_TrangThaiBanSao VARCHAR(50);
BEGIN
    -- Cùng thứ tự khóa với sp_MuonSach. Đọc lại trạng thái sau khi lấy khóa.
    IF NEW.TrangThai IN ('Dang muon', 'Qua han') THEN
        SELECT TrangThai INTO v_TrangThaiBanSao
        FROM BanSao WHERE MaBanSao = NEW.MaBanSao FOR UPDATE;

        IF NOT FOUND THEN
            RAISE EXCEPTION 'Mã bản sao % không tồn tại!', NEW.MaBanSao;
        END IF;
        IF v_TrangThaiBanSao IS DISTINCT FROM 'San sang' THEN
            RAISE EXCEPTION 'Bản sao % hiện không sẵn sàng để mượn (Trạng thái: %)!',
                NEW.MaBanSao, v_TrangThaiBanSao USING ERRCODE = '55000';
        END IF;
    END IF;

    -- 1. Kiểm tra bạn đọc
    SELECT TaiKhoanBiKhoa, NgayHetHan 
    INTO v_BiKhoa, v_NgayHetHan
    FROM BanDoc
    WHERE MaBanDoc = NEW.MaBanDoc
    FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Mã bạn đọc % không tồn tại trong hệ thống!', NEW.MaBanDoc
            USING ERRCODE = '23503';
    END IF;

    IF v_BiKhoa = TRUE THEN
        RAISE EXCEPTION 'Bạn đọc % đang bị khóa tài khoản, không thể mượn sách!', NEW.MaBanDoc;
    END IF;

    IF v_NgayHetHan IS NOT NULL AND v_NgayHetHan < CURRENT_DATE THEN
        RAISE EXCEPTION 'Thẻ bạn đọc % đã hết hạn vào ngày %!', NEW.MaBanDoc, v_NgayHetHan;
    END IF;

    -- 2. Kiểm tra giới hạn mượn (tối đa 5 cuốn đang mượn)
    SELECT COUNT(*) INTO v_SoSachDangMuon
    FROM PhieuMuon
    WHERE MaBanDoc = NEW.MaBanDoc AND TrangThai IN ('Dang muon', 'Qua han');

    IF v_SoSachDangMuon >= 5 THEN
        RAISE EXCEPTION 'Bạn đọc % đã đạt giới hạn mượn tối đa (5 cuốn)!', NEW.MaBanDoc;
    END IF;

    IF EXISTS (
        SELECT 1 FROM PhieuMuon
        WHERE MaBanDoc = NEW.MaBanDoc
          AND (TrangThai = 'Qua han'
               OR (TrangThai = 'Dang muon' AND NgayHanTra < CURRENT_TIMESTAMP))
    ) THEN
        RAISE EXCEPTION 'Bạn đọc % còn phiếu mượn quá hạn!', NEW.MaBanDoc;
    END IF;

    -- 4. Tự động thiết lập thời hạn trả nếu chưa có (Mặc định 14 ngày)
    IF NEW.NgayHanTra IS NULL THEN
        NEW.NgayHanTra := COALESCE(NEW.NgayMuon, CURRENT_TIMESTAMP) + INTERVAL '14 days';
    END IF;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_KiemTraDieuKienMuon ON PhieuMuon;
CREATE TRIGGER trg_KiemTraDieuKienMuon
BEFORE INSERT ON PhieuMuon
FOR EACH ROW
EXECUTE FUNCTION fn_trg_KiemTraDieuKienMuon();


-- TRIGGER 2: Tự động cập nhật trạng thái BanSao khi mượn hoặc trả sách
-- (AFTER INSERT OR UPDATE ON PhieuMuon)
CREATE OR REPLACE FUNCTION fn_trg_CapNhatTrangThaiBanSao()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        -- Khi tạo phiếu mới ở trạng thái 'Dang muon', tự động đổi BanSao sang 'Dang muon'
        IF NEW.TrangThai IN ('Dang muon', 'Qua han') THEN
            UPDATE BanSao
            SET TrangThai = 'Dang muon'
            WHERE MaBanSao = NEW.MaBanSao;
        END IF;
        RETURN NEW;

    ELSIF TG_OP = 'UPDATE' THEN
        -- Khi trả sách (chuyển sang 'Da tra') -> Đổi bản sao về 'San sang'
        IF NEW.TrangThai = 'Da tra' AND (OLD.TrangThai IS DISTINCT FROM 'Da tra') THEN
            UPDATE BanSao
            SET TrangThai = 'San sang'
            WHERE MaBanSao = NEW.MaBanSao;
        -- Nếu phiếu chuyển lại 'Dang muon'
        ELSIF NEW.TrangThai IN ('Dang muon', 'Qua han') AND OLD.TrangThai = 'Da tra' THEN
            UPDATE BanSao
            SET TrangThai = 'Dang muon'
            WHERE MaBanSao = NEW.MaBanSao;
        END IF;
        RETURN NEW;
    END IF;

    RETURN NULL;
END;
$$;

DROP TRIGGER IF EXISTS trg_CapNhatTrangThaiBanSao ON PhieuMuon;
CREATE TRIGGER trg_CapNhatTrangThaiBanSao
AFTER INSERT OR UPDATE OF TrangThai ON PhieuMuon
FOR EACH ROW
EXECUTE FUNCTION fn_trg_CapNhatTrangThaiBanSao();


-- TRIGGER 3: Tự động tính tiền phạt và ghi nhận thời gian trả thực tế khi trả sách
-- (BEFORE UPDATE ON PhieuMuon)
CREATE OR REPLACE FUNCTION fn_trg_TinhTienPhat()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_SoNgayTre INT;
    v_DonGiaPhat CONSTANT DECIMAL(12, 2) := 5000.00; -- 5,000 VNĐ / ngày trễ
BEGIN
    -- Chỉ kích hoạt khi trạng thái chuyển sang 'Da tra'
    IF NEW.TrangThai = 'Da tra' AND (OLD.TrangThai IS DISTINCT FROM 'Da tra') THEN
        -- 1. Tự động ghi nhận NgayTraThucTe nếu chưa có
        IF NEW.NgayTraThucTe IS NULL THEN
            NEW.NgayTraThucTe := CURRENT_TIMESTAMP;
        END IF;

        -- 2. Kiểm tra trễ hạn và tính tiền phạt
        IF NEW.NgayTraThucTe > NEW.NgayHanTra THEN
            -- Tính số ngày trễ (làm tròn lên theo ngày)
            v_SoNgayTre := CEIL(EXTRACT(EPOCH FROM (NEW.NgayTraThucTe - NEW.NgayHanTra)) / 86400)::INT;
            
            IF v_SoNgayTre > 0 THEN
                NEW.TienPhat := v_SoNgayTre * v_DonGiaPhat;
                NEW.GhiChu := COALESCE(NEW.GhiChu || '; ', '') || 
                              format('Trả trễ %s ngày, tiền phạt: %s VNĐ', v_SoNgayTre, to_char(NEW.TienPhat, 'FM999,999,999'));
            ELSE
                NEW.TienPhat := 0;
            END IF;
        ELSE
            NEW.TienPhat := 0;
        END IF;
    END IF;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_TinhTienPhat ON PhieuMuon;
CREATE TRIGGER trg_TinhTienPhat
BEFORE UPDATE OF TrangThai, NgayTraThucTe ON PhieuMuon
FOR EACH ROW
EXECUTE FUNCTION fn_trg_TinhTienPhat();


-- --------------------------------------------------------------------
-- PHẦN V: KỊCH BẢN KIỂM THỬ (TEST SCRIPT CHO GIẢNG VIÊN VÀ NHÓM)
-- --------------------------------------------------------------------
/*
-- 1. Test hàm thống kê theo chủ đề:
SELECT * FROM fn_ThongKe_HocLieu_TheoChuDe();

-- 2. Test hàm kiểm tra điều kiện mượn:
SELECT fn_KiemTra_CoTheMuon('BD01') AS CoTheMuon;

-- 3. Test hàm tìm kiếm toàn văn GIN Trigram:
SELECT * FROM fn_TimKiem_HocLieu('Database');

-- 4. Test hàm Dynamic SQL:
SELECT * FROM fn_TimKiem_HocLieu_Dong(p_TieuDe := 'Database', p_NamTu := 2015, p_SapXepTheo := 'GiaTien');

-- 5. Test Trigger 1 & 2: Mượn sách thành công, kiểm tra BanSao tự chuyển 'Dang muon'
INSERT INTO PhieuMuon(MaPhieu, MaBanDoc, MaBanSao) VALUES ('PM_TEST_01', 'BD01', 'BS02');
SELECT MaBanSao, TrangThai FROM BanSao WHERE MaBanSao = 'BS02'; -- Kết quả: 'Dang muon'

-- 6. Test Trigger 1 (Chặn mượn sách khi bản sao đã bị mượn):
-- Dòng này sẽ bắn lỗi EXCEPTION: "Bản sao BS02 hiện không sẵn sàng để mượn (Trạng thái: Dang muon)!"
-- INSERT INTO PhieuMuon(MaPhieu, MaBanDoc, MaBanSao) VALUES ('PM_TEST_02', 'BD01', 'BS02');

-- 7. Test Trigger 3 & Procedure Trả sách:
CALL sp_TraSach('PM_TEST_01');
SELECT MaPhieu, TrangThai, NgayTraThucTe, TienPhat, GhiChu FROM PhieuMuon WHERE MaPhieu = 'PM_TEST_01';
SELECT MaBanSao, TrangThai FROM BanSao WHERE MaBanSao = 'BS02'; -- Kết quả: 'San sang'

-- 8. Test View báo cáo:
SELECT * FROM v_BaoCao_MuonTra;
SELECT * FROM fn_BaoCao_TongQuan_ThuVien();
*/
