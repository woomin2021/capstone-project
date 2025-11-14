package com.example.jjb20.dto;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class HouseDto implements Serializable {

    @SerializedName("id")
    public long id;

    @SerializedName("hostUserId")
    public long hostUserId;

    @SerializedName("title")
    public String title;

    @SerializedName("description")
    public String description;

    @SerializedName("addressLine1")
    public String addressLine1;

    @SerializedName("city")
    public String city;

    @SerializedName("country")
    public String country;

    @SerializedName("pricePerNight")
    public Integer pricePerNight;

    @SerializedName("status")
    public String status;

    @SerializedName("createdAt")
    public String createdAt;

    @SerializedName("updatedAt")
    public String updatedAt;

    @SerializedName("startDay")
    public String startDay;

    @SerializedName("endDay")
    public String endDay;

    @SerializedName("latitude")
    public Double latitude;

    @SerializedName("longitude")
    public Double longitude;

    @SerializedName("coverPhotoUrl")
    public String coverPhotoUrl;

    @SerializedName("hostName")
    public String hostName;



    @SerializedName("hostRatingAvg")
    public Double hostRatingAvg;

    @SerializedName("hostRatingCount")
    public Integer hostRatingCount;
}