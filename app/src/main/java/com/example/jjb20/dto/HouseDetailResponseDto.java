package com.example.jjb20.dto;

import com.google.gson.annotations.SerializedName;

public class HouseDetailResponseDto {

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

    @SerializedName("status")
    private String status;

    @SerializedName("coverPhotoUrl")
    private String coverPhotoUrl;

    @SerializedName("hostName")
    private String hostName;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("latitude")
    private Double latitude;

    @SerializedName("longitude")
    private Double longitude;


    @SerializedName("hostRatingAvg")
    private Double hostRatingAvg;

    @SerializedName("hostRatingCount")
    private Integer hostRatingCount;

    public HouseDetailResponseDto() {}



    public Long getId() { return id; }

    public String getTitle() { return title; }

    public String getDescription() { return description; }

    public String getAddressLine1() { return addressLine1; }

    public String getCity() { return city; }

    public String getCountry() { return country; }

    public Integer getPricePerNight() { return pricePerNight; }

    public String getStatus() { return status; }

    public String getCoverPhotoUrl() { return coverPhotoUrl; }

    public String getHostName() { return hostName; }

    public String getCreatedAt() { return createdAt; }

    public Double getLatitude() { return latitude; }

    public Double getLongitude() { return longitude; }


    public Double getHostRatingAvg() { return hostRatingAvg; }

    public Integer getHostRatingCount() { return hostRatingCount; }
}