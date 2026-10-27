package com.library.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.CascadeType;

@Entity
public class BanSao {
    @Id
    private String maBanSao;

    @ManyToOne(cascade = CascadeType.PERSIST)
    private HocLieu hocLieu;
    private String trangThai;
    private String tinhTrangVatLy;
    private String viTriKe;

    public BanSao() {}

    public BanSao(String maBanSao, HocLieu hocLieu, String trangThai, String tinhTrangVatLy, String viTriKe) {
        this.maBanSao = maBanSao;
        this.hocLieu = hocLieu;
        this.trangThai = trangThai;
        this.tinhTrangVatLy = tinhTrangVatLy;
        this.viTriKe = viTriKe;
    }

    public String getMaBanSao() { return maBanSao; }
    public void setMaBanSao(String maBanSao) { this.maBanSao = maBanSao; }
    public HocLieu getHocLieu() { return hocLieu; }
    public void setHocLieu(HocLieu hocLieu) { this.hocLieu = hocLieu; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public String getTinhTrangVatLy() { return tinhTrangVatLy; }
    public void setTinhTrangVatLy(String tinhTrangVatLy) { this.tinhTrangVatLy = tinhTrangVatLy; }
    public String getViTriKe() { return viTriKe; }
    public void setViTriKe(String viTriKe) { this.viTriKe = viTriKe; }
}
