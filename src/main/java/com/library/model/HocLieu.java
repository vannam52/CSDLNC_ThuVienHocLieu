package com.library.model;

import java.util.Date;

public abstract class HocLieu {
    private String maHocLieu;
    private String tieuDe;
    private String moTa;
    private Integer namXuatBan;
    private String ngonNgu;
    private Double giaTien;
    private Date ngayNhapKho;
    private String anhBia;

    public HocLieu() {}

    public HocLieu(String maHocLieu, String tieuDe, String moTa, Integer namXuatBan, String ngonNgu, Double giaTien, Date ngayNhapKho, String anhBia) {
        this.maHocLieu = maHocLieu;
        this.tieuDe = tieuDe;
        this.moTa = moTa;
        this.namXuatBan = namXuatBan;
        this.ngonNgu = ngonNgu;
        this.giaTien = giaTien;
        this.ngayNhapKho = ngayNhapKho;
        this.anhBia = anhBia;
    }

    public String getMaHocLieu() { return maHocLieu; }
    public void setMaHocLieu(String maHocLieu) { this.maHocLieu = maHocLieu; }
    public String getTieuDe() { return tieuDe; }
    public void setTieuDe(String tieuDe) { this.tieuDe = tieuDe; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public Integer getNamXuatBan() { return namXuatBan; }
    public void setNamXuatBan(Integer namXuatBan) { this.namXuatBan = namXuatBan; }
    public String getNgonNgu() { return ngonNgu; }
    public void setNgonNgu(String ngonNgu) { this.ngonNgu = ngonNgu; }
    public Double getGiaTien() { return giaTien; }
    public void setGiaTien(Double giaTien) { this.giaTien = giaTien; }
    public Date getNgayNhapKho() { return ngayNhapKho; }
    public void setNgayNhapKho(Date ngayNhapKho) { this.ngayNhapKho = ngayNhapKho; }
    public String getAnhBia() { return anhBia; }
    public void setAnhBia(String anhBia) { this.anhBia = anhBia; }
}
