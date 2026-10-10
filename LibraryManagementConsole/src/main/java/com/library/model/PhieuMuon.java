package com.library.model;

import java.time.LocalDate;

public class PhieuMuon {
    private Long id;
    private LocalDate ngayMuon;
    private LocalDate ngayTra;

    public PhieuMuon() {
    }

    public PhieuMuon(Long id, LocalDate ngayMuon, LocalDate ngayTra) {
        this.id = id;
        this.ngayMuon = ngayMuon;
        this.ngayTra = ngayTra;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getNgayMuon() {
        return ngayMuon;
    }

    public void setNgayMuon(LocalDate ngayMuon) {
        this.ngayMuon = ngayMuon;
    }

    public LocalDate getNgayTra() {
        return ngayTra;
    }

    public void setNgayTra(LocalDate ngayTra) {
        this.ngayTra = ngayTra;
    }
}
