package com.example.jjb20.dto;


public class MyPageSummaryDto {
    public long houseCount;
    public long reservationCount;

    // 호스트가 받은 예약 요청 개수
    private long receivedRequestCount;

    public long getHouseCount() {
        return houseCount;
    }

    public void setHouseCount(long houseCount) {
        this.houseCount = houseCount;
    }

    public long getReservationCount() {
        return reservationCount;
    }
    public long getReceivedRequestCount() {
        return receivedRequestCount;
    }

    public void setReservationCount(long reservationCount) {
        this.reservationCount = reservationCount;
    }
}
