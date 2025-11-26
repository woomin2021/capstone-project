package com.example.jjb20.dto;

import com.google.gson.annotations.SerializedName;

public class HostProfileDto {

    @SerializedName("userId")
    public long userId;

    @SerializedName("displayName")
    public String displayName;

    @SerializedName("payoutMethod")
    public String payoutMethod;

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

    public HostProfileDto(String displayName, String businessNo, String hostingPolicy) {
        this.displayName = displayName;
        this.businessNo = businessNo;
        this.hostingPolicy = hostingPolicy;
    }
}
