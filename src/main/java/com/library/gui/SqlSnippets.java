package com.library.gui;

/**
 * Kho chứa các đoạn mã SQL / PL/pgSQL / JPQL chính xác của từng Tab.
 * Phục vụ tính năng xem nhanh mã nguồn SQL khi thuyết trình trước Hội đồng.
 */
public final class SqlSnippets {

    private SqlSnippets() { }

    public static final String TAB1_4NF_INHERITANCE = """
            -- ====================================================================
            -- 1. CHUẨN HÓA 4NF (LOẠI BỎ PHỤ THUỘC ĐA TRỊ)
            -- ====================================================================
            -- Bảng phẳng ban đầu vi phạm 4NF (HL01 có 2 tác giả x 2 chủ đề = 4 dòng lặp)
            -- Phụ thuộc đa trị: MaHocLieu ->> MaTacGia  và  MaHocLieu ->> MaChuDe
            
            -- Phân rã thành 2 quan hệ N:M độc lập:
            -- HocLieu_TacGia (MaHocLieu, MaTacGia)
            -- HocLieu_ChuDe  (MaHocLieu, MaChuDe)
            
            -- Khôi phục bằng NATURAL JOIN:
            SELECT t.MaHocLieu, t.MaTacGia, c.MaChuDe
            FROM HocLieu_TacGia t
            NATURAL JOIN HocLieu_ChuDe c;
            
            -- Kiểm chứng phép nối bảo toàn không mất mát (Lossless Join) bằng EXCEPT ALL 2 chiều:
            -- (Kết quả trả về chính xác 0 dòng chênh lệch)
            (SELECT * FROM HocLieu_ChuaChuanHoa_4NF
             EXCEPT ALL
             SELECT t.MaHocLieu, t.MaTacGia, c.MaChuDe
             FROM HocLieu_TacGia t NATURAL JOIN HocLieu_ChuDe c)
            UNION ALL
            (SELECT t.MaHocLieu, t.MaTacGia, c.MaChuDe
             FROM HocLieu_TacGia t NATURAL JOIN HocLieu_ChuDe c
             EXCEPT ALL
             SELECT * FROM HocLieu_ChuaChuanHoa_4NF);
            
            -- ====================================================================
            -- 2. ĐẾM Ô NULL TRONG SINGLE TABLE VS JOINED TABLE
            -- ====================================================================
            -- Single Table gom tất cả 17 thuộc tính của 4 lớp con vào 1 bảng duy nhất:
            SELECT MaHocLieu, LoaiHocLieu,
                   (CASE WHEN ISBN IS NULL THEN 1 ELSE 0 END +
                    CASE WHEN NhaXuatBan IS NULL THEN 1 ELSE 0 END +
                    CASE WHEN SoTrang IS NULL THEN 1 ELSE 0 END +
                    CASE WHEN LanTaiBan IS NULL THEN 1 ELSE 0 END +
                    CASE WHEN NguoiHuongDan IS NULL THEN 1 ELSE 0 END +
                    CASE WHEN Truong IS NULL THEN 1 ELSE 0 END +
                    CASE WHEN CapBaoVe IS NULL THEN 1 ELSE 0 END +
                    CASE WHEN ChuyenNganh IS NULL THEN 1 ELSE 0 END +
                    CASE WHEN DiemBaoVe IS NULL THEN 1 ELSE 0 END +
                    CASE WHEN ThoiLuong IS NULL THEN 1 ELSE 0 END +
                    CASE WHEN DoPhanGiai IS NULL THEN 1 ELSE 0 END +
                    CASE WHEN NgonNguPhuDe IS NULL THEN 1 ELSE 0 END +
                    CASE WHEN DungLuongMB_Video IS NULL THEN 1 ELSE 0 END +
                    CASE WHEN DinhDang IS NULL THEN 1 ELSE 0 END +
                    CASE WHEN DungLuongMB_TLDT IS NULL THEN 1 ELSE 0 END +
                    CASE WHEN QuyenTruyCap IS NULL THEN 1 ELSE 0 END +
                    CASE WHEN URL_LienKet IS NULL THEN 1 ELSE 0 END) AS SoCotLopConBiNull
            FROM HocLieu_SingleTable;
            -- Tổng cộng: 51 / 68 ô NULL (Lãng phí 75.00% bộ nhớ).
            -- Joined Table giải quyết triệt để: Tách 4 bảng con, 0 ô NULL dư thừa.
            """;

