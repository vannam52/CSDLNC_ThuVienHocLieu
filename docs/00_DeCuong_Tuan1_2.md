# BÁO CÁO TIẾN ĐỘ TUẦN 1 - 2
**Môn học:** Cơ sở dữ liệu Nâng cao
**Nhóm:** [Tên/Số nhóm của bạn]

---

## 1. Tên đề tài
**Đề tài 3: Thiết kế Cơ sở dữ liệu thư viện học liệu đa phương tiện**

## 2. Câu hỏi nghiên cứu
Dựa trên yêu cầu của đề tài, nhóm đặt ra trọng tâm nghiên cứu cần giải quyết:
> *"Cách biểu diễn các loại học liệu (Sách, Luận văn, Video, Tài liệu số) và các thuộc tính đa trị (Tác giả, Chủ đề) ảnh hưởng thế nào đến vấn đề dư thừa dữ liệu, tốc độ truy vấn và khả năng mở rộng của hệ thống?"*

**Định hướng trả lời (Hướng giải quyết):**
*   **Về dư thừa dữ liệu:** Việc phân rã các thuộc tính đa trị độc lập (Tác giả và Chủ đề) thành các quan hệ riêng biệt đạt dạng chuẩn 4NF (như `HocLieu_TacGia`, `HocLieu_ChuDe`) giúp loại bỏ hoàn toàn sự lặp lại dữ liệu (redundancy) của học liệu.
*   **Về tốc độ truy vấn:** Cách ánh xạ kế thừa quyết định tốc độ. Nếu gom tất cả vào 1 bảng chung (Single Table), truy vấn tổng hợp rất nhanh do không cần dùng lệnh `JOIN`, nhưng tốn dung lượng vì chứa nhiều giá trị `NULL`. Nếu tách bảng Cha-Con (Class Table), việc bảo vệ toàn vẹn dữ liệu rất tốt nhưng khi truy vấn phải `JOIN` nhiều bảng làm giảm tốc độ. CSDL Hướng đối tượng (ObjectDB) giải quyết việc này mượt mà hơn nhờ khả năng truy xuất cấu trúc cây đối tượng trực tiếp bằng OQL mà không cần `JOIN`.
*   **Về khả năng mở rộng:** Cấu trúc phân cấp (Cha - Con) hoặc CSDL Đối tượng mang lại khả năng mở rộng tuyệt vời. Khi thư viện cần bổ sung một loại học liệu mới (Ví dụ: Tạp chí, Podcast), ta chỉ cần tạo thêm một bảng con/lớp con kế thừa từ `Học Liệu` mà không làm phá vỡ hay phải sửa đổi cấu trúc của các dữ liệu cũ.

## 3. Phạm vi đề tài (Scope)
Hệ thống sẽ không ôm đồm quản lý tài chính hay nhân sự, mà chỉ tập trung tối đa vào thiết kế CSDL (Quan hệ & Hướng đối tượng) để quản lý luồng dữ liệu học liệu. Phạm vi cụ thể bao gồm:
*   **Học liệu:** Phân loại và lưu trữ đặc thù cho Sách, Luận văn, Video bài giảng, Tài liệu điện tử.
*   **Thuộc tính đa trị:** Quản lý tập Tác giả và tập Chủ đề độc lập theo quy tắc chuẩn hóa 4NF.
*   **Lưu thông:** Quản lý Bạn đọc, Bản sao (cuốn sách vật lý), Phiếu mượn và quyền truy cập (với tài liệu số).
*   **Giao dịch (Transaction):** Tập trung xử lý tính nguyên tử và kiểm soát đồng thời (Concurrency Control) khi xảy ra tranh chấp mượn sách.

## 4. Tài liệu tham khảo chính
1. Elmasri & Navathe, *Fundamentals of Database Systems* (Bản 7) - Chương 4 (EER), Chương 12 (CSDL Đối tượng), Chương 14-15 (Chuẩn hóa 4NF) và Chương 20-22 (Transaction).
2. Tài liệu PostgreSQL chính thức: Xử lý kế thừa bảng (Table Inheritance) và Mức độ cô lập giao dịch (Transaction Isolation).
3. Tài liệu ObjectDB / JPA: Phục vụ thiết kế CSDL Hướng đối tượng và truy vấn OQL/JPQL.

---

## 5. Kế hoạch và Phân công công việc dự kiến

