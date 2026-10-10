package com.library.model;

public class ChuDe {
    private Long id;
    private String tenChuDe;

    public ChuDe() {
    }

    public ChuDe(Long id, String tenChuDe) {
        this.id = id;
        this.tenChuDe = tenChuDe;
    }

    public Long getId() {
        return id;
    }

    public String getTenChuDe() {
        return tenChuDe;
    }

    public void setTenChuDe(String tenChuDe) {
        this.tenChuDe = tenChuDe;
    }
}
