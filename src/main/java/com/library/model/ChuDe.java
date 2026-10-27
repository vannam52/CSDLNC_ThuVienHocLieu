package com.library.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

@Entity
public class ChuDe {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String tenChuDe;

    public ChuDe() {
    }

    public ChuDe(String tenChuDe) {
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

    @Override
    public String toString() {
        return tenChuDe;
    }
}
