package com.library.model;

public class Sach extends HocLieu {
    private String isbn;

    public Sach() {
    }

    public Sach(Long id, String tieuDe, String isbn) {
        super(id, tieuDe);
        this.isbn = isbn;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
}
