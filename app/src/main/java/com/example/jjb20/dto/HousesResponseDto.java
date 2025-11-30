package com.example.jjb20.dto;

import com.google.gson.annotations.SerializedName;

public class HousesResponseDto {
    @SerializedName("id")
    private Long id;
    @SerializedName("title")
    private String title;
    @SerializedName("description")
    private String description;
    @SerializedName("addressLine1")
    private String addressLine1;
    @SerializedName("city")
    private String city;
    @SerializedName("country")
    private String country;
    @SerializedName("pricePerNight")
    private Integer pricePerNight;
    
    // Gson은 기본적으로 알 수 없는 필드(photos, amenities, houseAmenities, host 등) 무시
    // 순환 참조를 방지하기 위해 이 필드들은 DTO에 포함하지 않음.

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(Integer pricePerNight) {
        this.pricePerNight = pricePerNight;
    }
}