    public static final String TAB2_CONCURRENCY = """
            -- ====================================================================
            -- THỦ TỤC MƯỢN SÁCH & KHÓA DÒNG BẢO VỆ GIAO DỊCH ĐỒNG THỜI
            -- Tệp nguồn: 04_transaction_procedure.sql
            -- ====================================================================
            CREATE OR REPLACE PROCEDURE sp_MuonSach(
                p_MaPhieu VARCHAR(20),
                p_MaBanDoc VARCHAR(20),
                p_MaBanSao VARCHAR(20)
            )
            LANGUAGE plpgsql
            AS $$
            DECLARE
                v_TrangThai VARCHAR(50);
            BEGIN
                -- 1. Khóa bản ghi bản sao bằng FOR UPDATE để chống tranh chấp đồng thời (Race Condition)
                SELECT TrangThai
                INTO STRICT v_TrangThai
                FROM BanSao
                WHERE MaBanSao = p_MaBanSao
                FOR UPDATE;
            
                -- 2. Kiểm tra điều kiện khả dụng
                IF v_TrangThai IS DISTINCT FROM 'San sang' THEN
                    RAISE EXCEPTION 'Bản sao % hiện không khả dụng để mượn (Trạng thái: %)!', p_MaBanSao, v_TrangThai
                        USING ERRCODE = '55000';
                END IF;
            
                -- 3. Ghi nhận phiếu mượn (14 ngày mặc định)
                INSERT INTO PhieuMuon(MaPhieu, MaBanDoc, MaBanSao, NgayMuon, NgayHanTra, TrangThai)
                VALUES (p_MaPhieu, p_MaBanDoc, p_MaBanSao, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '14 days', 'Dang muon');
            
            EXCEPTION
                WHEN NO_DATA_FOUND THEN
                    RAISE EXCEPTION 'Không tìm thấy mã bản sao %!', p_MaBanSao;
            END;
            $$;
            
            -- ====================================================================
            -- CHỈ MỤC DUY NHẤT CÓ ĐIỀU KIỆN (PARTIAL UNIQUE INDEX)
            -- Đảm bảo mỗi bản sao chỉ có tối đa 1 phiếu mượn còn hiệu lực
            -- ====================================================================
            CREATE UNIQUE INDEX uq_phieumuon_bansao_dang_hieu_luc
            ON PhieuMuon (MaBanSao) WHERE TrangThai IN ('Dang muon', 'Qua han');
            """;

    public static final String TAB3_GIN_TRIGRAM = """
            -- ====================================================================
            -- CHỈ MỤC GIN TRIGRAM TỐI ƯU HÓA TÌM KIẾM TOÀN VĂN / CHUỖI CON
            -- Tệp nguồn: 01_schema.sql & 05_triggers_and_functions.sql
            -- ====================================================================
            
            -- 1. Kích hoạt Extension pg_trgm (PostgreSQL Trigram Matching)
            CREATE EXTENSION IF NOT EXISTS pg_trgm;
            
            -- 2. Tạo chỉ mục GIN với lớp toán tử gin_trgm_ops trên cột TieuDe
            CREATE INDEX idx_hoclieu_tieude_trgm 
            ON HocLieu USING gin (TieuDe gin_trgm_ops);
            
            -- 3. Phân tích Kế hoạch Thực thi (Query Execution Plan)
            EXPLAIN (ANALYZE, BUFFERS)
            SELECT MaHocLieu, TieuDe 
            FROM demo_gin 
            WHERE TieuDe ILIKE '%Database%';
            
            -- Kế hoạch trước khi có GIN:
            -- -> Seq Scan (Quét tuần tự cả 20.000 dòng trên đĩa) ~ 15 ms
            
            -- Kế hoạch sau khi có GIN:
            -- -> Bitmap Index Scan: Quét trên chỉ mục GIN để tạo Bitmap trong RAM
            -- -> Bitmap Heap Scan: Nhảy trực tiếp đến các khối đĩa có bit = 1
            -- => Thời gian giảm xuống chỉ còn ~ 0.4 ms (nhanh hơn ~30 lần)!
            """;

