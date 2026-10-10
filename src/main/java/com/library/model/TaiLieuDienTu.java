package com.library.model;

import java.util.Date;

public class TaiLieuDienTu extends HocLieu {
    private String dinhDang;
    private Double dungLuong;
    private String quyenTruyCap;
    private String urlLienKet;

    public TaiLieuDienTu() {}

    public TaiLieuDienTu(String maHocLieu, String tieuDe, String moTa, Integer namXuatBan, String ngonNgu, Double giaTien, Date ngayNhapKho, String anhBia, String dinhDang, Double dungLuong, String quyenTruyCap, String urlLienKet) {
        super(maHocLieu, tieuDe, moTa, namXuatBan, ngonNgu, giaTien, ngayNhapKho, anhBia);
        this.dinhDang = dinhDang;
        this.dungLuong = dungLuong;
        this.quyenTruyCap = quyenTruyCap;
        this.urlLienKet = urlLienKet;
    }

    public String getDinhDang() { return dinhDang; }
    public void setDinhDang(String dinhDang) { this.dinhDang = dinhDang; }
    public Double getDungLuong() { return dungLuong; }
    public void setDungLuong(Double dungLuong) { this.dungLuong = dungLuong; }
    public String getQuyenTruyCap() { return quyenTruyCap; }
    public void setQuyenTruyCap(String quyenTruyCap) { this.quyenTruyCap = quyenTruyCap; }
    public String getUrlLienKet() { return urlLienKet; }
    public void setUrlLienKet(String urlLienKet) { this.urlLienKet = urlLienKet; }
}
