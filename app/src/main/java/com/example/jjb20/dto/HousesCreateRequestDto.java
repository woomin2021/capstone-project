package com.example.jjb20.dto;

public class HousesCreateRequestDto {

    private long houseId;
    private String title;
    private String description;
    private String addressLine1;
    private String city;
    private String country;
    private int pricePerNight;
    private String startDay;
    private String endDay;
    private String imageUrl;

    // ✅ 서버의 HousesCreateRequestDto.houseAmenities 에 매핑될 중첩 DTO
    private HouseAmenitiesDto houseAmenities;

    // 기본 생성자 (Retrofit/Gson용)
    public HousesCreateRequestDto() {
    }

    // 기존에 쓰던 생성자 시그니처는 유지
    public HousesCreateRequestDto(String title,
                                  String description,
                                  String addressLine1,
                                  String city,
                                  String country,
                                  int pricePerNight,
                                  int bedroomCount,
                                  int bedCount,
                                  int bathroomCount,
                                  String startDay,
                                  String endDay,
                                  String imageUrl) {

        this.title = title;
        this.description = description;
        this.addressLine1 = addressLine1;
        this.city = city;
        this.country = country;
        this.pricePerNight = pricePerNight;
        this.startDay = startDay;
        this.endDay = endDay;
        this.imageUrl = imageUrl;

        // ✅ 침실/침대/욕실 수를 중첩 DTO 안에 넣어서 전송
        HouseAmenitiesDto ha = new HouseAmenitiesDto();
        ha.setBedroomCount(bedroomCount);
        ha.setBedCount(bedCount);
        ha.setBathroomCount(bathroomCount);
        this.houseAmenities = ha;
    }

    // ======= getter / setter =======

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

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
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

    // ✅ houseAmenities 필드 접근자
    public HouseAmenitiesDto getHouseAmenities() {
        return houseAmenities;
    }

    public void setHouseAmenities(HouseAmenitiesDto houseAmenities) {
        this.houseAmenities = houseAmenities;
    }

    // ======= 중첩 DTO: 서버의 HouseAmenitiesDto와 필드 이름 맞춤 =======
    public static class HouseAmenitiesDto {
        private int bedroomCount;
        private int bedCount;
        private int bathroomCount;

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
    }
}
