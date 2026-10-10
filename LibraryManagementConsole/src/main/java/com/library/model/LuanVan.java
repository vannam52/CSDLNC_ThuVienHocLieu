package com.library.model;

public class LuanVan extends HocLieu {
    private String chuyenNganh;

    public LuanVan() {
    }

    public LuanVan(Long id, String tieuDe, String chuyenNganh) {
        super(id, tieuDe);
        this.chuyenNganh = chuyenNganh;
    }

    public String getChuyenNganh() {
        return chuyenNganh;
    }

    public void setChuyenNganh(String chuyenNganh) {
        this.chuyenNganh = chuyenNganh;
    }
}
