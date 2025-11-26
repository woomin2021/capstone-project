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

    // 추가
    @SerializedName("hostId")
    private Long hostId;

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

    @SerializedName("startDay")
    private String startDay;

    @SerializedName("endDay")
    private String endDay;

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

    public String getStartDay() { return startDay; }

    public String getEndDay() { return endDay; }

    // 게터 추가
    public Long getHostId() { return hostId; }




    // 새로 추가한 어매니티 불러오기 디테일 페이지때 사용 하는거
    @SerializedName("parking")
    private Boolean parking;

    @SerializedName("wifi")
    private Boolean wifi;

    @SerializedName("airConditioning")
    private Boolean airConditioning;

    @SerializedName("heating")
    private Boolean heating;

    @SerializedName("kitchen")
    private Boolean kitchen;

    @SerializedName("washer")
    private Boolean washer;

    @SerializedName("dryer")
    private Boolean dryer;

    @SerializedName("bathtub")
    private Boolean bathtub;

    @SerializedName("diningTable")
    private Boolean diningTable;

    @SerializedName("microwave")
    private Boolean microwave;

    @SerializedName("refrigerator")
    private Boolean refrigerator;

    @SerializedName("tv")
    private Boolean tv;


    @SerializedName("bedroomCount")
    private Integer bedroomCount;

    @SerializedName("bedCount")
    private Integer bedCount;

    @SerializedName("bathroomCount")
    private Integer bathroomCount;


    public Boolean getParking() { return parking; }
    public Boolean getWifi() { return wifi; }
    public Boolean getAirConditioning() { return airConditioning; }
    public Boolean getHeating() { return heating; }
    public Boolean getKitchen() { return kitchen; }
    public Boolean getWasher() { return washer; }
    public Boolean getDryer() { return dryer; }
    public Boolean getBathtub() { return bathtub; }
    public Boolean getDiningTable() { return diningTable; }
    public Boolean getMicrowave() { return microwave; }
    public Boolean getRefrigerator() { return refrigerator; }
    public Boolean getTv() { return tv; }

    public Integer getBedroomCount() { return bedroomCount; }
    public Integer getBedCount() { return bedCount; }
    public Integer getBathroomCount() { return bathroomCount; }
}