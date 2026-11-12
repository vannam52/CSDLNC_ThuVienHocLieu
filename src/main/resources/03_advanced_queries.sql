-- ====================================================================
-- ĐỒ ÁN CSDL NÂNG CAO - ĐỀ TÀI 3: THƯ VIỆN HỌC LIỆU ĐA PHƯƠNG TIỆN
-- FILE: 03_advanced_queries.sql
-- MỤC ĐÍCH: BỘ 12 TRUY VẤN SQL NÂNG CAO THEO ĐÚNG CHUẨN ĐẦU RA ĐỀ CƯƠNG
--
-- Yêu cầu đề cương: "khoảng 10–12 truy vấn/nghiệp vụ, có truy vấn con, 
-- kết nối, kết gộp, ràng buộc, thủ tục/hàm, SQL động và đệ quy."
-- ====================================================================

-- --------------------------------------------------------------------
-- NHÓM 1: ĐỆ QUY VÀ KẾT NỐI ĐA HÌNH
-- --------------------------------------------------------------------

-- [Q01] Truy vấn đệ quy duyệt Cây Chủ đề phân cấp (WITH RECURSIVE)
-- Kỹ thuật: Common Table Expressions (CTE) đệ quy, xử lý quan hệ tự tham chiếu 1:N
WITH RECURSIVE CayChuDe AS (
    -- Phần neo (Anchor Member): Lấy các chủ đề gốc (cấp 1)
    SELECT MaChuDe, TenChuDe, ChuDeCha, 1 AS CapDo
    FROM ChuDe 
    WHERE ChuDeCha IS NULL
    UNION ALL
    -- Phần đệ quy (Recursive Member): Nối chủ đề con với cây cha đã duyệt
    SELECT c.MaChuDe, c.TenChuDe, c.ChuDeCha, ccd.CapDo + 1
    FROM ChuDe c 
    JOIN CayChuDe ccd ON c.ChuDeCha = ccd.MaChuDe
)
SELECT MaChuDe, TenChuDe, COALESCE(ChuDeCha, 'Gốc') AS ChuDeCha, CapDo
FROM CayChuDe 
ORDER BY CapDo, TenChuDe;


-- [Q02] Truy vấn Đa hình kết nối nhiều bảng (Polymorphic Outer Join)
-- Kỹ thuật: Class Table Inheritance, LEFT JOIN bảng cha HocLieu với 4 bảng con
SELECT 
    h.MaHocLieu,
    h.TieuDe,
    h.NamXuatBan,
    h.GiaTien,
    CASE 
        WHEN s.MaHocLieu IS NOT NULL THEN 'Sách'
        WHEN l.MaHocLieu IS NOT NULL THEN 'Luận văn'
        WHEN v.MaHocLieu IS NOT NULL THEN 'Video'
        WHEN t.MaHocLieu IS NOT NULL THEN 'Tài liệu điện tử'
        ELSE 'Khác'
    END AS LoaiHocLieu,
    COALESCE(s.ISBN, 'N/A') AS ISBN,
    COALESCE(s.SoTrang::TEXT, 'N/A') AS SoTrang,
    COALESCE(l.DiemBaoVe::TEXT, 'N/A') AS DiemBaoVe,
    COALESCE(v.ThoiLuong::TEXT || ' phút', 'N/A') AS ThoiLuongVideo
FROM HocLieu h
LEFT JOIN Sach s ON h.MaHocLieu = s.MaHocLieu
LEFT JOIN LuanVan l ON h.MaHocLieu = l.MaHocLieu
LEFT JOIN VideoBaiGiang v ON h.MaHocLieu = v.MaHocLieu
LEFT JOIN TaiLieuDienTu t ON h.MaHocLieu = t.MaHocLieu
ORDER BY h.MaHocLieu;


-- --------------------------------------------------------------------
-- NHÓM 2: KẾT NỐI, KẾT GỘP VÀ CHUẨN HÓA 4NF
-- --------------------------------------------------------------------

-- [Q03] Truy vấn lọc nhóm chứng minh chuẩn hóa 4NF (GROUP BY ... HAVING)
-- Kỹ thuật: Tìm các học liệu được phân loại vào NHIỀU HƠN 1 chủ đề (đa trị)
SELECT 
    h.MaHocLieu, 
    h.TieuDe, 
    COUNT(hc.MaChuDe) AS SoLuongChuDe
FROM HocLieu h
JOIN HocLieu_ChuDe hc ON h.MaHocLieu = hc.MaHocLieu
GROUP BY h.MaHocLieu, h.TieuDe
HAVING COUNT(hc.MaChuDe) > 1
ORDER BY SoLuongChuDe DESC, h.TieuDe;


-- [Q04] Thống kê tần suất mượn kết nối 3 bảng (Multi-Table Aggregation)
-- Kỹ thuật: JOIN 3 bảng (HocLieu -> BanSao -> PhieuMuon), COUNT gộp và ORDER BY
SELECT 
    h.MaHocLieu, 
    h.TieuDe, 
    COUNT(p.MaPhieu) AS TongLuotMuon,
    COUNT(DISTINCT b.MaBanSao) AS TongSoBanSaoHienCo
