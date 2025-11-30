package com.example.jjb20.dto;

public class HousePhotoDto {
    private Long id;
    private String photoUrl;
    private Boolean isCover;
    private Integer sortOrder;

    public Long getId() {
        return id;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public Boolean getIsCover() {
        return isCover;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }
}

