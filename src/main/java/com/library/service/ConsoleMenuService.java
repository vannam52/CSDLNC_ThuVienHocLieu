package com.library.service;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import com.library.dao.HocLieuDAO;
import com.library.dao.OqlQueryDAO;
import com.library.dao.PerformanceDAO;
import com.library.dao.TransactionDAO;

public class ConsoleMenuService {
    private final Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);
    
    private final HocLieuDAO hocLieuDAO = new HocLieuDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final OqlQueryDAO oqlQueryDAO = new OqlQueryDAO();
    private final PerformanceDAO performanceDAO = new PerformanceDAO();
    private final com.library.dao.PlPgSqlDAO plPgSqlDAO = new com.library.dao.PlPgSqlDAO();
    private final com.library.dao.ThucNghiemDeTai3DAO thucNghiemDeTai3DAO = new com.library.dao.ThucNghiemDeTai3DAO();

    public void start() {
        while (true) {
            System.out.println("\n====================================================");
            System.out.println("   ĐỒ ÁN MÔN CSDL NÂNG CAO - ĐỀ TÀI 3: THƯ VIỆN ");
            System.out.println("====================================================");
            System.out.println("1. [SQL Nâng cao] Tra cứu danh mục chủ đề (WITH RECURSIVE)");
            System.out.println("2. [SQL Nâng cao] Thống kê học liệu đa chủ đề (4NF)");
            System.out.println("3. [SQL Nâng cao] Thống kê lượt mượn học liệu");
            System.out.println("4. [Transaction] Mô phỏng mượn sách đồng thời (Locking)");
            System.out.println("5. [Transaction] Mô phỏng Lỗi & Rollback");
            System.out.println("6. [OQL/ObjectDB] Thực thi bộ truy vấn OQL / JPQL");
            System.out.println("7. [Thực nghiệm] Đo hiệu năng chỉ mục GIN Trigram");
            System.out.println("8. [PL/pgSQL] Demo Triggers, Functions & Procedures");
            System.out.println("9. [Thực nghiệm Đề 3] Minh chứng 4NF & So sánh Kế thừa (Trang 5)");
            System.out.println("10. [SQL Nâng cao] Bộ 12 truy vấn SQL nâng cao đầy đủ (Q01 - Q12)");
            System.out.println("0. Thoát chương trình");
            System.out.println("====================================================");
            System.out.print("Chọn chức năng demo (0-10): ");

            int choice = -1;
            try {
                if (!scanner.hasNextLine()) {
                    System.out.println("Đã kết thúc phiên làm việc.");
                    return;
                }
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;
                choice = Integer.parseInt(line);
            } catch (Exception e) {
                System.out.println("Lựa chọn không hợp lệ!");
                continue;
            }

            switch (choice) {
                case 1:
                    hocLieuDAO.getCayChuDe();
                    break;
                case 2:
                    hocLieuDAO.getHocLieuDaChuDe();
                    break;
                case 3:
                    hocLieuDAO.thongKeLuotMuon();
                    break;
                case 4:
                    transactionDAO.demoConcurrentBorrow();
                    break;
                case 5:
                    transactionDAO.demoRollbackAndVerify();
                    break;
                case 6:
                    oqlQueryDAO.executeOqlQueries();
                    break;
                case 7:
                    performanceDAO.runPerformanceExperiment();
                    break;
                case 8:
                    plPgSqlDAO.demoTriggersAndFunctions();
                    break;
                case 9:
                    thucNghiemDeTai3DAO.runDemo();
                    break;
                case 10:
                    showAdvancedQueriesMenu();
                    break;
                case 0:
                    System.out.println("Đã thoát chương trình!");
                    return;
                default:
                    System.out.println("Lựa chọn không hợp lệ!");
            }
        }
    }

    private void showAdvancedQueriesMenu() {
        while (true) {
            System.out.println("\n----------------------------------------------------");
            System.out.println("   BỘ 12 TRUY VẤN SQL NÂNG CAO (03_advanced_queries)");
            System.out.println("----------------------------------------------------");
            System.out.println(" 1. [Q01] Cây chủ đề phân cấp (WITH RECURSIVE)");
            System.out.println(" 2. [Q02] Truy vấn Đa hình 4 lớp con (LEFT JOIN)");
            System.out.println(" 3. [Q03] Học liệu đa chủ đề chứng minh 4NF (HAVING)");
            System.out.println(" 4. [Q04] Thống kê tần suất mượn kết nối 3 bảng");
            System.out.println(" 5. [Q05] Bạn đọc chưa từng mượn sách (NOT EXISTS)");
            System.out.println(" 6. [Q06] Học liệu giá cao hơn trung bình (Scalar AVG)");
            System.out.println(" 7. [Q07] Xếp hạng giá học liệu theo năm (DENSE_RANK)");
            System.out.println(" 8. [Q08] Thống kê theo chủ đề (fn_ThongKe_HocLieu_TheoChuDe)");
            System.out.println(" 9. [Q09] Kiểm tra tư cách mượn (fn_KiemTra_CoTheMuon)");
            System.out.println("10. [Q10] Tìm kiếm động (fn_TimKiem_HocLieu_Dong)");
            System.out.println("11. [Q11] Báo cáo tổng hợp từ View (v_HocLieu_DayDu)");
            System.out.println("12. [Q12] Phân tích kế hoạch thực thi (EXPLAIN ANALYZE)");
            System.out.println("13. Chạy tất cả 12 truy vấn (Q01 - Q12)");
            System.out.println(" 0. Quay lại menu chính");
            System.out.println("----------------------------------------------------");
            System.out.print("Chọn truy vấn (0-13): ");

            if (!scanner.hasNextLine()) {
                return;
            }
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;

            int subChoice;
            try {
                subChoice = Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Lựa chọn không hợp lệ!");
                continue;
            }

            switch (subChoice) {
                case 1 -> hocLieuDAO.getCayChuDe();
                case 2 -> hocLieuDAO.getHocLieuDaHinh();
                case 3 -> hocLieuDAO.getHocLieuDaChuDe();
                case 4 -> hocLieuDAO.thongKeTanSuatMuonDayDu();
                case 5 -> hocLieuDAO.getBanDocChuaMuon();
                case 6 -> hocLieuDAO.getHocLieuGiaCaoHonTB();
                case 7 -> hocLieuDAO.getXepHangGiaTheoNam();
                case 8 -> hocLieuDAO.getThongKeTheoChuDeFunction();
                case 9 -> {
                    System.out.print("Nhập mã bạn đọc (Enter để dùng mặc định BD01): ");
                    String maBD = scanner.nextLine().trim();
                    if (maBD.isEmpty()) maBD = "BD01";
                    hocLieuDAO.kiemTraTuCachMuon(maBD);
                }
                case 10 -> {
                    System.out.print("Nhập từ khóa tiêu đề (Enter = Database): ");
                    String kw = scanner.nextLine().trim();
                    if (kw.isEmpty()) kw = "Database";

                    System.out.print("Nhập năm xuất bản từ (Enter = 2018): ");
                    String namStr = scanner.nextLine().trim();
                    int namTu = 2018;
                    if (!namStr.isEmpty()) {
                        try {
                            namTu = Integer.parseInt(namStr);
                        } catch (NumberFormatException e) {
                            System.out.println("Năm không hợp lệ, dùng mặc định 2018.");
                        }
                    }

                    System.out.print("Sắp xếp theo (TieuDe/NamXuatBan/GiaTien, Enter = GiaTien): ");
                    String sort = scanner.nextLine().trim();
                    if (sort.isEmpty()) sort = "GiaTien";

                    hocLieuDAO.timKiemHocLieuDong(kw, namTu, sort);
                }
                case 11 -> hocLieuDAO.getViewHocLieuDayDu();
                case 12 -> hocLieuDAO.phanTichExplainGin();
                case 13 -> runAll12Queries();
                case 0 -> {
                    return;
                }
                default -> System.out.println("Lựa chọn không hợp lệ!");
            }
        }
    }

    private void runAll12Queries() {
        hocLieuDAO.getCayChuDe();
        hocLieuDAO.getHocLieuDaHinh();
        hocLieuDAO.getHocLieuDaChuDe();
        hocLieuDAO.thongKeTanSuatMuonDayDu();
        hocLieuDAO.getBanDocChuaMuon();
        hocLieuDAO.getHocLieuGiaCaoHonTB();
        hocLieuDAO.getXepHangGiaTheoNam();
        hocLieuDAO.getThongKeTheoChuDeFunction();
        hocLieuDAO.kiemTraTuCachMuon("BD01");
        hocLieuDAO.timKiemHocLieuDong("Database", 2018, "GiaTien");
        hocLieuDAO.getViewHocLieuDayDu();
        hocLieuDAO.phanTichExplainGin();
    }
}
