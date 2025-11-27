package com.example.jjb20.dto;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class ReservationDTO implements Serializable {

    @SerializedName("id")
    public long id;

    @SerializedName("houseId")
    public long houseId;

    @SerializedName("guestUserId")
    public long guestUserId;

    @SerializedName("checkinDate")
    public String checkinDate;

    @SerializedName("checkoutDate")
    public String checkoutDate;

    @SerializedName("guestCount")
    public int guestCount;

    @SerializedName("status")
    public String status;

    @SerializedName("totalPrice")
    public int totalPrice;

    @SerializedName("createdAt")
    public String createdAt;

    @SerializedName("updatedAt")
    public String updatedAt;

    public String guestName;
    public String guestProfileImageUrl;
    public boolean hasGuestReview;
    public boolean hasHouseReview;
}
