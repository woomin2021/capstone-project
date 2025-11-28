package com.example.jjb20.dto;

import java.util.List;

public class HousesCreateRequestDto {
    private long houseId;
    private String title;
    private String description;
    private String shortDescription;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String country;
    private int pricePerNight;
    private int bedroomCount;
    private int bedCount;
    private int bathroomCount;
    private String startDay;
    private String endDay;
    private String imageUrl;
    private Double latitude;
    private Double longitude;
    private List<PhotoRequest> photos;

    public HousesCreateRequestDto(String title, String description, String shortDescription,
                                  String addressLine1, String addressLine2,
                                  String city, String country, int pricePerNight,
                                  int bedroomCount, int bedCount, int bathroomCount,
                                  String startDay, String endDay, String imageUrl,
                                  Double latitude, Double longitude) {
        this.title = title;
        this.description = description;
        this.shortDescription = shortDescription;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.city = city;
        this.country = country;
        this.pricePerNight = pricePerNight;
        this.bedroomCount = bedroomCount;
        this.bedCount = bedCount;
        this.bathroomCount = bathroomCount;
        this.startDay = startDay;
        this.endDay = endDay;
        this.imageUrl = imageUrl;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public long getHouseId() {
        return houseId;
    }

    public void setHouseId(long houseId) {
        this.houseId = houseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public int getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(int pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public int getBedroomCount() {
        return bedroomCount;
    }

    public void setBedroomCount(int bedroomCount) {
        this.bedroomCount = bedroomCount;
    }

    public int getBedCount() {
        return bedCount;
    }

    public void setBedCount(int bedCount) {
        this.bedCount = bedCount;
    }

    public int getBathroomCount() {
        return bathroomCount;
    }

    public void setBathroomCount(int bathroomCount) {
        this.bathroomCount = bathroomCount;
    }

    public String getStartDay() {
        return startDay;
    }

    public void setStartDay(String startDay) {
        this.startDay = startDay;
    }

    public String getEndDay() {
        return endDay;
    }

    public void setEndDay(String endDay) {
        this.endDay = endDay;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public List<PhotoRequest> getPhotos() {
        return photos;
    }

    public void setPhotos(List<PhotoRequest> photos) {
        this.photos = photos;
    }

    public static class PhotoRequest {
        private String url;
        private Boolean isCover;
        private Integer sortOrder;

        public PhotoRequest(String url, Boolean isCover, Integer sortOrder) {
            this.url = url;
            this.isCover = isCover;
            this.sortOrder = sortOrder;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public Boolean getIsCover() {
            return isCover;
        }

        public void setIsCover(Boolean isCover) {
            this.isCover = isCover;
        }

        public Integer getSortOrder() {
            return sortOrder;
        }

        public void setSortOrder(Integer sortOrder) {
            this.sortOrder = sortOrder;
        }
    }
}