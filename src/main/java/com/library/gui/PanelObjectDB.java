package com.library.gui;

import com.library.dao.OqlQueryDAO;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Tab 4 — ObjectDB & OQL/JPQL.
 * Chạy 5 bài toán OQL/JPQL trên ObjectDB và hiển thị output dạng terminal.
 */
public class PanelObjectDB extends JPanel {

    private final JTextArea areaOutput = new JTextArea();
    private final JLabel lblStatus = new JLabel("Nhấn nút để thực thi 5 bài toán trên ObjectDB.");
    private JButton btnRun;

    public JButton getBtnRun() { return btnRun; }

    public PanelObjectDB() {
        super(new BorderLayout(0, 12));
        setBackground(MainGui.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        buildUI();
    }

    private void buildUI() {
        // Toolbar
        btnRun = MainGui.makeButton("Chạy 5 OQL/JPQL Queries trên ObjectDB", MainGui.ACCENT);
        btnRun.addActionListener(e -> runOQL());

        JButton btnClear = MainGui.makeButton("Xoá màn hình", new Color(0x64748B));
        btnClear.addActionListener(e -> { areaOutput.setText(""); lblStatus.setText("Đã xoá output."); });

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        toolbar.setOpaque(false);
        toolbar.add(btnRun);
        toolbar.add(btnClear);
        lblStatus.setFont(MainGui.FONT_BOLD);
        lblStatus.setForeground(new Color(0x64748B));
        toolbar.add(lblStatus);

        // Output area
        JScrollPane scrollOutput = MainGui.makeOutputArea(areaOutput);
        areaOutput.setText("Thực nghiệm CSDL Hướng đối tượng (ObjectDB):\n" +
                "• ObjectDB lưu trữ đối tượng Java trực tiếp vào tập tin thuvien.odb.\n" +
                "• OQL/JPQL khai thác trực tiếp tính kế thừa, tập hợp đa trị và path expression\n" +
                "  mà SQL quan hệ cần nhiều phép JOIN phức tạp để đạt được kết quả tương đương.\n\n" +
                "[Lưu ý]: ObjectDB chỉ cho phép 1 JVM mở tập tin thuvien.odb tại một thời điểm.\n" +
                "Nếu menu console đang chạy, hãy thoát trước khi nhấn nút này.\n");

        // Bảng 5 bài toán
        String[] cols = {"#", "Bài toán", "Cú pháp OQL / JPQL", "Ưu điểm so với SQL quan hệ"};
        Object[][] rows = {
            {"1", "Kế thừa đối tượng", "SELECT s FROM Sach s WHERE s.soTrang > 500",
             "Không cần JOIN bảng HocLieu — đa hình đối tượng tự nhiên"},
            {"2", "Dữ liệu đa trị (4NF)", "SELECT h FROM HocLieu h WHERE SIZE(h.danhSachTacGia) > 1",
             "Hàm SIZE() thao tác trên Collection thay cho GROUP BY + HAVING"},
            {"3", "Path Expression", "SELECT p FROM PhieuMuon p WHERE p.banDoc.hoTen = :hoTen",
             "Điều hướng p.banDoc.hoTen thay cho 3 lần JOIN quan hệ"},
            {"4", "Thống kê gom nhóm", "SELECT c.tenChuDe, COUNT(h) FROM HocLieu h JOIN h.danhSachChuDe c GROUP BY c.tenChuDe",
             "JOIN trực tiếp trên collection danhSachChuDe nhúng trong Object"},
            {"5", "Truy vấn lớp con", "SELECT l FROM LuanVan l WHERE l.diemBaoVe >= 9.0",
             "Lọc trực tiếp trên lớp con LuanVan và gọi phương thức isXuatSac()"},
        };
        JTable tblQueries = MainGui.makeStyledTable(cols, rows);
        tblQueries.getColumnModel().getColumn(0).setPreferredWidth(30);
        tblQueries.getColumnModel().getColumn(1).setPreferredWidth(160);
        tblQueries.getColumnModel().getColumn(2).setPreferredWidth(380);
        tblQueries.getColumnModel().getColumn(3).setPreferredWidth(230);
        JScrollPane scrollTable = new JScrollPane(tblQueries);
        scrollTable.setBorder(BorderFactory.createLineBorder(MainGui.BORDER_COL));
        scrollTable.setPreferredSize(new Dimension(0, 165));

        JPanel tableCard = new JPanel(new BorderLayout(0, 6));
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainGui.BORDER_COL),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        JLabel tblTitle = new JLabel("Danh sách 5 truy vấn OQL/JPQL minh chứng đề tài");
        tblTitle.setFont(MainGui.FONT_BOLD);
        tblTitle.setForeground(MainGui.ACCENT);
        tableCard.add(tblTitle,     BorderLayout.NORTH);
        tableCard.add(scrollTable,  BorderLayout.CENTER);

        JPanel outputCard = new JPanel(new BorderLayout(0, 6));
        outputCard.setBackground(Color.WHITE);
        outputCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainGui.BORDER_COL),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        JLabel outTitle = new JLabel("Kết quả thực thi thực tế trên ObjectDB");
        outTitle.setFont(MainGui.FONT_BOLD);
        outTitle.setForeground(MainGui.ACCENT);
        outputCard.add(outTitle,    BorderLayout.NORTH);
        outputCard.add(scrollOutput, BorderLayout.CENTER);

        // Split pane
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableCard, outputCard);
        split.setResizeWeight(0.35);
        split.setBorder(null);
        split.setDividerSize(6);

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setOpaque(false);
        content.add(MainGui.sectionLabel("Thực nghiệm 4: ObjectDB & OQL/JPQL — Mô hình Hướng đối tượng vs Quan hệ"), BorderLayout.NORTH);
        content.add(split, BorderLayout.CENTER);

        add(toolbar, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
    }

    private void runOQL() {
        btnRun.setEnabled(false);
        lblStatus.setText("Đang kết nối ObjectDB và thực thi truy vấn...");
        lblStatus.setForeground(MainGui.WARNING);
        areaOutput.setText("Đang chạy...\n");

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override protected String doInBackground() {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(bos, true, StandardCharsets.UTF_8);
                PrintStream oldOut = System.out;
                PrintStream oldErr = System.err;
                System.setOut(ps);
                System.setErr(ps);
                try {
                    new OqlQueryDAO().executeOqlQueries();
                } finally {
                    System.setOut(oldOut);
                    System.setErr(oldErr);
                    ps.flush();
                }
                return bos.toString(StandardCharsets.UTF_8);
            }

            @Override protected void done() {
                try {
                    String out = get();
                    areaOutput.setText(out);
                    areaOutput.setCaretPosition(0);
                    boolean success = out.contains("KẾT LUẬN") || out.contains("thực tế");
                    if (success) {
                        lblStatus.setText("[ĐÃ HOÀN THÀNH] 5 bài toán OQL/JPQL đã thực thi thành công!");
                        lblStatus.setForeground(MainGui.SUCCESS);
                    } else if (out.contains("LỖI") || out.contains("Error")) {
                        lblStatus.setText("[LỖI] Xem chi tiết trong khung kết quả.");
                        lblStatus.setForeground(MainGui.DANGER);
                    } else {
                        lblStatus.setText("Hoàn thành.");
                        lblStatus.setForeground(MainGui.WARNING);
                    }
                } catch (Exception ex) {
                    areaOutput.setText("[LỖI] " + ex.getMessage());
                    lblStatus.setForeground(MainGui.DANGER);
                }
                btnRun.setEnabled(true);
            }
        };
        worker.execute();
    }
}
