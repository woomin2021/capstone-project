package com.example.jjb20.entity;

import java.io.Serializable;

public class HotelItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String title;
    private final String date;      // 추천 섹션에서 사용(없으면 빈 문자열로)
    private final String location;  // HOT 섹션에서 사용(없으면 빈 문자열로)
    private final String price;
    private final int imageRes;

    public HotelItem(String title, String date, String location, String price, int imageRes) {
        this.title = title;
        this.date = date;
        this.location = location;
        this.price = price;
        this.imageRes = imageRes;
    }

    public String getTitle()    { return title; }
    public String getDate()     { return date; }
    public String getLocation() { return location; }
    public String getPrice()    { return price; }
    public int getImageRes()    { return imageRes; }
}