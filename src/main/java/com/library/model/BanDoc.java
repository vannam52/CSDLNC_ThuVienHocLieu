package com.library.model;

import java.util.Date;

public class BanDoc {
    private String maBanDoc;
    private String hoTen;
    private String email;
    private String soDienThoai;
    private String matKhau;
    private String loaiTaiKhoan;
    private Date ngayHetHan;
    private Boolean taiKhoanBiKhoa;

    public BanDoc() {}

    public BanDoc(String maBanDoc, String hoTen, String email, String soDienThoai, String matKhau, String loaiTaiKhoan, Date ngayHetHan, Boolean taiKhoanBiKhoa) {
        this.maBanDoc = maBanDoc;
        this.hoTen = hoTen;
        this.email = email;
        this.soDienThoai = soDienThoai;
        this.matKhau = matKhau;
        this.loaiTaiKhoan = loaiTaiKhoan;
        this.ngayHetHan = ngayHetHan;
        this.taiKhoanBiKhoa = taiKhoanBiKhoa;
    }

    public String getMaBanDoc() { return maBanDoc; }
    public void setMaBanDoc(String maBanDoc) { this.maBanDoc = maBanDoc; }
    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
    public String getMatKhau() { return matKhau; }
    public void setMatKhau(String matKhau) { this.matKhau = matKhau; }
    public String getLoaiTaiKhoan() { return loaiTaiKhoan; }
    public void setLoaiTaiKhoan(String loaiTaiKhoan) { this.loaiTaiKhoan = loaiTaiKhoan; }
    public Date getNgayHetHan() { return ngayHetHan; }
    public void setNgayHetHan(Date ngayHetHan) { this.ngayHetHan = ngayHetHan; }
    public Boolean getTaiKhoanBiKhoa() { return taiKhoanBiKhoa; }
    public void setTaiKhoanBiKhoa(Boolean taiKhoanBiKhoa) { this.taiKhoanBiKhoa = taiKhoanBiKhoa; }
}
