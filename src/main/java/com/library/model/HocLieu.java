package com.library.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToMany;
import javax.persistence.CascadeType;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class HocLieu {
    @Id
    private String maHocLieu;
    private String tieuDe;
    private String moTa;
    private Integer namXuatBan;
    private String ngonNgu;
    private Double giaTien;

    @Temporal(TemporalType.DATE)
    private Date ngayNhapKho;
    private String anhBia;

    @ManyToMany(cascade = CascadeType.PERSIST)
    private List<TacGia> danhSachTacGia = new ArrayList<>();

    @ManyToMany(cascade = CascadeType.PERSIST)
    private List<ChuDe> danhSachChuDe = new ArrayList<>();

    public HocLieu() {}

    public HocLieu(String maHocLieu, String tieuDe, String moTa, Integer namXuatBan,
                   String ngonNgu, Double giaTien, Date ngayNhapKho, String anhBia) {
        this.maHocLieu = maHocLieu;
        this.tieuDe = tieuDe;
        this.moTa = moTa;
        this.namXuatBan = namXuatBan;
        this.ngonNgu = ngonNgu;
        this.giaTien = giaTien;
        this.ngayNhapKho = ngayNhapKho;
        this.anhBia = anhBia;
    }

    // --- Getters & Setters ---
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
    public List<TacGia> getDanhSachTacGia() { return danhSachTacGia; }
    public void setDanhSachTacGia(List<TacGia> danhSachTacGia) { this.danhSachTacGia = danhSachTacGia; }
    public List<ChuDe> getDanhSachChuDe() { return danhSachChuDe; }
    public void setDanhSachChuDe(List<ChuDe> danhSachChuDe) { this.danhSachChuDe = danhSachChuDe; }

    // ===================================================================
    //  PHƯƠNG THỨC NGHIỆP VỤ HƯỚNG ĐỐI TƯỢNG (OODBMS Methods)
    // ===================================================================
    public boolean isTaiLieuGiaTriCao() {
        return giaTien != null && giaTien >= 500000;
    }

    public int getSoLuongTacGia() {
        return danhSachTacGia != null ? danhSachTacGia.size() : 0;
    }

    public int getSoLuongChuDe() {
        return danhSachChuDe != null ? danhSachChuDe.size() : 0;
    }
}