FROM HocLieu h
JOIN BanSao b ON h.MaHocLieu = b.MaHocLieu
LEFT JOIN PhieuMuon p ON b.MaBanSao = p.MaBanSao
GROUP BY h.MaHocLieu, h.TieuDe
ORDER BY TongLuotMuon DESC, h.TieuDe;


-- --------------------------------------------------------------------
-- NHÓM 3: TRUY VẤN CON (SUBQUERIES) VÀ WINDOW FUNCTIONS
-- --------------------------------------------------------------------

-- [Q05] Truy vấn con tương quan phủ định (Correlated Subquery with NOT EXISTS)
-- Nghiệp vụ: Tìm các bạn đọc đã có tài khoản nhưng CHƯA TỪNG mượn cuốn sách nào
SELECT 
    bd.MaBanDoc, 
    bd.HoTen, 
    bd.Email, 
    bd.LoaiTaiKhoan
FROM BanDoc bd
WHERE NOT EXISTS (
    SELECT 1 
    FROM PhieuMuon pm 
    WHERE pm.MaBanDoc = bd.MaBanDoc
)
ORDER BY bd.MaBanDoc;


-- [Q06] Truy vấn con lồng nhau vô hướng (Scalar Subquery with AVG)
-- Nghiệp vụ: Tìm các học liệu có giá tiền CAO HƠN giá tiền trung bình của toàn thư viện
SELECT 
    h.MaHocLieu, 
    h.TieuDe, 
    h.GiaTien,
    ROUND((SELECT AVG(GiaTien) FROM HocLieu WHERE GiaTien > 0), 2) AS GiaTienTrungBinh
FROM HocLieu h
WHERE h.GiaTien > (
    SELECT AVG(GiaTien) 
    FROM HocLieu 
    WHERE GiaTien > 0
)
ORDER BY h.GiaTien DESC;


-- [Q07] Hàm cửa sổ phân tích và xếp hạng (Window Function: DENSE_RANK)
-- Nghiệp vụ: Xếp hạng học liệu theo giá trị tiền trong từng năm xuất bản
SELECT 
    h.MaHocLieu,
    h.TieuDe,
    h.NamXuatBan,
    h.GiaTien,
    DENSE_RANK() OVER (
        PARTITION BY h.NamXuatBan 
        ORDER BY h.GiaTien DESC
    ) AS XepHangGiaTheoNam
FROM HocLieu h
WHERE h.GiaTien > 0
ORDER BY h.NamXuatBan DESC, XepHangGiaTheoNam;


-- --------------------------------------------------------------------
-- NHÓM 4: HÀM STORED FUNCTIONS, DYNAMIC SQL VÀ VIEWS
-- --------------------------------------------------------------------

-- [Q08] Gọi Function trả về bảng thống kê theo chủ đề (TABLE Function)
-- Kỹ thuật: PL/pgSQL Function tính toán tỷ lệ % và tổng giá trị từng nhóm
SELECT 
    MaChuDe, 
    TenChuDe, 
    SoLuongHocLieu, 
    TiLePhanTram, 
    TongGiaTri
FROM fn_ThongKe_HocLieu_TheoChuDe();


-- [Q09] Gọi Function kiểm tra tư cách bạn đọc (Scalar Function with Subquery)
-- Nghiệp vụ: Bẫy kiểm tra tài khoản không bị khóa, chưa hết hạn, < 5 cuốn mượn
SELECT 
    'BD01' AS MaBanDoc, 
    fn_KiemTra_CoTheMuon('BD01') AS DuDieuKienMuon;


-- [Q10] Thực thi Stored Function chứa SQL Động (Dynamic SQL with EXECUTE)
-- Kỹ thuật: Ghép chuỗi SQL động an toàn với whitelist phòng chống SQL Injection
SELECT * 
FROM fn_TimKiem_HocLieu_Dong(
    p_TieuDe := 'Database', 
    p_NamTu := 2018, 
    p_SapXepTheo := 'GiaTien'
);


-- [Q11] Truy vấn View đa hình tổng hợp kèm gom mảng chuỗi (STRING_AGG)
-- Nghiệp vụ: Báo cáo đầy đủ tình trạng tài nguyên gồm danh sách tác giả và chủ đề
SELECT 
    MaHocLieu, 
    TieuDe, 
    LoaiHocLieu, 
    DanhSachTacGia, 
    DanhSachChuDe, 
    TongSoBanSao, 
    SoBanSaoKhaDung
FROM v_HocLieu_DayDu
WHERE TongSoBanSao > 0;


-- --------------------------------------------------------------------
-- NHÓM 5: THIẾT KẾ VẬT LÝ VÀ PHÂN TÍCH KẾ HOẠCH THỰC THI
-- --------------------------------------------------------------------

-- [Q12] Phân tích Kế hoạch Thực thi (Query Execution Plan with EXPLAIN ANALYZE)
-- Kỹ thuật: Đánh giá chi phí (Cost), số block I/O và thời gian thực thi (ms)
-- khi tìm kiếm chuỗi ký tự pattern trên chỉ mục GIN Trigram
EXPLAIN (ANALYZE, BUFFERS)
SELECT MaHocLieu, TieuDe 
FROM HocLieu 
WHERE TieuDe ILIKE '%Database%';
