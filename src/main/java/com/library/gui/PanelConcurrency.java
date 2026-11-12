package com.library.gui;

import com.library.dao.TransactionDAO;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Tab 2 — Giao dịch Đồng thời & Rollback.
 * Minh hoạ 2 kịch bản:
 *   A) Hai bạn đọc cùng mượn 1 bản sao → 1 thành công, 1 bị chặn
 *   B) Rollback khi INSERT vi phạm FK → tình trạng ban đầu được khôi phục
 */
public class PanelConcurrency extends JPanel {

    private final JTextArea areaDemo    = new JTextArea();
    private final JTextArea areaRollback = new JTextArea();
    private final JLabel    lblResultDemo    = new JLabel(" ");
    private final JLabel    lblResultRollback = new JLabel(" ");
    private JButton btnConcurrent;
    private JButton btnRollback;
    private JScrollPane mainScroll;

    public JButton getBtnConcurrent() { return btnConcurrent; }
    public JButton getBtnRollback() { return btnRollback; }
    public JScrollPane getMainScroll() { return mainScroll; }

    public PanelConcurrency() {
        super(new BorderLayout(0, 12));
        setBackground(MainGui.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        buildUI();
    }

    private void buildUI() {
        // ---- Phần trên: Mượn đồng thời ----
        btnConcurrent = MainGui.makeButton("Demo mượn đồng thời (2 luồng)", MainGui.ACCENT);
        btnConcurrent.addActionListener(e -> runConcurrent(btnConcurrent));

        JScrollPane scrollDemo = MainGui.makeOutputArea(areaDemo);
        scrollDemo.setPreferredSize(new Dimension(0, 200));

        lblResultDemo.setFont(MainGui.FONT_BOLD);
        lblResultDemo.setForeground(MainGui.SUCCESS);

        JPanel topCard = makeCard(
                "Kịch bản A: Mượn đồng thời — 1 bản sao, 2 bạn đọc song song",
                "Hai luồng cùng gọi CALL sp_MuonSach() — khoá FOR UPDATE đảm bảo chỉ 1 phiên thành công.",
                btnConcurrent, scrollDemo, lblResultDemo);

        // ---- Phần dưới: Rollback ----
        btnRollback = MainGui.makeButton("Demo Rollback giao dịch", new Color(0x7C3AED));
        btnRollback.addActionListener(e -> runRollback(btnRollback));

        JScrollPane scrollRollback = MainGui.makeOutputArea(areaRollback);
        scrollRollback.setPreferredSize(new Dimension(0, 200));

        lblResultRollback.setFont(MainGui.FONT_BOLD);
        lblResultRollback.setForeground(MainGui.SUCCESS);

        JPanel botCard = makeCard(
                "Kịch bản B: Rollback — Vi phạm FK -> Khôi phục nguyên trạng",
                "INSERT bạn đọc không tồn tại -> SQLException 23503 -> ROLLBACK -> BanSao vẫn ở trạng thái ban đầu.",
                btnRollback, scrollRollback, lblResultRollback);

        // ---- Giải thích cơ chế ----
        JPanel explainPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        explainPanel.setOpaque(false);
        explainPanel.add(makeInfoBox("Cơ chế khoá trong sp_MuonSach",
                "SELECT ... FOR UPDATE NO WAIT\n" +
                "-> Luồng đến sau nhận ngay lỗi 55000\n" +
                "   (chặn lập tức, không chờ, không deadlock)\n\n" +
                "PostgreSQL dùng row-level lock:\n" +
                "• Phiên 1 khoá hàng BanSao trước\n" +
                "• Phiên 2 cố khoá -> bị chặn ngay lập tức\n" +
                "• Đảm bảo chỉ duy nhất 1 PhieuMuon được tạo"));
        explainPanel.add(makeInfoBox("Cơ chế bảo toàn tính toàn vẹn (Rollback)",
                "ATOMIC: UPDATE + INSERT trong 1 giao dịch\n" +
                "-> Nếu INSERT thất bại (vi phạm FK)\n" +
                "   -> toàn bộ giao dịch bị ROLLBACK\n\n" +
                "Xác minh sau rollback:\n" +
                "• TinhTrangVatLy = 'Mới' (giữ nguyên gốc)\n" +
                "• TrangThai BanSao = 'San sang'\n" +
                "• Số PhieuMuon = 0 (không phát sinh rác)"));

        // ---- Layout ----
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.add(topCard);
        content.add(Box.createVerticalStrut(14));
        content.add(botCard);
        content.add(Box.createVerticalStrut(14));
        content.add(MainGui.sectionLabel("Phân tích cơ chế kỹ thuật"));
        content.add(Box.createVerticalStrut(8));
        content.add(explainPanel);

        mainScroll = new JScrollPane(content);
        mainScroll.setBorder(null);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScroll, BorderLayout.CENTER);
    }

