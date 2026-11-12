package com.library.model;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.ManyToOne;
import javax.persistence.CascadeType;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class PhieuMuon {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(cascade = CascadeType.PERSIST)
    private BanDoc banDoc;

    @ManyToOne(cascade = CascadeType.PERSIST)
    private BanSao banSao;

    @Temporal(TemporalType.DATE)
    private Date ngayMuon;
    private String trangThai;

    public PhieuMuon() {}

    public PhieuMuon(BanDoc banDoc, BanSao banSao, Date ngayMuon, String trangThai) {
        this.banDoc = banDoc;
        this.banSao = banSao;
        this.ngayMuon = ngayMuon;
        this.trangThai = trangThai;
    }

    public Long getId() { return id; }

    public BanDoc getBanDoc() { return banDoc; }
    public void setBanDoc(BanDoc banDoc) { this.banDoc = banDoc; }

    public BanSao getBanSao() { return banSao; }
    public void setBanSao(BanSao banSao) { this.banSao = banSao; }

    public Date getNgayMuon() { return ngayMuon; }
    public void setNgayMuon(Date ngayMuon) { this.ngayMuon = ngayMuon; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    // Phương thức nghiệp vụ đối tượng
    public boolean isDangMuon() {
        return "Dang muon".equalsIgnoreCase(trangThai) || "Đang mượn".equalsIgnoreCase(trangThai);
    }
}
