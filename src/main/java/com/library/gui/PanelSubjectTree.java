package com.library.gui;

import com.library.config.DatabaseConfig;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.List;

/**
 * Tab 5 — Cây Chủ đề (WITH RECURSIVE).
 * Hiển thị:
 *   - JTree trực quan theo cấp độ
 *   - JTable dữ liệu gốc
 *   - Thống kê học liệu đa chủ đề (4NF proof)
 */
public class PanelSubjectTree extends JPanel {

    private final DefaultTreeModel treeModel = new DefaultTreeModel(new DefaultMutableTreeNode("Cây Chủ đề Phân cấp"));
    private final JTree tree = new JTree(treeModel);
    private final javax.swing.table.DefaultTableModel tableModel = new javax.swing.table.DefaultTableModel();
    private final javax.swing.table.DefaultTableModel tableMulti  = new javax.swing.table.DefaultTableModel();
    private final JLabel lblStatus = new JLabel("Nhấn nút để tải cây chủ đề.");
    private JButton btnLoad;

    public JButton getBtnLoad() { return btnLoad; }

    public PanelSubjectTree() {
        super(new BorderLayout(0, 12));
        setBackground(MainGui.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        buildUI();
    }

    private void buildUI() {
        // Toolbar
        btnLoad = MainGui.makeButton("Tải cây chủ đề (WITH RECURSIVE)", MainGui.ACCENT);
        btnLoad.addActionListener(e -> loadData());
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        toolbar.setOpaque(false);
        toolbar.add(btnLoad);
        lblStatus.setFont(MainGui.FONT_BOLD);
        lblStatus.setForeground(new Color(0x64748B));
        toolbar.add(lblStatus);

        // ---- JTree (trực quan cây) ----
        tree.setFont(MainGui.FONT_BODY);
        tree.setBackground(Color.WHITE);
        tree.setRootVisible(true);
        tree.setShowsRootHandles(true);
        tree.setRowHeight(26);
        tree.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override public Component getTreeCellRendererComponent(JTree t, Object val,
                    boolean sel, boolean exp, boolean leaf, int row, boolean hasFocus) {
                super.getTreeCellRendererComponent(t, val, sel, exp, leaf, row, hasFocus);
                String txt = val.toString();
                if (txt.contains("[Cấp 1]") || txt.startsWith("Cây")) {
                    setForeground(sel ? Color.WHITE : MainGui.ACCENT);
                    setFont(MainGui.FONT_BOLD);
                } else if (txt.contains("[Cấp 2]")) {
                    setForeground(sel ? Color.WHITE : new Color(0x047857));
                    setFont(MainGui.FONT_BOLD);
                } else {
                    setForeground(sel ? Color.WHITE : new Color(0x374151));
                    setFont(MainGui.FONT_BODY);
                }
                return this;
            }
        });

        JScrollPane treeScroll = new JScrollPane(tree);
        treeScroll.setBorder(BorderFactory.createLineBorder(MainGui.BORDER_COL));
        treeScroll.setPreferredSize(new Dimension(280, 300));

