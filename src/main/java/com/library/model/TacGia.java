package com.library.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

@Entity
public class TacGia {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String ten;

    public TacGia() {
    }

    public TacGia(String ten) {
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

    @Override
    public String toString() {
        return ten;
    }
}
