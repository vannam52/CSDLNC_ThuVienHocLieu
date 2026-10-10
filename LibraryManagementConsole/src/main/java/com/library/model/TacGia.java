package com.library.model;

public class TacGia {
    private Long id;
    private String hoTen;

    public TacGia() {
    }

    public TacGia(Long id, String hoTen) {
        this.id = id;
        this.hoTen = hoTen;
    }

    public Long getId() {
        return id;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }
}
