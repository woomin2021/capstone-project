package com.example.jjb20.dto;

import com.google.gson.annotations.SerializedName;

public class UserResponseDto {
    public Long id;
    public String email;
    public String name;
    public String phone;

    @SerializedName("firebaseUid")
    public String firebaseUid;

    // 서버가 문자열로 내려주면 String, epoch면 long으로 바꾸세요
    public String createdAt;
    public String updatedAt;
}
