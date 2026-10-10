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

    public void start() {
        while (true) {
            System.out.println("\n====================================================");
            System.out.println("   ĐỒ ÁN MÔN CSDL NÂNG CAO - ĐỀ TÀI 3: THƯ VIỆN ");
            System.out.println("====================================================");
            System.out.println("1. [SQL Nâng cao] Tra cứu danh mục chủ đề (WITH RECURSIVE)");
            System.out.println("2. [SQL Nâng cao] Thống kê học liệu đa chủ đề (4NF)");
            System.out.println("3. [Transaction] Mô phỏng mượn sách đồng thời (Locking)");
            System.out.println("4. [Transaction] Mô phỏng Lỗi & Rollback");
            System.out.println("5. [OQL/ObjectDB] Thực thi bộ truy vấn OQL / JPQL");
            System.out.println("6. [Thực nghiệm] Đo hiệu năng chỉ mục GIN Trigram");
            System.out.println("0. Thoát chương trình");
            System.out.println("====================================================");
            System.out.print("Chọn chức năng demo (0-6): ");

            int choice = -1;
            try {
                choice = Integer.parseInt(scanner.nextLine());
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
                    transactionDAO.demoConcurrentBorrow("BS01");
                    break;
                case 4:
                    transactionDAO.demoRollbackAndVerify();
                    break;
                case 5:
                    oqlQueryDAO.executeOqlQueries();
                    break;
                case 6:
                    performanceDAO.runPerformanceExperiment();
                    break;
                case 0:
                    System.out.println("Đã thoát chương trình!");
                    return;
                default:
                    System.out.println("Lựa chọn không hợp lệ!");
            }
        }
    }
}