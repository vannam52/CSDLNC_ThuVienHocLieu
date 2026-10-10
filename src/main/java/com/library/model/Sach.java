package com.library.model;

import java.util.Date;

public class Sach extends HocLieu {
    private String isbn;
    private String nhaXuatBan;
    private Integer soTrang;
    private Integer lanTaiBan;

    public Sach() {}

    public Sach(String maHocLieu, String tieuDe, String moTa, Integer namXuatBan, String ngonNgu, Double giaTien, Date ngayNhapKho, String anhBia, String isbn, String nhaXuatBan, Integer soTrang, Integer lanTaiBan) {
        super(maHocLieu, tieuDe, moTa, namXuatBan, ngonNgu, giaTien, ngayNhapKho, anhBia);
        this.isbn = isbn;
        this.nhaXuatBan = nhaXuatBan;
        this.soTrang = soTrang;
        this.lanTaiBan = lanTaiBan;
    }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getNhaXuatBan() { return nhaXuatBan; }
    public void setNhaXuatBan(String nhaXuatBan) { this.nhaXuatBan = nhaXuatBan; }
    public Integer getSoTrang() { return soTrang; }
    public void setSoTrang(Integer soTrang) { this.soTrang = soTrang; }
    public Integer getLanTaiBan() { return lanTaiBan; }
    public void setLanTaiBan(Integer lanTaiBan) { this.lanTaiBan = lanTaiBan; }
}
