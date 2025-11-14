package com.example.jjb20.dto;

public class HouseReviewDTO {
    public long id;
    public long reservation_id;
    public long reviewer_guest_user_id;
    public long target_house_id;
    public int rating;
    public String comment;
    public String createdAt;

    public HouseReviewDTO() {
    }

    public HouseReviewDTO(long id, long reservation_id, long reviewer_guest_user_id, long target_house_id, int rating, String comment, String createdAt) {
        this.id = id;
        this.reservation_id = reservation_id;
        this.reviewer_guest_user_id = reviewer_guest_user_id;
        this.target_house_id = target_house_id;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getReservation_id() {
        return reservation_id;
    }

    public void setReservation_id(long reservation_id) {
        this.reservation_id = reservation_id;
    }

    public long getReviewer_guest_user_id() {
        return reviewer_guest_user_id;
    }

    public void setReviewer_guest_user_id(long reviewer_guest_user_id) {
        this.reviewer_guest_user_id = reviewer_guest_user_id;
    }

    public long getTarget_house_id() {
        return target_house_id;
    }

    public void setTarget_house_id(long target_house_id) {
        this.target_house_id = target_house_id;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
