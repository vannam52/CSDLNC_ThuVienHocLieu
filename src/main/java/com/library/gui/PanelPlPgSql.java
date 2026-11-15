package com.library.gui;

import com.library.dao.PlPgSqlDAO;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Tab 7 — PL/pgSQL: Triggers, Stored Functions, Savepoint & Procedures.
 * Thực thi PlPgSqlDAO.demoTriggersAndFunctions() và hiển thị trực quan trên GUI.
 */
public class PanelPlPgSql extends JPanel {

    private final JTextArea areaOutput = new JTextArea();
    private final JLabel lblStatus = new JLabel("Nhấn nút để chạy toàn bộ kịch bản kiểm thử PL/pgSQL.");
    private JButton btnRun;

    public PanelPlPgSql() {
        super(new BorderLayout(0, 12));
        setBackground(MainGui.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        buildUI();
    }

    private void buildUI() {
        // ---- Toolbar ----
        btnRun = MainGui.makeButton("Chạy Demo PL/pgSQL (Triggers, Functions & Savepoint)", MainGui.ACCENT);
        btnRun.addActionListener(e -> runDemo());

        JButton btnClear = MainGui.makeButton("Xoá màn hình", new Color(0x64748B));
        btnClear.addActionListener(e -> {
            areaOutput.setText("");
            lblStatus.setForeground(new Color(0x64748B));
            lblStatus.setText("Đã xoá output.");
        });

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        toolbar.setOpaque(false);
        toolbar.add(btnRun);
        toolbar.add(btnClear);
        lblStatus.setFont(MainGui.FONT_BOLD);
        lblStatus.setForeground(new Color(0x64748B));
        toolbar.add(lblStatus);

        // ---- Bảng tóm tắt các đối tượng PL/pgSQL được kiểm thử ----
        String[] cols = {"#", "Đối tượng PL/pgSQL", "Loại", "Nội dung kiểm thử"};
        Object[][] rows = {
            {"1", "fn_ThongKe_HocLieu_TheoChuDe()", "TABLE Function", "Thống kê số lượng, tỷ lệ % và tổng giá trị học liệu theo từng chủ đề"},
            {"2", "fn_KiemTra_CoTheMuon(maBD)", "Scalar Function", "Kiểm tra điều kiện mượn (không khóa, chưa hết hạn, < 5 cuốn đang mượn)"},
            {"3", "fn_TimKiem_HocLieu(keyword)", "GIN Trigram Function", "Tìm kiếm gần đúng với độ tương đồng Trigram"},
            {"4", "trg_DongBo_TrangThai_MuonTra", "Trigger", "Tự động chuyển trạng thái bản sao khi mượn/trả và kiểm tra ràng buộc"},
            {"5", "SAVEPOINT & Phạt trễ hạn", "Transaction + Function", "Kiểm thử savepoint, tính tiền phạt trễ hạn và hoàn tác dữ liệu demo"},
        };
        JTable tblSummary = MainGui.makeStyledTable(cols, rows);
        tblSummary.getColumnModel().getColumn(0).setPreferredWidth(35);
        tblSummary.getColumnModel().getColumn(1).setPreferredWidth(230);
        tblSummary.getColumnModel().getColumn(2).setPreferredWidth(150);
        tblSummary.getColumnModel().getColumn(3).setPreferredWidth(460);

        JScrollPane scrollTable = new JScrollPane(
                tblSummary,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollTable.setBorder(BorderFactory.createLineBorder(MainGui.BORDER_COL));
        scrollTable.setPreferredSize(new Dimension(0, 175));

        JPanel tableCard = new JPanel(new BorderLayout(0, 6));
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainGui.BORDER_COL),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        JLabel tblTitle = new JLabel("Danh mục Triggers, Functions & Procedures được kiểm thử");
        tblTitle.setFont(MainGui.FONT_BOLD);
        tblTitle.setForeground(MainGui.ACCENT);
        tableCard.add(tblTitle, BorderLayout.NORTH);
        tableCard.add(scrollTable, BorderLayout.CENTER);

        // ---- Output Area ----
        JScrollPane scrollOutput = MainGui.makeOutputArea(areaOutput);
        areaOutput.setText("Nhấn nút 'Chạy Demo PL/pgSQL' ở trên để kiểm thử:\n" +
                "• Các Stored Functions (thống kê chủ đề, kiểm tra điều kiện mượn, tìm kiếm GIN Trigram)\n" +
                "• Trigger đồng bộ trạng thái bản sao khi mượn / trả\n" +
                "• SAVEPOINT và tính phạt trễ hạn (dữ liệu demo tự động hoàn tác sau khi chạy)\n");

        JPanel outputCard = new JPanel(new BorderLayout(0, 6));
        outputCard.setBackground(Color.WHITE);
        outputCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainGui.BORDER_COL),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        JLabel outTitle = new JLabel("Nhật ký thực thi thực tế trên PostgreSQL");
        outTitle.setFont(MainGui.FONT_BOLD);
        outTitle.setForeground(MainGui.ACCENT);
        outputCard.add(outTitle, BorderLayout.NORTH);
        outputCard.add(scrollOutput, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableCard, outputCard);
        split.setResizeWeight(0.32);
        split.setDividerSize(6);
        split.setBorder(null);

        add(toolbar, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
    }

    private void runDemo() {
        btnRun.setEnabled(false);
        lblStatus.setForeground(new Color(0xD97706));
        lblStatus.setText("⏳ Đang thực thi demo PL/pgSQL...");
        areaOutput.setText("Đang kết nối PostgreSQL và chạy kịch bản PL/pgSQL...\n");

        new SwingWorker<String, Void>() {
            private double elapsedMs;

            @Override
            protected String doInBackground() {
                long startNs = System.nanoTime();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(bos, true, StandardCharsets.UTF_8);
                PrintStream oldOut = System.out;
                PrintStream oldErr = System.err;
                System.setOut(ps);
                System.setErr(ps);
                try {
                    new PlPgSqlDAO().demoTriggersAndFunctions();
                } finally {
                    System.setOut(oldOut);
                    System.setErr(oldErr);
                    ps.flush();
                    elapsedMs = (System.nanoTime() - startNs) / 1_000_000.0;
                }
                return bos.toString(StandardCharsets.UTF_8);
            }

            @Override
            protected void done() {
                btnRun.setEnabled(true);
                try {
                    String out = get();
                    areaOutput.setText(out);
                    areaOutput.setCaretPosition(0);
                    lblStatus.setForeground(MainGui.SUCCESS);
                    lblStatus.setText(String.format("✅ Hoàn tất demo PL/pgSQL (⏱ %.2f ms)", elapsedMs));
                } catch (Exception ex) {
                    lblStatus.setForeground(MainGui.DANGER);
                    lblStatus.setText("❌ Lỗi: " + ex.getMessage());
                    areaOutput.append("\n[LỖI] " + ex.getMessage());
                }
            }
        }.execute();
    }
}
