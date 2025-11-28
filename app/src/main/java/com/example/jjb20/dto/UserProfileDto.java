package com.example.jjb20.dto;

import com.google.gson.annotations.SerializedName;

public class UserProfileDto {

    @SerializedName("temperature")
    public double temperature;

    @SerializedName("guestReviewCount")
    public long guestReviewCount;

    @SerializedName("guestRatingAvg")
    public double guestRatingAvg;

    public UserProfileDto(double temperature, long guestReviewCount, double guestRatingAvg) {
        this.temperature = temperature;
        this.guestReviewCount = guestReviewCount;
        this.guestRatingAvg = guestRatingAvg;
    }

    public double getTemperature() {
        return temperature;
    }

    public long getGuestReviewCount() {
        return guestReviewCount;
    }

    public double getGuestRatingAvg() {
        return guestRatingAvg;
    }
}
