package com.library.model;

import java.util.Date;

public class VideoBaiGiang extends HocLieu {
    private Integer thoiLuong;
    private String doPhanGiai;
    private String ngonNguPhuDe;
    private Double dungLuong;

    public VideoBaiGiang() {}

    public VideoBaiGiang(String maHocLieu, String tieuDe, String moTa, Integer namXuatBan, String ngonNgu, Double giaTien, Date ngayNhapKho, String anhBia, Integer thoiLuong, String doPhanGiai, String ngonNguPhuDe, Double dungLuong) {
        super(maHocLieu, tieuDe, moTa, namXuatBan, ngonNgu, giaTien, ngayNhapKho, anhBia);
        this.thoiLuong = thoiLuong;
        this.doPhanGiai = doPhanGiai;
        this.ngonNguPhuDe = ngonNguPhuDe;
        this.dungLuong = dungLuong;
    }

    public Integer getThoiLuong() { return thoiLuong; }
    public void setThoiLuong(Integer thoiLuong) { this.thoiLuong = thoiLuong; }
    public String getDoPhanGiai() { return doPhanGiai; }
    public void setDoPhanGiai(String doPhanGiai) { this.doPhanGiai = doPhanGiai; }
    public String getNgonNguPhuDe() { return ngonNguPhuDe; }
    public void setNgonNguPhuDe(String ngonNguPhuDe) { this.ngonNguPhuDe = ngonNguPhuDe; }
    public Double getDungLuong() { return dungLuong; }
    public void setDungLuong(Double dungLuong) { this.dungLuong = dungLuong; }
}