        JPanel treeCard = new JPanel(new BorderLayout(0, 6));
        treeCard.setBackground(Color.WHITE);
        treeCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainGui.BORDER_COL),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        JLabel treeTitle = new JLabel("Cấu trúc Cây Chủ đề Phân cấp");
        treeTitle.setFont(MainGui.FONT_BOLD);
        treeTitle.setForeground(MainGui.ACCENT);
        treeCard.add(treeTitle, BorderLayout.NORTH);
        treeCard.add(treeScroll, BorderLayout.CENTER);

        // ---- JTable dữ liệu gốc ----
        tableModel.setColumnIdentifiers(new String[]{"Mã Chủ Đề", "Tên Chủ Đề", "Chủ Đề Cha", "Cấp Độ"});
        JTable tblRaw = MainGui.makeStyledTable(new String[0], new Object[0][0]);
        tblRaw.setModel(tableModel);
        JScrollPane tableScroll = new JScrollPane(tblRaw);
        tableScroll.setBorder(BorderFactory.createLineBorder(MainGui.BORDER_COL));

        JPanel tableCard = new JPanel(new BorderLayout(0, 6));
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainGui.BORDER_COL),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        JLabel rawTitle = new JLabel("Dữ liệu gốc truy vấn từ CTE đệ quy (WITH RECURSIVE)");
        rawTitle.setFont(MainGui.FONT_BOLD);
        rawTitle.setForeground(MainGui.ACCENT);
        tableCard.add(rawTitle,  BorderLayout.NORTH);
        tableCard.add(tableScroll, BorderLayout.CENTER);

        // ---- Split tree vs table ----
        JSplitPane topSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeCard, tableCard);
        topSplit.setResizeWeight(0.35);
        topSplit.setDividerSize(6);
        topSplit.setBorder(null);

        // ---- Học liệu đa chủ đề ----
        tableMulti.setColumnIdentifiers(new String[]{"Mã Học Liệu", "Tiêu Đề", "Số Lượng Chủ Đề Trực Thuộc"});
        JTable tblMulti = MainGui.makeStyledTable(new String[0], new Object[0][0]);
        tblMulti.setModel(tableMulti);
        JScrollPane multiScroll = new JScrollPane(tblMulti);
        multiScroll.setBorder(BorderFactory.createLineBorder(MainGui.BORDER_COL));
        multiScroll.setPreferredSize(new Dimension(0, 160));

        JPanel multiCard = new JPanel(new BorderLayout(0, 6));
        multiCard.setBackground(Color.WHITE);
        multiCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainGui.BORDER_COL),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        JLabel multiTitle = new JLabel("Thống kê Học liệu thuộc nhiều Chủ đề (Minh chứng 4NF — Bảng liên kết tách biệt)");
        multiTitle.setFont(MainGui.FONT_BOLD);
        multiTitle.setForeground(MainGui.ACCENT);
        multiCard.add(multiTitle,  BorderLayout.NORTH);
        multiCard.add(multiScroll, BorderLayout.CENTER);

        // ---- Note ----
        JTextArea note = new JTextArea(
                "[!] Cơ chế CTE đệ quy (WITH RECURSIVE):\n" +
                "• Cho phép duyệt toàn bộ cấu trúc cây phân cấp chỉ bằng duy nhất 1 câu SQL.\n" +
                "• Anchor member: Tìm tất cả nút gốc có ChuDeCha IS NULL (Cấp 1).\n" +
                "• Recursive member: Nối đệ quy bảng ChuDe với kết quả trước đó để tính CapDo = CapDo + 1.\n" +
                "-> Giảm tải hoàn toàn cho mã Java, việc duyệt cây diễn ra tại tầng CSDL.");
        note.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        note.setEditable(false);
        note.setBackground(new Color(0xEFF6FF));
        note.setForeground(new Color(0x1E40AF));
        note.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xBFDBFE)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        note.setLineWrap(true);
        note.setWrapStyleWord(true);

        // SQL hint
        JTextArea sqlNote = new JTextArea(
                "Cú pháp SQL chuẩn:\n" +
                "WITH RECURSIVE CayChuDe AS (\n" +
                "  SELECT MaChuDe, TenChuDe, ChuDeCha, 1 AS CapDo        -- Anchor\n" +
                "  FROM ChuDe WHERE ChuDeCha IS NULL\n" +
                "  UNION ALL\n" +
                "  SELECT c.MaChuDe, c.TenChuDe, c.ChuDeCha, ccd.CapDo+1 -- Recursive\n" +
                "  FROM ChuDe c JOIN CayChuDe ccd ON c.ChuDeCha = ccd.MaChuDe\n" +
                ") SELECT * FROM CayChuDe ORDER BY CapDo, TenChuDe;");
        sqlNote.setFont(MainGui.FONT_MONO);
        sqlNote.setEditable(false);
        sqlNote.setBackground(new Color(0x1E293B));
        sqlNote.setForeground(new Color(0x7DD3FC));
        sqlNote.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x334155)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        sqlNote.setLineWrap(false);

        JPanel noteRow = new JPanel(new GridLayout(1, 2, 12, 0));
        noteRow.setOpaque(false);
        noteRow.add(note);
        noteRow.add(sqlNote);

        // ---- Toàn bộ content ----
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.add(MainGui.sectionLabel("Thực nghiệm 5: Cây Chủ đề (WITH RECURSIVE) & Học liệu Đa chủ đề"));
        content.add(Box.createVerticalStrut(8));
        content.add(topSplit);
        content.add(Box.createVerticalStrut(14));
        content.add(multiCard);
        content.add(Box.createVerticalStrut(14));
        content.add(noteRow);

        JScrollPane mainScroll = new JScrollPane(content);
        mainScroll.setBorder(null);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);

        add(toolbar, BorderLayout.NORTH);
        add(mainScroll, BorderLayout.CENTER);
    }

    private void loadData() {
        btnLoad.setEnabled(false);
        lblStatus.setText("Đang truy vấn CSDL...");
        lblStatus.setForeground(MainGui.WARNING);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            List<String[]> rawRows = new ArrayList<>();
            List<String[]> multiRows = new ArrayList<>();
            String error;

            @Override protected Void doInBackground() {
                try (Connection conn = DatabaseConfig.getConnection();
                     Statement stmt = conn.createStatement()) {
                    // Cây chủ đề
                    String sql = "WITH RECURSIVE CayChuDe AS (" +
                            "SELECT MaChuDe, TenChuDe, ChuDeCha, 1 AS CapDo " +
                            "FROM ChuDe WHERE ChuDeCha IS NULL " +
                            "UNION ALL " +
                            "SELECT c.MaChuDe, c.TenChuDe, c.ChuDeCha, ccd.CapDo+1 " +
                            "FROM ChuDe c JOIN CayChuDe ccd ON c.ChuDeCha = ccd.MaChuDe" +
                            ") SELECT * FROM CayChuDe ORDER BY CapDo, TenChuDe";
                    try (ResultSet rs = stmt.executeQuery(sql)) {
                        while (rs.next()) {
                            rawRows.add(new String[]{
                                rs.getString("MaChuDe"),
                                rs.getString("TenChuDe"),
                                rs.getString("ChuDeCha") == null ? "(Gốc)" : rs.getString("ChuDeCha"),
                                String.valueOf(rs.getInt("CapDo"))
                            });
                        }
                    }
                    // Học liệu đa chủ đề
                    try (ResultSet rs = stmt.executeQuery(
                            "SELECT h.MaHocLieu, h.TieuDe, COUNT(hc.MaChuDe) AS SoLuongChuDe " +
                            "FROM HocLieu h JOIN HocLieu_ChuDe hc ON h.MaHocLieu = hc.MaHocLieu " +
                            "GROUP BY h.MaHocLieu, h.TieuDe HAVING COUNT(hc.MaChuDe) > 1")) {
                        while (rs.next()) {
                            multiRows.add(new String[]{
                                rs.getString("MaHocLieu"),
                                rs.getString("TieuDe"),
                                String.valueOf(rs.getInt("SoLuongChuDe"))
                            });
                        }
                    }
                } catch (SQLException ex) {
                    error = ex.getMessage();
                }
                return null;
            }

            @Override protected void done() {
                if (error != null) {
                    lblStatus.setText("[LỖI] " + error);
                    lblStatus.setForeground(MainGui.DANGER);
                } else {
                    // Cập nhật bảng raw
                    tableModel.setRowCount(0);
                    for (String[] r : rawRows) tableModel.addRow(r);

                    // Xây dựng JTree
                    buildTree(rawRows);

                    // Học liệu đa chủ đề
                    tableMulti.setRowCount(0);
                    for (String[] r : multiRows) tableMulti.addRow(r);

                    lblStatus.setText("[ĐÃ TẢI] " + rawRows.size() + " chủ đề | " +
                            multiRows.size() + " học liệu đa chủ đề.");
                    lblStatus.setForeground(MainGui.SUCCESS);
                }
                btnLoad.setEnabled(true);
            }
        };
        worker.execute();
    }

    private void buildTree(List<String[]> rows) {
        Map<String, DefaultMutableTreeNode> nodes = new LinkedHashMap<>();

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Cây Chủ đề Phân cấp");
        treeModel.setRoot(root);

        for (String[] r : rows) {
            String ma = r[0], ten = r[1], cha = r[2];
            int cap = Integer.parseInt(r[3]);
            DefaultMutableTreeNode node = new DefaultMutableTreeNode("[" + ma + "] " + ten + "  [Cấp " + cap + "]");
            nodes.put(ma, node);
            if ("(Gốc)".equals(cha) || cha == null) {
                root.add(node);
            } else {
                DefaultMutableTreeNode parent = nodes.get(cha);
                if (parent != null) parent.add(node);
                else root.add(node);
            }
        }

        treeModel.reload();
        for (int i = 0; i < tree.getRowCount(); i++) tree.expandRow(i);
    }
}