    public static final String TAB4_OBJECTDB_OQL = """
            // ====================================================================
            // 5 TRUY VẤN OQL / JPQL TRÊN CSDL HƯỚNG ĐỐI TƯỢNG OBJECTDB
            // Tệp nguồn: OqlQueryDAO.java
            // ====================================================================
            
            // [Truy vấn 1] Lọc lớp con Sách có số trang > 500
            TypedQuery<Sach> q1 = em.createQuery(
                "SELECT s FROM Sach s WHERE s.soTrang > :soTrangMin", Sach.class);
            q1.setParameter("soTrangMin", 500);
            
            // [Truy vấn 2] Điều hướng quan hệ đối tượng: Học liệu có nhiều tác giả
            TypedQuery<HocLieu> q2 = em.createQuery(
                "SELECT h FROM HocLieu h WHERE SIZE(h.danhSachTacGia) > 1", HocLieu.class);
            
            // [Truy vấn 3] Tìm phiếu mượn theo tên Bạn Đọc
            TypedQuery<PhieuMuon> q3 = em.createQuery(
                "SELECT pm FROM PhieuMuon pm WHERE pm.banDoc.hoTen = :hoTen", PhieuMuon.class);
            q3.setParameter("hoTen", "Lê Thị Hoa");
            
            // [Truy vấn 4] Gom nhóm đối tượng: Đếm số lượng học liệu theo từng Chủ đề
            TypedQuery<Object[]> q4 = em.createQuery(
                "SELECT c.tenChuDe, COUNT(h) FROM HocLieu h JOIN h.danhSachChuDe c GROUP BY c.tenChuDe", Object[].class);
            
            // [Truy vấn 5] Lọc lớp con Luận văn có điểm bảo vệ xuất sắc (>= 9.0)
            TypedQuery<LuanVan> q5 = em.createQuery(
                "SELECT lv FROM LuanVan lv WHERE lv.diemBaoVe >= :diemMin", LuanVan.class);
            q5.setParameter("diemMin", 9.0);
            """;

    public static final String TAB5_RECURSIVE_TREE = """
            -- ====================================================================
            -- TRUY VẤN ĐỆ QUY DUYỆT CÂY CHỦ ĐỀ PHÂN CẤP (WITH RECURSIVE)
            -- Tệp nguồn: 03_advanced_queries.sql [Q01]
            -- ====================================================================
            WITH RECURSIVE CayChuDe AS (
                -- 1. Anchor Member: Lấy các chủ đề gốc (cấp 1 - không có cha)
                SELECT 
                    MaChuDe, 
                    TenChuDe, 
                    ChuDeCha, 
                    1 AS CapDo
                FROM ChuDe 
                WHERE ChuDeCha IS NULL
                
                UNION ALL
                
                -- 2. Recursive Member: Nối đệ quy chủ đề con với cây cha đã duyệt
                SELECT 
                    c.MaChuDe, 
                    c.TenChuDe, 
                    c.ChuDeCha, 
                    ccd.CapDo + 1
                FROM ChuDe c 
                JOIN CayChuDe ccd ON c.ChuDeCha = ccd.MaChuDe
            )
            SELECT 
                MaChuDe, 
                TenChuDe, 
                COALESCE(ChuDeCha, '(Gốc)') AS ChuDeCha, 
                CapDo
            FROM CayChuDe 
            ORDER BY CapDo, TenChuDe;
            
            -- ====================================================================
            -- THỐNG KÊ HỌC LIỆU ĐA CHỦ ĐỀ (GROUP BY ... HAVING)
            -- Tệp nguồn: 03_advanced_queries.sql [Q03]
            -- ====================================================================
            SELECT 
                h.MaHocLieu, 
                h.TieuDe, 
                COUNT(hc.MaChuDe) AS SoLuongChuDe
            FROM HocLieu h
            JOIN HocLieu_ChuDe hc ON h.MaHocLieu = hc.MaHocLieu
            GROUP BY h.MaHocLieu, h.TieuDe
            HAVING COUNT(hc.MaChuDe) > 1;
            """;

