package com.example.jjb20.dto;

public class HousesCreateRequestDto {
    private long houseId;
    private String title;
    private String description;
    private String addressLine1;
    private String city;
    private String country;
    private int pricePerNight;

    public HousesCreateRequestDto(String title, String description, String addressLine1, 
                                  String city, String country, int pricePerNight) {
        this.title = title;
        this.description = description;
        this.addressLine1 = addressLine1;
        this.city = city;
        this.country = country;
        this.pricePerNight = pricePerNight;
    }

    public long getHouseId() {
        return houseId;
    }

    public void setHouseId(long houseId) {
        this.houseId = houseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public int getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(int pricePerNight) {
        this.pricePerNight = pricePerNight;
    }
}

