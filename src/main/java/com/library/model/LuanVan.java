package com.library.model;

import java.util.Date;

public class LuanVan extends HocLieu {
    private String nguoiHuongDan;
    private String truong;
    private String capBaoVe;
    private String chuyenNganh;
    private Double diemBaoVe;

    public LuanVan() {}

    public LuanVan(String maHocLieu, String tieuDe, String moTa, Integer namXuatBan, String ngonNgu, Double giaTien, Date ngayNhapKho, String anhBia, String nguoiHuongDan, String truong, String capBaoVe, String chuyenNganh, Double diemBaoVe) {
        super(maHocLieu, tieuDe, moTa, namXuatBan, ngonNgu, giaTien, ngayNhapKho, anhBia);
        this.nguoiHuongDan = nguoiHuongDan;
        this.truong = truong;
        this.capBaoVe = capBaoVe;
        this.chuyenNganh = chuyenNganh;
        this.diemBaoVe = diemBaoVe;
    }

    public String getNguoiHuongDan() { return nguoiHuongDan; }
    public void setNguoiHuongDan(String nguoiHuongDan) { this.nguoiHuongDan = nguoiHuongDan; }
    public String getTruong() { return truong; }
    public void setTruong(String truong) { this.truong = truong; }
    public String getCapBaoVe() { return capBaoVe; }
    public void setCapBaoVe(String capBaoVe) { this.capBaoVe = capBaoVe; }
    public String getChuyenNganh() { return chuyenNganh; }
    public void setChuyenNganh(String chuyenNganh) { this.chuyenNganh = chuyenNganh; }
    public Double getDiemBaoVe() { return diemBaoVe; }
    public void setDiemBaoVe(Double diemBaoVe) { this.diemBaoVe = diemBaoVe; }
}
