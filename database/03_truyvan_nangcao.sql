-- =======================================================
-- FILE 03: CÁC CÂU TRUY VẤN SQL NÂNG CAO
-- Dùng để báo cáo và demo tính năng hệ thống
-- =======================================================

-- 1. TRUY VẤN ĐA HÌNH (POLYMORPHIC QUERY)
-- Lấy danh sách học liệu kèm theo thông tin đặc thù của từng loại (Kế thừa)
-- Giải thích: Dùng LEFT JOIN để nếu nó là Sách thì hiện Số trang, nếu là Video thì hiện Thời lượng.
SELECT 
    h.MaHocLieu, h.TieuDe, h.GiaTien,
    s.NhaXuatBan, s.SoTrang,      -- Chỉ Sách mới có
    v.ThoiLuong, v.DoPhanGiai     -- Chỉ Video mới có
FROM HocLieu h
LEFT JOIN Sach s ON h.MaHocLieu = s.MaHocLieu
LEFT JOIN VideoBaiGiang v ON h.MaHocLieu = v.MaHocLieu;


-- 2. TRUY VẤN THUỘC TÍNH ĐA TRỊ (CHUẨN HÓA 4NF)
-- Tìm các học liệu thuộc NHIỀU HƠN 1 chủ đề (Ví dụ: Sách vừa thuộc 'Toán', vừa thuộc 'Tin học')
SELECT 
    h.MaHocLieu, 
    h.TieuDe, 
    COUNT(hc.MaChuDe) AS SoLuongChuDe
FROM HocLieu h
JOIN HocLieu_ChuDe hc ON h.MaHocLieu = hc.MaHocLieu
GROUP BY h.MaHocLieu, h.TieuDe
HAVING COUNT(hc.MaChuDe) > 1;


-- 3. THỐNG KÊ LƯỢT MƯỢN (GROUP BY & ORDER BY)
-- Thống kê Top 5 Học liệu được mượn nhiều nhất và Tổng tiền phạt thu được từ cuốn đó
SELECT 
    h.MaHocLieu, 
    h.TieuDe, 
    COUNT(p.MaPhieu) AS TongLuotMuon,
    SUM(p.TienPhat) AS TongTienPhat
FROM HocLieu h
JOIN BanSao bs ON h.MaHocLieu = bs.MaHocLieu
JOIN PhieuMuon p ON bs.MaBanSao = p.MaBanSao
GROUP BY h.MaHocLieu, h.TieuDe
ORDER BY TongLuotMuon DESC
LIMIT 5;


-- 4. TRUY VẤN ĐỆ QUY (RECURSIVE CTE) - DUYỆT CÂY CHỦ ĐỀ
-- Lưu ý: Để chạy được tính năng Cây chủ đề (Ví dụ: Khoa học -> Máy tính -> Trí tuệ nhân tạo), 
-- ta cần bổ sung thêm cột "ChuDeCha" vào bảng ChuDe bằng lệnh ALTER dưới đây:

ALTER TABLE ChuDe ADD COLUMN ChuDeCha VARCHAR(20) REFERENCES ChuDe(MaChuDe);


-- Câu truy vấn đệ quy lấy toàn bộ chủ đề phân cấp:
WITH RECURSIVE CayChuDe AS (
    -- Bước 1: Chọn nhánh gốc (Các chủ đề cấp 1 không có cha)
    SELECT MaChuDe, TenChuDe, ChuDeCha, 1 AS CapDo
    FROM ChuDe
    WHERE ChuDeCha IS NULL 
    
    UNION ALL
    
    -- Bước 2: Đệ quy tìm vòng lặp các chủ đề con cấp 2, 3, 4...
    SELECT c.MaChuDe, c.TenChuDe, c.ChuDeCha, ccd.CapDo + 1
    FROM ChuDe c
    JOIN CayChuDe ccd ON c.ChuDeCha = ccd.MaChuDe
)
SELECT * FROM CayChuDe ORDER BY CapDo, TenChuDe;
