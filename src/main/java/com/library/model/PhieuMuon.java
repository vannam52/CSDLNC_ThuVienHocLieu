package com.library.model;

import java.util.Date;

public class PhieuMuon {
    private Long id;
    private BanDoc banDoc;
    private BanSao banSao;
    private Date ngayMuon;
    private String trangThai;

    public PhieuMuon() {
    }

    public PhieuMuon(Long id, BanDoc banDoc, BanSao banSao, Date ngayMuon, String trangThai) {
        this.id = id;
        this.banDoc = banDoc;
        this.banSao = banSao;
        this.ngayMuon = ngayMuon;
        this.trangThai = trangThai;
    }

    public Long getId() {
        return id;
    }

    public BanDoc getBanDoc() {
        return banDoc;
    }

    public void setBanDoc(BanDoc banDoc) {
        this.banDoc = banDoc;
    }

    public BanSao getBanSao() {
        return banSao;
    }

    public void setBanSao(BanSao banSao) {
        this.banSao = banSao;
    }

    public Date getNgayMuon() {
        return ngayMuon;
    }

    public void setNgayMuon(Date ngayMuon) {
        this.ngayMuon = ngayMuon;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}
