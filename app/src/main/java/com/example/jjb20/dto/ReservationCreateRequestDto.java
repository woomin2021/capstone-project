package com.example.jjb20.dto;

public class ReservationCreateRequestDto {
    public ReservationCreateRequestDto() {
    }

    public long houseId;
    public long guestUserId;
    public String checkinDate;   // "2026-01-06"
    public String checkoutDate;  // "2026-01-08"
    public int guestCount;
    public int totalPrice;

    public ReservationCreateRequestDto(long houseId, long guestUserId,
                                       String checkinDate, String checkoutDate,
                                       int guestCount, int totalPrice) {
        this.houseId = houseId;
        this.guestUserId = guestUserId;
        this.checkinDate = checkinDate;
        this.checkoutDate = checkoutDate;
        this.guestCount = guestCount;
        this.totalPrice = totalPrice;
    }
}