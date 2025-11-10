package com.example.jjb20.dto;

public class FirebaseLoginRequestDto {
    private String idToken;

    public FirebaseLoginRequestDto(String idToken) {
        this.idToken = idToken;
    }

    public String getIdToken() { return idToken; }
    public void setIdToken(String idToken) { this.idToken = idToken; }
}
