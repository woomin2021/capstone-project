package com.example.jjb20.dto;

public class RegisterRequestDto {
    public String idToken;
    public String name;
    public String phone;

    public RegisterRequestDto(String idToken, String name, String phone) {
        this.idToken = idToken;
        this.name = name;
        this.phone = phone;
    }
}
