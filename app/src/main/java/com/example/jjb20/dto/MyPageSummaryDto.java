package com.example.jjb20.dto;


public class MyPageSummaryDto {
    public Long houseCount;
    public Long reservationCount;

    // 호스트가 받은 예약 요청 개수
    private Long receivedRequestCount;

    public Long getHouseCount() {
        return houseCount;
    }

    public void setHouseCount(Long houseCount) {
        this.houseCount = houseCount;
    }

    public Long getReservationCount() {
        return reservationCount;
    }
    public Long getReceivedRequestCount() {
        return receivedRequestCount;
    }

    public void setReservationCount(Long reservationCount) {
        this.reservationCount = reservationCount;
    }
}
