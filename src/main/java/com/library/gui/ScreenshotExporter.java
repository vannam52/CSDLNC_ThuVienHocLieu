package com.library.gui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Tự động chạy tất cả 5 thực nghiệm trên GUI và chụp ảnh độ phân giải cao
 * lưu vào thư mục report/images/ để đưa vào báo cáo Word và Slide.
 */
public class ScreenshotExporter {

    public static void main(String[] args) {
        System.out.println("=== BẮT ĐẦU CHỤP ẢNH MINH CHỨNG THỰC NGHIỆM GUI ===");

        File outDir = new File("report/images");
        if (!outDir.exists()) outDir.mkdirs();

        SwingUtilities.invokeLater(() -> {
            try {
                MainGui gui = new MainGui();
                gui.setSize(1150, 730);
                gui.doLayout();
                gui.setVisible(true);

                new Thread(() -> {
                    try {
                        Thread.sleep(600);

                        JTabbedPane tabs = gui.getTabs();

                        // --- 1. Tab 1: 4NF & Kế thừa ---
                        System.out.println("-> Đang chạy và chụp Tab 1 (4NF & Kế thừa)...");
                        SwingUtilities.invokeAndWait(() -> tabs.setSelectedIndex(0));
                        PanelNormalization p1 = (PanelNormalization) tabs.getSelectedComponent();
                        SwingUtilities.invokeAndWait(() -> p1.getBtnRun().doClick());
                        waitForButton(p1.getBtnRun());
                        Thread.sleep(300);
                        saveComponent(gui, new File(outDir, "Hinh_GUI_Tab1_4NF_KeThua.png"));

                        // --- 2. Tab 2: Giao dịch đồng thời ---
                        System.out.println("-> Đang chạy và chụp Tab 2 (Giao dịch đồng thời)...");
                        SwingUtilities.invokeAndWait(() -> tabs.setSelectedIndex(1));
                        PanelConcurrency p2 = (PanelConcurrency) tabs.getSelectedComponent();
                        SwingUtilities.invokeAndWait(() -> p2.getBtnConcurrent().doClick());
                        waitForButton(p2.getBtnConcurrent());
                        SwingUtilities.invokeAndWait(() -> {
                            if (p2.getMainScroll() != null) {
                                p2.getMainScroll().getVerticalScrollBar().setValue(0);
                            }
                        });
                        Thread.sleep(400);
                        saveComponent(gui, new File(outDir, "Hinh_GUI_Tab2_A_TranhChap.png"));

                        SwingUtilities.invokeAndWait(() -> p2.getBtnRollback().doClick());
                        waitForButton(p2.getBtnRollback());
                        SwingUtilities.invokeAndWait(() -> {
                            if (p2.getMainScroll() != null) {
                                JScrollBar sb = p2.getMainScroll().getVerticalScrollBar();
                                sb.setValue(sb.getMaximum() / 2 + 100);
                            }
                        });
                        Thread.sleep(400);
                        saveComponent(gui, new File(outDir, "Hinh_GUI_Tab2_B_Rollback.png"));
                        saveComponent(gui, new File(outDir, "Hinh_GUI_Tab2_GiaoDich_DongThoi.png"));

                        // --- 3. Tab 3: GIN Trigram Benchmark ---
                        System.out.println("-> Đang chạy và chụp Tab 3 (GIN Trigram Benchmark)...");
                        SwingUtilities.invokeAndWait(() -> tabs.setSelectedIndex(2));
                        PanelGinBenchmark p3 = (PanelGinBenchmark) tabs.getSelectedComponent();
                        SwingUtilities.invokeAndWait(() -> p3.getBtnRun().doClick());
                        waitForButton(p3.getBtnRun());
                        Thread.sleep(400);
                        saveComponent(gui, new File(outDir, "Hinh_GUI_Tab3_GIN_Benchmark.png"));

                        // --- 4. Tab 4: ObjectDB & OQL ---
                        System.out.println("-> Đang chạy và chụp Tab 4 (ObjectDB & OQL)...");
                        SwingUtilities.invokeAndWait(() -> tabs.setSelectedIndex(3));
                        PanelObjectDB p4 = (PanelObjectDB) tabs.getSelectedComponent();
                        SwingUtilities.invokeAndWait(() -> p4.getBtnRun().doClick());
                        waitForButton(p4.getBtnRun());
                        Thread.sleep(300);
                        saveComponent(gui, new File(outDir, "Hinh_GUI_Tab4_ObjectDB_OQL.png"));

                        // --- 5. Tab 5: Cây chủ đề ---
                        System.out.println("-> Đang chạy và chụp Tab 5 (Cây chủ đề WITH RECURSIVE)...");
                        SwingUtilities.invokeAndWait(() -> tabs.setSelectedIndex(4));
                        PanelSubjectTree p5 = (PanelSubjectTree) tabs.getSelectedComponent();
                        SwingUtilities.invokeAndWait(() -> p5.getBtnLoad().doClick());
                        waitForButton(p5.getBtnLoad());
                        Thread.sleep(300);
                        saveComponent(gui, new File(outDir, "Hinh_GUI_Tab5_CayChuDe_Recursive.png"));

                        System.out.println("=== HOÀN TẤT CHỤP 5 ẢNH MINH CHỨNG THỰC NGHIỆM! ===");
                        gui.dispose();
                        System.exit(0);

                    } catch (Exception e) {
                        e.printStackTrace();
                        gui.dispose();
                        System.exit(1);
                    }
                }).start();

            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        });
    }

    private static void waitForButton(JButton btn) throws InterruptedException {
        while (!btn.isEnabled()) {
            Thread.sleep(100);
        }
    }

    private static void saveComponent(Component comp, File file) {
        int w = comp.getWidth();
        int h = comp.getHeight();
        if (w <= 0 || h <= 0) {
            w = 1150;
            h = 730;
        }
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        comp.paint(g2);
        g2.dispose();

        try {
            ImageIO.write(img, "PNG", file);
            System.out.println("   [Đã lưu] " + file.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("   [Lỗi lưu ảnh] " + e.getMessage());
        }
    }
}

