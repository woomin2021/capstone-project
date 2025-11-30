package com.example.jjb20.dto;

import com.google.gson.annotations.SerializedName;

public class HostProfileDto {

    @SerializedName("userId")
    public long userId;

    @SerializedName("realName")
    public String realName;

    @SerializedName("businessNo")
    public String businessNo;

    @SerializedName("hostingPolicy")
    public String hostingPolicy;

    @SerializedName("ratingAvg")
    public double ratingAvg;

    @SerializedName("ratingCount")
    public int ratingCount;

    @SerializedName("temperature")
    public double temperature;   // 온도 추가

    @SerializedName("profileImageUrl")
    public String profileImageUrl;

    @SerializedName("receivedRequestCount")
    public long receivedRequestCount;

    public HostProfileDto(String realName, String businessNo, String hostingPolicy) {
        this.realName = realName;
        this.businessNo = businessNo;
        this.hostingPolicy = hostingPolicy;
    }
}
