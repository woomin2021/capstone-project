package com.example.jjb20.dto;


public class MyPageSummaryDto {
    public long houseCount;
    public long reservationCount;

    public long getHouseCount() {
        return houseCount;
    }

    public void setHouseCount(long houseCount) {
        this.houseCount = houseCount;
    }

    public long getReservationCount() {
        return reservationCount;
    }

    public void setReservationCount(long reservationCount) {
        this.reservationCount = reservationCount;
    }
}