    public static final String TAB6_PLPGSQL_TRIGGERS = """
            -- ====================================================================
            -- BỘ THỦ TỤC, TRIGGER & VIEW QUẢN LÝ MƯỢN TRẢ (PL/pgSQL)
            -- Tệp nguồn: 05_triggers_and_functions.sql
            -- ====================================================================
            
            -- 1. THỦ TỤC TRẢ SÁCH (sp_TraSach)
            CREATE OR REPLACE PROCEDURE sp_TraSach(
                p_MaPhieu VARCHAR(20),
                p_GhiChu TEXT DEFAULT NULL
            )
            LANGUAGE plpgsql
            AS $$
            DECLARE
                v_TrangThai VARCHAR(50);
            BEGIN
                SELECT TrangThai INTO STRICT v_TrangThai
                FROM PhieuMuon WHERE MaPhieu = p_MaPhieu FOR UPDATE;
            
                IF v_TrangThai = 'Da tra' THEN
                    RAISE EXCEPTION 'Phiếu mượn % đã được trả trước đó!', p_MaPhieu;
                END IF;
            
                UPDATE PhieuMuon
                SET TrangThai = 'Da tra',
                    NgayTraThucTe = CURRENT_TIMESTAMP,
                    GhiChu = CASE WHEN p_GhiChu IS NOT NULL THEN COALESCE(GhiChu || '; ', '') || p_GhiChu ELSE GhiChu END
                WHERE MaPhieu = p_MaPhieu;
            END;
            $$;
            
            -- 2. TRIGGER TỰ ĐỘNG TÍNH TIỀN PHẠT TRỄ HẠN (trg_TinhTienPhat)
            -- Đơn giá: 5.000 VNĐ / ngày trễ
            CREATE OR REPLACE FUNCTION fn_trg_TinhTienPhat()
            RETURNS TRIGGER LANGUAGE plpgsql AS $$
            DECLARE
                v_SoNgayTre INT := 0;
                v_DonGiaPhat CONSTANT DECIMAL(12, 2) := 5000.00;
            BEGIN
                IF NEW.TrangThai = 'Da tra' AND OLD.TrangThai IS DISTINCT FROM 'Da tra' THEN
                    IF NEW.NgayTraThucTe IS NULL THEN
                        NEW.NgayTraThucTe := CURRENT_TIMESTAMP;
                    END IF;
                    IF NEW.NgayTraThucTe > NEW.NgayHanTra THEN
                        v_SoNgayTre := CEIL(EXTRACT(EPOCH FROM (NEW.NgayTraThucTe - NEW.NgayHanTra)) / 86400)::INT;
                        NEW.TienPhat := v_SoNgayTre * v_DonGiaPhat;
                        NEW.GhiChu := COALESCE(NEW.GhiChu || '; ', '') || 'Trả trễ ' || v_SoNgayTre || ' ngày. Phạt: ' || TO_CHAR(NEW.TienPhat, 'FM999,999,999') || ' VNĐ';
                    ELSE
                        NEW.TienPhat := 0;
                    END IF;
                END IF;
                RETURN NEW;
            END;
            $$;
            
            -- 3. TRIGGER TỰ ĐỘNG ĐỒNG BỘ TRẠNG THÁI BẢN SAO (trg_CapNhatTrangThaiBanSao)
            -- Khi mượn -> Đổi bản sao sang 'Dang muon'
            -- Khi trả  -> Tự động đưa bản sao về 'San sang'
            CREATE OR REPLACE FUNCTION fn_trg_CapNhatTrangThaiBanSao()
            RETURNS TRIGGER LANGUAGE plpgsql AS $$
            BEGIN
                IF TG_OP = 'INSERT' AND NEW.TrangThai IN ('Dang muon', 'Qua han') THEN
                    UPDATE BanSao SET TrangThai = 'Dang muon' WHERE MaBanSao = NEW.MaBanSao;
                ELSIF TG_OP = 'UPDATE' AND NEW.TrangThai = 'Da tra' THEN
                    UPDATE BanSao SET TrangThai = 'San sang' WHERE MaBanSao = NEW.MaBanSao;
                END IF;
                RETURN NEW;
            END;
            $$;
            
            -- 4. VIEW BÁO CÁO TỔNG HỢP MƯỢN TRẢ (v_BaoCao_MuonTra)
            CREATE OR REPLACE VIEW v_BaoCao_MuonTra AS
            SELECT 
                pm.MaPhieu, bd.HoTen AS TenBanDoc, hl.TieuDe AS TenHocLieu,
                pm.TrangThai,
                CASE 
                    WHEN pm.TrangThai = 'Dang muon' AND CURRENT_TIMESTAMP > pm.NgayHanTra 
                        THEN GREATEST(0, CEIL(EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - pm.NgayHanTra)) / 86400)::INT)
                    WHEN pm.NgayTraThucTe IS NOT NULL AND pm.NgayTraThucTe > pm.NgayHanTra 
                        THEN GREATEST(0, CEIL(EXTRACT(EPOCH FROM (pm.NgayTraThucTe - pm.NgayHanTra)) / 86400)::INT)
                    ELSE 0
                END AS SoNgayTre,
                pm.TienPhat, pm.GhiChu
            FROM PhieuMuon pm
            JOIN BanDoc bd ON pm.MaBanDoc = bd.MaBanDoc
            JOIN BanSao bs ON pm.MaBanSao = bs.MaBanSao
            JOIN HocLieu hl ON bs.MaHocLieu = hl.MaHocLieu;
            """;
}
