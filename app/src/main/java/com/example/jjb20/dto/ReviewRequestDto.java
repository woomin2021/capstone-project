package com.example.jjb20.dto;

public class ReviewRequestDto {

    public long reservationId;
    public short rating;
    public String comment;

    public ReviewRequestDto(long reservationId, short rating, String comment) {
        this.reservationId = reservationId;
        this.rating = rating;
        this.comment = comment;
    }
}
