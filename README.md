# Cơ sở dữ liệu thư viện học liệu đa phương tiện

Đồ án số 3 môn Cơ sở dữ liệu nâng cao, nhóm 5, Trường Đại học Sài Gòn. PostgreSQL triển khai lược đồ quan hệ và giao dịch. ObjectDB chạy phân hệ đối tượng với bộ dữ liệu mẫu riêng.

## Hồ sơ

- [Báo cáo Word hoàn chỉnh](report/BaoCao_CSDLNC_DeTai3.docx)
- [Slide thuyết trình](report/Slide_DeTai3_ThuVienHocLieu.pptx)
- [Biên bản phân công CLO7](report/BienBan_PhanCong_CLO7.docx)
- [Sơ đồ EER](report/Hinh1_SoDoEER.png)
- [Nhật ký thực nghiệm](report/MinhChung_ThucNghiem_DeTai3.txt)

## Khởi chạy trên Windows

Cần Java 17 trở lên, Maven, PostgreSQL 14 trở lên và Python 3. Tài khoản PostgreSQL dùng để dựng demo cần có quyền tạo cơ sở dữ liệu và extension pg_trgm.

~~~powershell
python -m pip install psycopg2-binary faker
$env:LIBRARY_DB_NAME = 'library_management_demo'
$env:LIBRARY_DB_PASSWORD = '<mật khẩu PostgreSQL của bạn>'
python scripts\setup_library_management_db.py
python scripts\generate_mock_data.py
.\mvn.cmd compile
.\mvn.cmd exec:java
~~~

Script setup dựng lại schema public trong database đã chọn. Dùng tên database riêng cho lần demo để giữ dữ liệu của phiên bản cũ. Nếu một script lỗi, giao dịch khởi tạo hoàn tác thay đổi schema và lệnh trả lỗi. Bước sinh dữ liệu mở rộng là tùy chọn. Bản dựng mặc định có bốn loại học liệu, hai bản sao sách, một bạn đọc và một phiếu đã trả.

Java và các script Python dùng chung các biến LIBRARY_DB_HOST, LIBRARY_DB_PORT, LIBRARY_DB_NAME, LIBRARY_DB_USER và LIBRARY_DB_PASSWORD. Nếu bỏ qua, cấu hình demo mặc định là localhost:5432, database library_management, tài khoản postgres và mật khẩu demo 12345. ObjectDB tạo thuvien.odb khi chạy Menu 6 lần đầu.

Nếu chạy SQL thủ công, thực thi 01, 02, 04, 05 và 06 theo thứ tự. Nạp 02 trong **một giao dịch** để ghi đồng thời cha và lớp con. Ví dụ với psql, dùng tùy chọn --single-transaction và -v ON_ERROR_STOP=1 cho từng tệp. Chạy 03 riêng sau khi hoàn tất setup để xem kết quả 12 truy vấn.

## Thiết kế

- HocLieu có bốn lớp con Sach, LuanVan, VideoBaiGiang và TaiLieuDienTu. Cột phân loại ở cha và khóa ngoại ghép bảo đảm đúng loại. Trigger ràng buộc hoãn đến khi commit kiểm tra mỗi cha có một dòng lớp con.
- HocLieu_TacGia và HocLieu_ChuDe biểu diễn hai tập liên kết độc lập. Với HL01, phép nối khôi phục đúng bốn dòng bảng phẳng. Script 06 đối chiếu bằng EXCEPT ALL hai chiều.
- Hai trạng thái Dang muon và Qua han chiếm bản sao. Chỉ mục duy nhất có điều kiện giới hạn mỗi bản sao ở một phiếu đang hiệu lực. Thủ tục mượn và trigger chèn khóa bản sao trước khi kiểm tra trạng thái. Trigger đồng bộ trạng thái khi mượn/trả.
- Tài liệu điện tử lưu định dạng, dung lượng, URL và phạm vi truy cập ở mức bản ghi. Phân quyền theo từng bạn đọc và lưu nhiều tệp trên cùng học liệu nằm ngoài phạm vi bản mẫu.

## Kịch bản demo và tái lập

| Menu | Minh chứng |
| --- | --- |
| 1–3 | Duyệt cây chủ đề, tìm học liệu đa chủ đề, thống kê lượt mượn. |
| 4 | Tạo dữ liệu demo có hai bạn đọc và một bản sao duy nhất. Hai kết nối cùng gọi thủ tục mượn. Kết quả kỳ vọng là một phiên thành công, một phiên bị chặn và một phiếu hiệu lực. |
| 5 | Sửa tình trạng bản sao, chèn phiếu với bạn đọc không tồn tại rồi rollback. Ứng dụng kiểm tra tình trạng ban đầu và số phiếu. |
| 6 | Chạy 5 JPQL trên ObjectDB. Báo cáo đặt cú pháp OQL học thuật cạnh câu JPQL thực thi. |
| 7 | Sinh 20.000 tiêu đề xác định, 200 tiêu đề khớp ILIKE '%Database%'. Chạy cùng truy vấn trước và sau khi tạo GIN trên bảng tạm. Mỗi nhóm có một lượt làm nóng và năm lượt đo, ghi trung vị Execution Time và kế hoạch EXPLAIN (ANALYZE, BUFFERS). |
| 8 | Kiểm thử trigger mượn/trả, savepoint và phạt trễ. Hoàn tác dữ liệu demo khi kết thúc. |
| 9 | In đối chiếu 4NF và tỷ lệ NULL của các thuộc tính lớp con. Script 06 chứa hai kế hoạch truy vấn cùng đầu ra trên bộ bốn học liệu mẫu. |

Để lưu riêng đầu ra Menu 4, 5, 7 và 9 sau khi đã biên dịch, chạy trong PowerShell.

~~~powershell
@('4', '5', '7', '9', '0') | .\mvn.cmd -q exec:java |
    Tee-Object -FilePath ket_qua_demo.txt
~~~

Thời gian đo phụ thuộc máy, dữ liệu và bộ đệm. Khi đối chiếu nhật ký, kiểm tra quy mô mẫu, số dòng khớp, nút thực thi và thứ tự phép đo trước khi so sánh trung vị.

## Thành viên

- Chế Văn Nam
- Nguyễn Thành Nhân
- Hồ Minh Khuyến
- Đặng Hoàng Phúc
- Lý Nguyễn
