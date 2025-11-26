package com.example.jjb20.dto;

import com.google.gson.annotations.SerializedName;

public class HostRegisterRequestDto {

    @SerializedName("realName")
    public String realName;

    @SerializedName("businessNo")
    public String businessNo;

    @SerializedName("hostingPolicy")
    public String hostingPolicy;

    public HostRegisterRequestDto(String realName, String businessNo, String hostingPolicy) {
        this.realName = realName;
        this.businessNo = businessNo;
        this.hostingPolicy = hostingPolicy;
    }
}

