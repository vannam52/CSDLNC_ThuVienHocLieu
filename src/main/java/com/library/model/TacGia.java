package com.library.model;

public class TacGia {
    private Long id;
    private String ten;

    public TacGia() {
    }

    public TacGia(Long id, String ten) {
        this.id = id;
        this.ten = ten;
    }

    public Long getId() {
        return id;
    }

    public String getTen() {
        return ten;
    }

    public void setTen(String ten) {
        this.ten = ten;
    }
}
