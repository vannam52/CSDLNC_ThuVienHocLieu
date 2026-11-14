package com.library.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;

/**
 * Hộp thoại hiển thị mã nguồn SQL / PL/pgSQL tương ứng của từng nghiệp vụ.
 * Giúp nhóm trình chiếu và giải trình câu hỏi của Hội đồng bảo vệ đồ án một cách chuyên nghiệp.
 */
public class SqlViewerDialog extends JDialog {

    public static void show(Component parent, String title, String sourceFile, String sqlCode) {
        Window window = SwingUtilities.getWindowAncestor(parent);
        SqlViewerDialog dialog = new SqlViewerDialog(window, title, sourceFile, sqlCode);
        dialog.setVisible(true);
    }

    public SqlViewerDialog(Window owner, String title, String sourceFile, String sqlCode) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        setSize(850, 580);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(0, 10));
        getContentPane().setBackground(MainGui.BG_MAIN);

        // Header
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, MainGui.BORDER_COL),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(MainGui.FONT_HEADER);
        lblTitle.setForeground(MainGui.ACCENT);

        JLabel lblFile = new JLabel("📄 Tệp nguồn: " + sourceFile);
        lblFile.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblFile.setForeground(new Color(0x64748B));

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 3));
        titleBox.setOpaque(false);
        titleBox.add(lblTitle);
        titleBox.add(lblFile);
        header.add(titleBox, BorderLayout.WEST);

        // Code Editor/Viewer
        JTextArea codeArea = new JTextArea(sqlCode.trim());
        codeArea.setFont(MainGui.FONT_MONO);
        codeArea.setBackground(new Color(0x0F172A));
        codeArea.setForeground(new Color(0xE2E8F0));
        codeArea.setCaretColor(Color.WHITE);
        codeArea.setEditable(false);
        codeArea.setMargin(new Insets(12, 16, 12, 16));
        codeArea.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(codeArea);
        scroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 18, 0, 18),
                BorderFactory.createLineBorder(MainGui.BORDER_COL)));

        // Footer buttons
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, MainGui.BORDER_COL));

        JLabel lblStatus = new JLabel(" ");
        lblStatus.setFont(MainGui.FONT_BOLD);
        lblStatus.setForeground(MainGui.SUCCESS);

        JButton btnCopy = MainGui.makeButton("📋 Sao chép mã SQL", MainGui.ACCENT);
        btnCopy.addActionListener(e -> {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(codeArea.getText()), null);
            lblStatus.setText("✓ Đã sao chép vào bộ nhớ đệm!");
        });

        JButton btnClose = new JButton("Đóng");
        btnClose.setFont(MainGui.FONT_BODY);
        btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());

        footer.add(lblStatus);
        footer.add(btnCopy);
        footer.add(btnClose);

        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }
}
