package com.library.model;

public class VideoBaiGiang extends HocLieu {
    private String url;

    public VideoBaiGiang() {
    }

    public VideoBaiGiang(Long id, String tieuDe, String url) {
        super(id, tieuDe);
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
