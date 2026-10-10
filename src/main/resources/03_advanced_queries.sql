-- 1. Truy vấn Đa hình (LEFT JOIN lấy thông tin đặc thù của từng lớp con)
SELECT h.MaHocLieu, h.TieuDe, h.GiaTien, s.NhaXuatBan, s.SoTrang, v.ThoiLuong
FROM HocLieu h
LEFT JOIN Sach s ON h.MaHocLieu = s.MaHocLieu
LEFT JOIN VideoBaiGiang v ON h.MaHocLieu = v.MaHocLieu;

-- 2. Truy vấn chứng minh 4NF (Học liệu thuộc > 1 chủ đề)
SELECT h.MaHocLieu, h.TieuDe, COUNT(hc.MaChuDe) AS SoLuongChuDe
FROM HocLieu h
JOIN HocLieu_ChuDe hc ON h.MaHocLieu = hc.MaHocLieu
GROUP BY h.MaHocLieu, h.TieuDe
HAVING COUNT(hc.MaChuDe) > 1;

-- 3. Truy vấn đệ quy duyệt Cây Chủ Đề (WITH RECURSIVE)
WITH RECURSIVE CayChuDe AS (
    SELECT MaChuDe, TenChuDe, ChuDeCha, 1 AS CapDo
    FROM ChuDe WHERE ChuDeCha IS NULL
    UNION ALL
    SELECT c.MaChuDe, c.TenChuDe, c.ChuDeCha, ccd.CapDo + 1
    FROM ChuDe c JOIN CayChuDe ccd ON c.ChuDeCha = ccd.MaChuDe
)
SELECT * FROM CayChuDe ORDER BY CapDo, TenChuDe;

-- 4. Phân tích Kế hoạch Thực thi (EXPLAIN ANALYZE cho báo cáo)
EXPLAIN ANALYZE 
SELECT * FROM HocLieu WHERE TieuDe LIKE '%Database%';