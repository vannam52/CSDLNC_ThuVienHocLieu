-- 1. Nạp Tác giả & Chủ đề
INSERT INTO TacGia VALUES 
('TG01', 'A. Silberschatz', 'Yale University', 'avi@yale.edu'),
('TG02', 'R. Elmasri', 'UT Arlington', 'elmasri@uta.edu'),
('TG03', 'Nguyễn Văn A', 'ĐH Sài Gòn', 'nva@sgu.edu.vn');

INSERT INTO ChuDe (MaChuDe, TenChuDe, MoTaChuDe, ChuDeCha) VALUES 
('CD01', 'CNTT', 'Công nghệ thông tin', NULL),
('CD02', 'CSDL', 'Cơ sở dữ liệu', 'CD01'),
('CD03', 'CSDL Nâng cao', 'Chủ đề chuyên sâu CSDL', 'CD02'),
('CD04', 'Lập trình', 'Ngôn ngữ lập trình', 'CD01');

-- 2. Nạp Học liệu & Bảng con
INSERT INTO HocLieu VALUES 
('HL01', 'Database System Concepts', 'Sách giáo trình CSDL', 2020, 'Tiếng Anh', 500000, CURRENT_DATE, 'cover1.jpg'),
('HL02', 'Nghiên cứu CSDL Hướng đối tượng', 'Luận văn thạc sĩ', 2023, 'Tiếng Việt', 0, CURRENT_DATE, 'cover2.jpg'),
('HL03', 'Bài giảng Video SQL Nâng cao', 'Video khóa học', 2024, 'Tiếng Việt', 0, CURRENT_DATE, 'cover3.jpg');

INSERT INTO Sach VALUES ('HL01', '978-0078022159', 'McGraw-Hill', 1376, 7);
INSERT INTO LuanVan VALUES ('HL02', 'GS.TS Trần Văn B', 'ĐH Sài Gòn', 'Thạc sĩ', 'Khoa học máy tính', 9.5);
INSERT INTO VideoBaiGiang VALUES ('HL03', 120, '1080p', 'Tiếng Việt', 1024.50);

-- 3. Nạp liên kết Đa trị (1 Học liệu có nhiều Chủ đề & Tác giả - Chuẩn 4NF)
INSERT INTO HocLieu_TacGia VALUES ('HL01', 'TG01'), ('HL01', 'TG02');
INSERT INTO HocLieu_ChuDe VALUES ('HL01', 'CD02'), ('HL01', 'CD03');

-- 4. Nạp Bản sao & Bạn đọc
INSERT INTO BanSao VALUES 
('BS01', 'HL01', 'San sang', 'Mới', 'Kệ A1-01'),
('BS02', 'HL01', 'San sang', 'Mới', 'Kệ A1-02');

INSERT INTO BanDoc VALUES 
('BD01', 'Thanh Nhàn', 'nhan@gmail.com', '0901234567', '123456', 'Sinh vien', '2026-12-31', FALSE);