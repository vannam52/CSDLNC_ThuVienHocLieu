package com.library.model;

public abstract class HocLieu {
    private Long id;
    private String tieuDe;

    protected HocLieu() {
    }

    protected HocLieu(Long id, String tieuDe) {
        this.id = id;
        this.tieuDe = tieuDe;
    }

    public Long getId() {
        return id;
    }

    public String getTieuDe() {
        return tieuDe;
    }

    public void setTieuDe(String tieuDe) {
        this.tieuDe = tieuDe;
    }
}