    private JPanel makeCard(String title, String desc,
                            JButton btn, JScrollPane output, JLabel result) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainGui.BORDER_COL),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));

        JLabel lbl = new JLabel(title);
        lbl.setFont(MainGui.FONT_HEADER);
        lbl.setForeground(MainGui.ACCENT);

        JLabel descLbl = new JLabel("<html><i>" + desc + "</i></html>");
        descLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLbl.setForeground(new Color(0x64748B));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.add(btn);
        btnRow.add(result);

        JPanel top = new JPanel(new GridLayout(3, 1, 0, 4));
        top.setOpaque(false);
        top.add(lbl);
        top.add(descLbl);
        top.add(btnRow);

        card.add(top,    BorderLayout.NORTH);
        card.add(output, BorderLayout.CENTER);
        return card;
    }

    private JPanel makeInfoBox(String title, String content) {
        JPanel box = new JPanel(new BorderLayout(0, 6));
        box.setBackground(new Color(0xF0FDF4));
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xBBF7D0)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        JLabel t = new JLabel(title);
        t.setFont(MainGui.FONT_BOLD);
        t.setForeground(MainGui.SUCCESS);
        JTextArea txt = new JTextArea(content);
        txt.setFont(MainGui.FONT_MONO);
        txt.setOpaque(false);
        txt.setEditable(false);
        txt.setForeground(new Color(0x166534));
        box.add(t, BorderLayout.NORTH);
        box.add(txt, BorderLayout.CENTER);
        return box;
    }

    // ---- Workers ----

    private void runConcurrent(JButton btn) {
        btn.setEnabled(false);
        areaDemo.setText("Đang khởi tạo demo...\n");
        lblResultDemo.setText(" ");

        SwingWorker<String, String> worker = new SwingWorker<>() {
            @Override protected String doInBackground() {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(bos, true, StandardCharsets.UTF_8);
                PrintStream old = System.out;
                System.setOut(ps);
                try {
                    new TransactionDAO().demoConcurrentBorrow();
                } finally {
                    System.setOut(old);
                    ps.flush();
                }
                return bos.toString(StandardCharsets.UTF_8);
            }

            @Override protected void done() {
                try {
                    String out = get();
                    areaDemo.setText(out);
                    // Phân tích kết quả
                    boolean ok = out.contains("thành công") && out.contains("bị chặn");
                    if (ok) {
                        lblResultDemo.setText("[KẾT QUẢ ĐÚNG] 1 phiên thành công, 1 phiên bị chặn — khoá FOR UPDATE hoạt động chuẩn xác!");
                        lblResultDemo.setForeground(MainGui.SUCCESS);
                    } else if (out.contains("Lỗi")) {
                        lblResultDemo.setText("[LỖI] Có lỗi xảy ra — vui lòng xem log chi tiết.");
                        lblResultDemo.setForeground(MainGui.DANGER);
                    }
                } catch (Exception ex) {
                    areaDemo.setText("[LỖI] " + ex.getMessage());
                    lblResultDemo.setForeground(MainGui.DANGER);
                }
                btn.setEnabled(true);
            }
        };
        worker.execute();
    }

    private void runRollback(JButton btn) {
        btn.setEnabled(false);
        areaRollback.setText("Đang chạy kịch bản kiểm thử rollback...\n");
        lblResultRollback.setText(" ");

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override protected String doInBackground() {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(bos, true, StandardCharsets.UTF_8);
                PrintStream old = System.out;
                System.setOut(ps);
                try {
                    new TransactionDAO().demoRollbackAndVerify();
                } finally {
                    System.setOut(old);
                    ps.flush();
                }
                return bos.toString(StandardCharsets.UTF_8);
            }

            @Override protected void done() {
                try {
                    String out = get();
                    areaRollback.setText(out);
                    boolean ok = out.contains("Rollback thành công");
                    if (ok) {
                        lblResultRollback.setText("[KẾT QUẢ ĐÚNG] Rollback thành công — dữ liệu khôi phục nguyên trạng!");
                        lblResultRollback.setForeground(MainGui.SUCCESS);
                    } else if (out.contains("Lỗi")) {
                        lblResultRollback.setText("[LỖI] Có lỗi xảy ra.");
                        lblResultRollback.setForeground(MainGui.DANGER);
                    }
                } catch (Exception ex) {
                    areaRollback.setText("[LỖI] " + ex.getMessage());
                    lblResultRollback.setForeground(MainGui.DANGER);
                }
                btn.setEnabled(true);
            }
        };
        worker.execute();
    }
}
