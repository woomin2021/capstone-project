package com.example.jjb20.dto;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ReservationDTO implements Serializable {

    @SerializedName("id")
    public long id;

    @SerializedName("house_id")
    public long houseId;

    @SerializedName("guest_user_id")
    public long guestUserId;

    @SerializedName("checkin_date")
    public String checkinDate;

    @SerializedName("checkout_date")
    public String checkoutDate;

    @SerializedName("guests_count")
    public int guestsCount;

    @SerializedName("status")
    public String status;   // 예: pending, confirmed, canceled

    @SerializedName("total_price")
    public int totalPrice;

    @SerializedName("created_at")
    public String createdAt;

    @SerializedName("updated_at")
    public String updatedAt;
}
